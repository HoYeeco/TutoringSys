package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.GradingDetailVo;
import com.tutoringsys.common.dto.GradingListVo;
import com.tutoringsys.dao.entity.*;
import com.tutoringsys.dao.mapper.*;
import com.tutoringsys.service.StudentGradingService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class StudentGradingServiceImpl implements StudentGradingService {

    @Resource
    private SubmissionMapper submissionMapper;
    
    @Resource
    private AssignmentMapper assignmentMapper;
    
    @Resource
    private CourseMapper courseMapper;
    
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private StudentAnswerMapper answerMapper;
    
    @Resource
    private QuestionMapper questionMapper;
    
    @Resource
    private ErrorBookMapper errorBookMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> getGradingList(Long studentId, int page, int size, Long courseId, String sortBy, String sortOrder) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getStudentId, studentId)
               .isNotNull(Submission::getReviewTime)
               .orderByDesc(Submission::getReviewTime);
        
        IPage<Submission> submissionPage = submissionMapper.selectPage(new Page<>(page, size), wrapper);
        
        List<GradingListVo> records = new ArrayList<>();
        for (Submission submission : submissionPage.getRecords()) {
            GradingListVo vo = convertToListVo(submission);
            records.add(vo);
        }
        
        result.put("records", records);
        result.put("total", submissionPage.getTotal());
        result.put("current", submissionPage.getCurrent());
        result.put("size", submissionPage.getSize());
        
        Map<String, Object> stats = calculateStats(studentId);
        result.put("stats", stats);
        
        List<Map<String, Object>> scoreTrend = getScoreTrend(studentId);
        result.put("scoreTrend", scoreTrend);
        
        List<Map<String, Object>> courseCompare = getCourseCompare(studentId);
        result.put("courseCompare", courseCompare);
        
        return result;
    }

    @Override
    public GradingDetailVo getGradingDetail(Long submissionId, Long studentId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null || !submission.getStudentId().equals(studentId)) {
            return null;
        }
        
        GradingDetailVo detail = new GradingDetailVo();
        detail.setId(submission.getId());
        detail.setAssignmentId(submission.getAssignmentId());
        
        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment != null) {
            detail.setAssignmentTitle(assignment.getTitle());
            
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                detail.setCourseName(course.getName());
                
                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    detail.setTeacherName(teacher.getRealName());
                }
            }
        }
        
        detail.setStudentScore(submission.getFinalTotalScore() != null ? submission.getFinalTotalScore() : 
                              (submission.getAiTotalScore() != null ? submission.getAiTotalScore() : 0));
        detail.setTotalScore(assignment != null ? assignment.getTotalScore() : 100);
        
        if (detail.getTotalScore() > 0) {
            detail.setAccuracy(Math.round(detail.getStudentScore() * 100.0 / detail.getTotalScore() * 10) / 10.0);
        } else {
            detail.setAccuracy(0.0);
        }
        
        if (submission.getReviewTime() != null) {
            detail.setGradingTime(DATE_FORMAT.format(submission.getReviewTime()));
        }
        
        detail.setGradingType("AI");
        
        List<GradingDetailVo.GradingQuestionVo> questions = getQuestionDetails(submissionId);
        detail.setQuestions(questions);
        
        return detail;
    }

    @Override
    public boolean addToErrorBook(Long studentId, Long questionId) {
        LambdaQueryWrapper<ErrorBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErrorBook::getStudentId, studentId)
               .eq(ErrorBook::getQuestionId, questionId);
        
        if (errorBookMapper.selectCount(wrapper) > 0) {
            return true;
        }
        
        ErrorBook errorBook = new ErrorBook();
        errorBook.setStudentId(studentId);
        errorBook.setQuestionId(questionId);
        errorBook.setCreateTime(new Date());
        
        return errorBookMapper.insert(errorBook) > 0;
    }

    @Override
    public boolean batchAddToErrorBook(Long studentId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return true;
        }
        
        for (Long questionId : questionIds) {
            addToErrorBook(studentId, questionId);
        }
        return true;
    }

    private GradingListVo convertToListVo(Submission submission) {
        GradingListVo vo = new GradingListVo();
        vo.setId(submission.getId());
        vo.setAssignmentId(submission.getAssignmentId());
        
        Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
        if (assignment != null) {
            vo.setTitle(assignment.getTitle());
            vo.setTotalScore(assignment.getTotalScore());
            
            Course course = courseMapper.selectById(assignment.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getName());
                vo.setCourseId(course.getId());
                
                User teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getRealName());
                }
            }
            
            if (assignment.getDeadline() != null) {
                vo.setDeadline(DATE_FORMAT.format(assignment.getDeadline()));
            }
            if (assignment.getCreateTime() != null) {
                vo.setCreateTime(DATE_FORMAT.format(assignment.getCreateTime()));
            }
        }
        
        vo.setScore(submission.getFinalTotalScore() != null ? submission.getFinalTotalScore() : 
                   (submission.getAiTotalScore() != null ? submission.getAiTotalScore() : 0));
        
        if (vo.getTotalScore() != null && vo.getTotalScore() > 0) {
            vo.setAccuracy(Math.round(vo.getScore() * 100.0 / vo.getTotalScore() * 10) / 10.0);
        } else {
            vo.setAccuracy(0.0);
        }
        
        if (submission.getReviewTime() != null) {
            vo.setGradingTime(DATE_FORMAT.format(submission.getReviewTime()));
        }
        
        if (submission.getSubmitTime() != null) {
            vo.setSubmitTime(DATE_FORMAT.format(submission.getSubmitTime()));
        }
        
        vo.setGradingType("AI");
        
        LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(StudentAnswer::getSubmissionId, submission.getId());
        List<StudentAnswer> answers = answerMapper.selectList(answerWrapper);
        
        int errorCount = 0;
        for (StudentAnswer answer : answers) {
            if (answer.getFinalScore() == null || answer.getFinalScore() == 0) {
                errorCount++;
            }
        }
        vo.setErrorCount(errorCount);
        
        return vo;
    }

    private Map<String, Object> calculateStats(Long studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getStudentId, studentId)
               .isNotNull(Submission::getReviewTime);
        
        List<Submission> submissions = submissionMapper.selectList(wrapper);
        
        if (submissions.isEmpty()) {
            stats.put("averageScore", 0);
            stats.put("gradedCount", 0);
            stats.put("accuracy", 0);
            stats.put("errorCount", 0);
            return stats;
        }
        
        double totalScore = 0;
        double totalAccuracy = 0;
        int errorCount = 0;
        
        for (Submission submission : submissions) {
            Integer score = submission.getFinalTotalScore() != null ? submission.getFinalTotalScore() : 
                           (submission.getAiTotalScore() != null ? submission.getAiTotalScore() : 0);
            totalScore += score;
            
            Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
            if (assignment != null && assignment.getTotalScore() > 0) {
                totalAccuracy += score * 100.0 / assignment.getTotalScore();
            }
            
            LambdaQueryWrapper<StudentAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(StudentAnswer::getSubmissionId, submission.getId());
            List<StudentAnswer> answers = answerMapper.selectList(answerWrapper);
            
            for (StudentAnswer answer : answers) {
                if (answer.getFinalScore() == null || answer.getFinalScore() == 0) {
                    errorCount++;
                }
            }
        }
        
        stats.put("averageScore", Math.round(totalScore / submissions.size() * 10) / 10.0);
        stats.put("gradedCount", submissions.size());
        stats.put("accuracy", Math.round(totalAccuracy / submissions.size() * 10) / 10.0);
        stats.put("errorCount", errorCount);
        
        return stats;
    }

    private List<Map<String, Object>> getScoreTrend(Long studentId) {
        List<Map<String, Object>> trend = new ArrayList<>();
        
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getStudentId, studentId)
               .isNotNull(Submission::getReviewTime)
               .orderByAsc(Submission::getReviewTime)
               .last("LIMIT 10");
        
        List<Submission> submissions = submissionMapper.selectList(wrapper);
        
        SimpleDateFormat labelFormat = new SimpleDateFormat("MM-dd");
        
        for (int i = 0; i < submissions.size(); i++) {
            Submission submission = submissions.get(i);
            Map<String, Object> item = new HashMap<>();
            
            Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
            Integer score = submission.getFinalTotalScore() != null ? submission.getFinalTotalScore() : 
                           (submission.getAiTotalScore() != null ? submission.getAiTotalScore() : 0);
            
            if (assignment != null && assignment.getTotalScore() > 0) {
                item.put("score", Math.round(score * 100.0 / assignment.getTotalScore()));
            } else {
                item.put("score", 0);
            }
            
            if (submission.getReviewTime() != null) {
                item.put("label", labelFormat.format(submission.getReviewTime()));
            } else {
                item.put("label", "第" + (i + 1) + "次");
            }
            
            trend.add(item);
        }
        
        return trend;
    }

    private List<Map<String, Object>> getCourseCompare(Long studentId) {
        List<Map<String, Object>> compare = new ArrayList<>();
        
        LambdaQueryWrapper<Submission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Submission::getStudentId, studentId)
               .isNotNull(Submission::getReviewTime);
        
        List<Submission> submissions = submissionMapper.selectList(wrapper);
        
        Map<Long, List<Submission>> courseSubmissions = new HashMap<>();
        
        for (Submission submission : submissions) {
            Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
            if (assignment != null) {
                courseSubmissions.computeIfAbsent(assignment.getCourseId(), k -> new ArrayList<>())
                                 .add(submission);
            }
        }
        
        for (Map.Entry<Long, List<Submission>> entry : courseSubmissions.entrySet()) {
            Long courseId = entry.getKey();
            List<Submission> courseSubmissionList = entry.getValue();
            
            Course course = courseMapper.selectById(courseId);
            if (course == null) continue;
            
            double totalScore = 0;
            int count = 0;
            
            for (Submission submission : courseSubmissionList) {
                Assignment assignment = assignmentMapper.selectById(submission.getAssignmentId());
                Integer score = submission.getFinalTotalScore() != null ? submission.getFinalTotalScore() : 
                               (submission.getAiTotalScore() != null ? submission.getAiTotalScore() : 0);
                
                if (assignment != null && assignment.getTotalScore() > 0) {
                    totalScore += score * 100.0 / assignment.getTotalScore();
                    count++;
                }
            }
            
            if (count > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("courseName", course.getName());
                item.put("averageScore", Math.round(totalScore / count * 10) / 10.0);
                compare.add(item);
            }
        }
        
        return compare;
    }

    private List<GradingDetailVo.GradingQuestionVo> getQuestionDetails(Long submissionId) {
        List<GradingDetailVo.GradingQuestionVo> questions = new ArrayList<>();
        
        LambdaQueryWrapper<StudentAnswer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentAnswer::getSubmissionId, submissionId)
               .orderByAsc(StudentAnswer::getQuestionId);
        
        List<StudentAnswer> answers = answerMapper.selectList(wrapper);
        
        for (StudentAnswer answer : answers) {
            GradingDetailVo.GradingQuestionVo qv = new GradingDetailVo.GradingQuestionVo();
            
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question == null) continue;
            
            qv.setQuestionId(question.getId());
            qv.setType(question.getType());
            qv.setContent(question.getContent());
            qv.setOptions(question.getOptions());
            qv.setCorrectAnswer(question.getAnswer());
            qv.setMaxScore(question.getScore());
            qv.setAnalysis(question.getAnalysis());
            
            qv.setStudentAnswer(answer.getAnswer());
            qv.setScore(answer.getFinalScore() != null ? answer.getFinalScore() : 
                       (answer.getAiScore() != null ? answer.getAiScore() : 0));
            
            boolean isCorrect = false;
            if ("single".equals(question.getType()) || "multiple".equals(question.getType()) || "judgment".equals(question.getType())) {
                if (question.getAnswer() != null && answer.getAnswer() != null) {
                    isCorrect = question.getAnswer().equalsIgnoreCase(answer.getAnswer());
                }
            } else {
                isCorrect = qv.getScore() >= question.getScore() * 0.8;
            }
            
            qv.setIsCorrect(isCorrect);
            
            if (answer.getFeedback() != null) {
                qv.setAiFeedback(answer.getFeedback());
                qv.setErrorTags(parseErrorTags(answer.getFeedback()));
                qv.setSuggestions(parseSuggestions(answer.getFeedback()));
            }
            
            qv.setTeacherComment(answer.getTeacherFeedback());
            
            questions.add(qv);
        }
        
        return questions;
    }

    private List<String> parseErrorTags(String feedback) {
        List<String> tags = new ArrayList<>();
        if (feedback == null || feedback.isEmpty()) {
            return tags;
        }
        
        if (feedback.contains("概念混淆")) tags.add("概念混淆");
        if (feedback.contains("逻辑跳跃")) tags.add("逻辑跳跃");
        if (feedback.contains("表述不清")) tags.add("表述不清");
        if (feedback.contains("论据不足")) tags.add("论据不足");
        if (feedback.contains("理解偏差")) tags.add("理解偏差");
        
        if (tags.isEmpty() && feedback.length() > 10) {
            tags.add("需要改进");
        }
        
        return tags;
    }

    private List<String> parseSuggestions(String feedback) {
        List<String> suggestions = new ArrayList<>();
        if (feedback == null || feedback.isEmpty()) {
            return suggestions;
        }
        
        String[] lines = feedback.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("建议") || line.startsWith("-") || line.startsWith("•") || line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.")) {
                suggestions.add(line.replaceAll("^[\\-•\\d\\.]+\\s*", ""));
            }
        }
        
        if (suggestions.isEmpty() && feedback.length() > 20) {
            suggestions.add(feedback);
        }
        
        return suggestions;
    }
}
