package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoring.dto.GradingResult;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.GradingAsyncService;
import com.tutoring.service.QwenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradingAsyncServiceImpl implements GradingAsyncService {

    private final SubmissionMapper submissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionMapper questionMapper;
    private final AssignmentMapper assignmentMapper;
    private final MessageMapper messageMapper;
    private final QwenService qwenService;

    private static final String SUBJECTIVE_TYPE = "SUBJECTIVE";

    @Override
    @Async("taskExecutor")
    @Transactional
    public void processGrading(Long submissionId, Long studentId, Long assignmentId) {
        log.info("开始异步批改任务，submissionId: {}, studentId: {}, assignmentId: {}, 线程: {}", 
            submissionId, studentId, assignmentId, Thread.currentThread().getName());

        try {
            Submission submission = submissionMapper.selectById(submissionId);
            if (submission == null) {
                log.error("提交记录不存在: {}", submissionId);
                return;
            }

            submission.setStatus(2);
            submissionMapper.updateById(submission);
            log.info("更新提交状态为批改中: {}", submissionId);

            List<StudentAnswer> answers = studentAnswerMapper.selectList(
                new LambdaQueryWrapper<StudentAnswer>()
                    .eq(StudentAnswer::getSubmissionId, submissionId)
                    .eq(StudentAnswer::getIsDraft, 0)
            );

            if (answers.isEmpty()) {
                log.warn("没有找到学生答案: submissionId={}", submissionId);
                submission.setStatus(3);
                submission.setAiTotalScore(0);
                submission.setFinalTotalScore(0);
                submission.setReviewStatus(0);
                submissionMapper.updateById(submission);
                return;
            }

            List<Long> questionIds = answers.stream()
                .map(StudentAnswer::getQuestionId)
                .distinct()
                .collect(Collectors.toList());

            List<Question> questions = questionMapper.selectList(
                new LambdaQueryWrapper<Question>()
                    .in(Question::getId, questionIds)
            );

            Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));

            boolean hasSubjectiveQuestion = false;
            int totalAiScore = 0;

            for (StudentAnswer answer : answers) {
                Question question = questionMap.get(answer.getQuestionId());
                if (question == null) {
                    log.warn("题目不存在: questionId={}", answer.getQuestionId());
                    continue;
                }

                if (SUBJECTIVE_TYPE.equals(question.getType())) {
                    hasSubjectiveQuestion = true;
                    int aiScore = gradeSubjectiveQuestion(answer, question);
                    totalAiScore += aiScore;
                } else {
                    int score = gradeObjectiveQuestion(answer, question);
                    totalAiScore += score;
                }
            }

            submission.setAiTotalScore(totalAiScore);
            
            if (hasSubjectiveQuestion) {
                submission.setReviewStatus(1);
            } else {
                submission.setReviewStatus(0);
                submission.setFinalTotalScore(totalAiScore);
            }
            
            submission.setStatus(3);
            submissionMapper.updateById(submission);

            log.info("批改完成: submissionId={}, aiTotalScore={}, reviewStatus={}", 
                submissionId, totalAiScore, submission.getReviewStatus());

            sendNotification(studentId, assignmentId, totalAiScore);

        } catch (Exception e) {
            log.error("批改任务执行失败: submissionId={}", submissionId, e);
            
            try {
                Submission submission = submissionMapper.selectById(submissionId);
                if (submission != null) {
                    submission.setStatus(4);
                    submissionMapper.updateById(submission);
                }
            } catch (Exception ex) {
                log.error("更新失败状态异常", ex);
            }
        }
    }

    private int gradeObjectiveQuestion(StudentAnswer answer, Question question) {
        String correctAnswer = question.getAnswer();
        String studentAnswer = answer.getAnswer();
        
        int score = 0;
        
        if (correctAnswer != null && studentAnswer != null) {
            String normalizedCorrect = correctAnswer.trim().toUpperCase();
            String normalizedStudent = studentAnswer.trim().toUpperCase();
            
            if (normalizedCorrect.equals(normalizedStudent)) {
                score = question.getScore();
            }
        }

        answer.setScore(score);
        answer.setAiScore(score);
        answer.setFinalScore(score);
        answer.setGraderType("AUTO");
        answer.setReviewStatus(0);
        studentAnswerMapper.updateById(answer);

        log.debug("客观题评分: questionId={}, score={}", question.getId(), score);
        return score;
    }

    private int gradeSubjectiveQuestion(StudentAnswer answer, Question question) {
        try {
            String questionContent = question.getContent();
            String referenceAnswer = question.getReferenceAnswer();
            String studentAnswerContent = answer.getAnswerContent() != null 
                ? answer.getAnswerContent() 
                : answer.getAnswer();
            int maxScore = question.getScore();

            GradingResult result = qwenService.gradeAnswer(
                questionContent, 
                referenceAnswer, 
                studentAnswerContent, 
                maxScore
            );

            int aiScore = result.getScore() != null ? result.getScore() : 0;
            aiScore = Math.min(aiScore, maxScore);
            aiScore = Math.max(aiScore, 0);

            answer.setAiScore(aiScore);
            answer.setScore(aiScore);
            answer.setAiFeedback(buildAiFeedback(result));
            answer.setGraderType("AI");
            answer.setReviewStatus(1);
            
            studentAnswerMapper.updateById(answer);

            log.info("主观题AI评分: questionId={}, aiScore={}", question.getId(), aiScore);
            return aiScore;

        } catch (Exception e) {
            log.error("主观题AI评分失败: questionId={}", question.getId(), e);
            
            answer.setAiScore(0);
            answer.setScore(0);
            answer.setAiFeedback("AI评分失败，等待教师人工批改");
            answer.setGraderType("AI");
            answer.setReviewStatus(1);
            studentAnswerMapper.updateById(answer);
            
            return 0;
        }
    }

    private String buildAiFeedback(GradingResult result) {
        StringBuilder feedback = new StringBuilder();
        
        if (result.getFeedback() != null) {
            feedback.append(result.getFeedback());
        }
        
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            feedback.append("\n\n【错误点】\n");
            for (String error : result.getErrors()) {
                feedback.append("- ").append(error).append("\n");
            }
        }
        
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            feedback.append("\n【改进建议】\n");
            for (String suggestion : result.getSuggestions()) {
                feedback.append("- ").append(suggestion).append("\n");
            }
        }
        
        return feedback.toString();
    }

    private void sendNotification(Long studentId, Long assignmentId, Integer score) {
        try {
            Assignment assignment = assignmentMapper.selectById(assignmentId);
            String assignmentTitle = assignment != null ? assignment.getTitle() : "作业";

            Message message = new Message();
            message.setTitle("作业批改完成");
            message.setContent(String.format("您的作业「%s」已完成AI批改，得分：%d分。%s", 
                assignmentTitle, score, 
                score >= 60 ? "表现不错，继续保持！" : "请查看详细反馈，继续努力！"));
            message.setType("GRADING");
            message.setRelatedId(studentId);
            message.setRelatedType("STUDENT");

            messageMapper.insert(message);
            log.info("发送批改完成通知: studentId={}, assignmentId={}", studentId, assignmentId);

        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }
}
