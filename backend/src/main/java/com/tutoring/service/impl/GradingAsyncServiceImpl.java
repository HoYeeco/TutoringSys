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
    private static final String ESSAY_TYPE = "ESSAY";

    private static final List<String> OBJECTIVE_TYPES = List.of("SINGLE", "MULTIPLE", "JUDGMENT", "JUDGE");
    private static final List<String> SUBJECTIVE_TYPES = List.of(SUBJECTIVE_TYPE, ESSAY_TYPE, "ESSAY");

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
            int processedCount = 0;
            int subjectiveCount = 0;
            int objectiveCount = 0;

            for (StudentAnswer answer : answers) {
                Question question = questionMap.get(answer.getQuestionId());
                if (question == null) {
                    log.warn("题目不存在: questionId={}, answerId={}, 跳过该答案", 
                        answer.getQuestionId(), answer.getId());
                    continue;
                }

                String rawType = question.getType();
                String questionType = rawType != null ? rawType.toUpperCase() : "";
                
                log.debug("处理题目: answerId={}, questionId={}, type='{}', normalized='{}'", 
                    answer.getId(), question.getId(), rawType, questionType);

                if (isSubjectiveType(questionType)) {
                    hasSubjectiveQuestion = true;
                    subjectiveCount++;
                    log.info("发现主观题: questionId={}, type={}, 开始AI评分", question.getId(), rawType);
                    try {
                        int aiScore = gradeSubjectiveQuestion(answer, question);
                        totalAiScore += aiScore;
                        processedCount++;
                    } catch (Exception ex) {
                        log.error("主观题评分异常: questionId={}, 使用默认0分", question.getId(), ex);
                        safeSetDefaultScore(answer, 0);
                    }
                } else {
                    objectiveCount++;
                    if (!OBJECTIVE_TYPES.contains(questionType) && !questionType.isEmpty()) {
                        log.warn("未知题型 '{}' 被当作客观题处理: questionId={}", rawType, question.getId());
                    }
                    try {
                        int score = gradeObjectiveQuestion(answer, question);
                        totalAiScore += score;
                        processedCount++;
                    } catch (Exception ex) {
                        log.error("客观题评分异常：questionId={}, 使用默认 0 分", question.getId(), ex);
                        safeSetDefaultScore(answer, 0);
                    }
                }
            }

            log.info("批改统计: submissionId={}, 总题数={}, 主观题={}, 客观题={}, 已处理={}, 总分={}", 
                submissionId, answers.size(), subjectiveCount, objectiveCount, processedCount, totalAiScore);

            submission.setAiTotalScore(totalAiScore);
            
            LocalDateTime now = LocalDateTime.now();
            
            if (hasSubjectiveQuestion) {
                submission.setReviewStatus(1);
                log.info("作业包含主观题，AI已预评分，待教师复核: submissionId={}, aiTotalScore={}", 
                    submissionId, totalAiScore);
            } else {
                submission.setReviewStatus(0);
                submission.setFinalTotalScore(totalAiScore);
                submission.setReviewTime(now);
                log.info("全客观题作业，自动批改完成: submissionId={}, finalTotalScore={}", 
                    submissionId, totalAiScore);
            }
            
            submission.setStatus(3);
            submissionMapper.updateById(submission);

            log.info("批改完成: submissionId={}, aiTotalScore={}, reviewStatus={}, hasSubjective={}", 
                submissionId, totalAiScore, submission.getReviewStatus(), hasSubjectiveQuestion);

            sendNotification(studentId, assignmentId, totalAiScore, hasSubjectiveQuestion);

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

    private boolean isSubjectiveType(String questionType) {
        return SUBJECTIVE_TYPES.contains(questionType);
    }

    private void safeSetDefaultScore(StudentAnswer answer, int defaultScore) {
        try {
            answer.setScore(defaultScore);
            answer.setAiScore(defaultScore);
            answer.setFinalScore(defaultScore);
            if (answer.getGraderType() == null || "PENDING".equals(answer.getGraderType())) {
                answer.setGraderType("AUTO");
                answer.setReviewStatus(0);
            }
            studentAnswerMapper.updateById(answer);
            log.info("安全设置默认分: answerId={}, score={}", answer.getId(), defaultScore);
        } catch (Exception e) {
            log.error("安全设置默认分也失败: answerId={}", answer.getId(), e);
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
            answer.setAiFeedback("【错误点】AI评分失败\n\n【改进建议】请稍后重试或联系管理员");
            answer.setGraderType("AI");
            answer.setReviewStatus(1);
            studentAnswerMapper.updateById(answer);

            return 0;
        }
    }

    private String buildAiFeedback(GradingResult result) {
        StringBuilder feedback = new StringBuilder();

        // 错误点 - 多个错误用顿号分隔，每个错误不超过6个字
        if (result.getErrors() != null && !result.getErrors().isEmpty()) {
            feedback.append("【错误点】");
            List<String> truncatedErrors = result.getErrors().stream()
                .map(e -> e.length() > 6 ? e.substring(0, 6) : e)
                .distinct()
                .collect(Collectors.toList());
            String errorsStr = String.join("、", truncatedErrors);
            feedback.append(errorsStr);
        }

        // 改进建议 - 合并为一段文本
        if (result.getSuggestions() != null && !result.getSuggestions().isEmpty()) {
            if (feedback.length() > 0) {
                feedback.append("\n\n");
            }
            feedback.append("【改进建议】");
            String suggestionsStr = String.join("；", result.getSuggestions());
            feedback.append(suggestionsStr);
        }

        return feedback.toString().trim();
    }

    private void sendNotification(Long studentId, Long assignmentId, Integer score, boolean hasSubjective) {
        try {
            Assignment assignment = assignmentMapper.selectById(assignmentId);
            String assignmentTitle = assignment != null ? assignment.getTitle() : "作业";

            Message message = new Message();
            if (hasSubjective) {
                message.setTitle("作业AI预批改完成");
                message.setContent(String.format("您的作业「%s」已完成AI预批改（%s），AI预评分：%d分。主观题部分待教师复核后确定最终得分。", 
                    assignmentTitle, score >= 60 ? "表现不错" : "请查看详细反馈", score));
            } else {
                message.setTitle("作业批改完成");
                message.setContent(String.format("您的作业「%s」已自动批改完成，得分：%d分。%s", 
                    assignmentTitle, score, 
                    score >= 60 ? "表现不错，继续保持！" : "请查看详细反馈，继续努力！"));
            }
            message.setType("GRADING");
            message.setRelatedId(studentId);
            message.setRelatedType("STUDENT");

            messageMapper.insert(message);
            log.info("发送批改完成通知: studentId={}, assignmentId={}, hasSubjective={}", studentId, assignmentId, hasSubjective);

        } catch (Exception e) {
            log.error("发送通知失败", e);
        }
    }
}
