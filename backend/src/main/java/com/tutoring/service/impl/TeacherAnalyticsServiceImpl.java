package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.*;
import com.tutoring.entity.*;
import com.tutoring.mapper.*;
import com.tutoring.service.TeacherAnalyticsService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherAnalyticsServiceImpl implements TeacherAnalyticsService {

    private final CourseMapper courseMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final SubmissionMapper submissionMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionMapper questionMapper;
    private final UserMapper userMapper;

    @Override
    public OverviewStatsVO getOverviewStats(Long teacherId) {
        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        if (courses.isEmpty()) {
            return OverviewStatsVO.builder()
                .totalCourses(0)
                .totalAssignments(0)
                .totalStudents(0)
                .totalSubmissions(0)
                .completionRate(0.0)
                .averageScore(0.0)
                .excellentRate(0.0)
                .overdueRate(0.0)
                .overdueCount(0)
                .scoreDistribution(new ArrayList<>())
                .courseStats(new ArrayList<>())
                .build();
        }

        List<Long> courseIds = courses.stream()
            .map(Course::getId)
            .collect(Collectors.toList());

        Integer totalCourses = courses.size();

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, courseIds)
                .eq(Assignment::getStatus, "published")
        );

        Integer totalAssignments = assignments.size();

        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .in(CourseSelection::getCourseId, courseIds)
        );

        Integer totalStudents = (int) selections.stream()
            .map(CourseSelection::getStudentId)
            .distinct()
            .count();

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        List<Submission> submissions = assignmentIds.isEmpty() ? new ArrayList<>() :
            submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>()
                    .in(Submission::getAssignmentId, assignmentIds)
            );

        Integer totalSubmissions = submissions.size();

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        double overdueCount = 0;
        for (Submission submission : submissions) {
            Assignment assignment = assignmentMap.get(submission.getAssignmentId());
            if (assignment != null && assignment.getDeadline() != null 
                && submission.getSubmitTime() != null 
                && submission.getSubmitTime().isAfter(assignment.getDeadline())) {
                overdueCount++;
            }
        }

        Double overdueRate = totalSubmissions > 0 
            ? Math.round(overdueCount * 100.0 / totalSubmissions * 100) / 100.0 
            : 0.0;

        Integer totalPossible = assignments.size() * totalStudents;
        Double completionRate = totalPossible > 0 
            ? Math.round(totalSubmissions * 100.0 / totalPossible * 100) / 100.0 
            : 0.0;

        Double averageScore = 0.0;
        Double excellentRate = 0.0;
        List<OverviewStatsVO.ScoreDistribution> scoreDistribution = new ArrayList<>();

        if (!submissions.isEmpty()) {
            List<Double> scores = submissions.stream()
                .filter(s -> s.getFinalTotalScore() != null)
                .map(s -> s.getFinalTotalScore().doubleValue())
                .collect(Collectors.toList());

            if (!scores.isEmpty()) {
                averageScore = Math.round(scores.stream().mapToDouble(Double::doubleValue).average().orElse(0) * 100) / 100.0;

                Integer excellentCount = (int) scores.stream()
                    .filter(s -> s >= 85)
                    .count();
                excellentRate = Math.round(excellentCount * 100.0 / scores.size() * 100) / 100.0;

                Map<String, Integer> distributionMap = new LinkedHashMap<>();
                distributionMap.put("0-59", 0);
                distributionMap.put("60-69", 0);
                distributionMap.put("70-79", 0);
                distributionMap.put("80-89", 0);
                distributionMap.put("90-100", 0);

                for (Double score : scores) {
                    if (score < 60) distributionMap.merge("0-59", 1, Integer::sum);
                    else if (score < 70) distributionMap.merge("60-69", 1, Integer::sum);
                    else if (score < 80) distributionMap.merge("70-79", 1, Integer::sum);
                    else if (score < 90) distributionMap.merge("80-89", 1, Integer::sum);
                    else distributionMap.merge("90-100", 1, Integer::sum);
                }

                scoreDistribution = distributionMap.entrySet().stream()
                    .map(e -> OverviewStatsVO.ScoreDistribution.builder()
                        .range(e.getKey())
                        .count(e.getValue())
                        .build())
                    .collect(Collectors.toList());
            }
        }

        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        List<OverviewStatsVO.CourseStats> courseStats = courses.stream()
            .map(course -> {
                List<Assignment> courseAssignments = assignments.stream()
                    .filter(a -> a.getCourseId().equals(course.getId()))
                    .collect(Collectors.toList());

                Integer assignmentCount = courseAssignments.size();

                Integer studentCount = (int) selections.stream()
                    .filter(s -> s.getCourseId().equals(course.getId()))
                    .map(CourseSelection::getStudentId)
                    .distinct()
                    .count();

                List<Long> courseAssignmentIds = courseAssignments.stream()
                    .map(Assignment::getId)
                    .collect(Collectors.toList());

                List<Submission> courseSubmissions = courseAssignmentIds.isEmpty() ? new ArrayList<>() :
                    submissions.stream()
                        .filter(s -> courseAssignmentIds.contains(s.getAssignmentId()))
                        .collect(Collectors.toList());

                Double avgScore = 0.0;
                if (!courseSubmissions.isEmpty()) {
                    avgScore = courseSubmissions.stream()
                        .filter(s -> s.getFinalTotalScore() != null)
                        .mapToDouble(s -> s.getFinalTotalScore().doubleValue())
                        .average()
                        .orElse(0);
                    avgScore = Math.round(avgScore * 100) / 100.0;
                }

                Integer possible = assignmentCount * studentCount;
                Double completion = possible > 0 
                    ? Math.round(courseSubmissions.size() * 100.0 / possible * 100) / 100.0 
                    : 0.0;

                return OverviewStatsVO.CourseStats.builder()
                    .courseId(course.getId())
                    .courseName(course.getName())
                    .assignmentCount(assignmentCount)
                    .studentCount(studentCount)
                    .averageScore(avgScore)
                    .completionRate(completion)
                    .build();
            })
            .collect(Collectors.toList());

        return OverviewStatsVO.builder()
            .totalCourses(totalCourses)
            .totalAssignments(totalAssignments)
            .totalStudents(totalStudents)
            .totalSubmissions(totalSubmissions)
            .completionRate(completionRate)
            .averageScore(averageScore)
            .excellentRate(excellentRate)
            .overdueRate(overdueRate)
            .overdueCount((int) overdueCount)
            .scoreDistribution(scoreDistribution)
            .courseStats(courseStats)
            .build();
    }

    @Override
    public ErrorAnalysisVO getErrorAnalysis(Long teacherId, Long courseId) {
        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        if (courses.isEmpty()) {
            return ErrorAnalysisVO.builder()
                .errorByType(new ArrayList<>())
                .errorByCourse(new ArrayList<>())
                .errorByAssignment(new ArrayList<>())
                .build();
        }

        List<Long> courseIds = courseId != null 
            ? Collections.singletonList(courseId)
            : courses.stream().map(Course::getId).collect(Collectors.toList());

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, courseIds)
                .eq(Assignment::getStatus, "published")
        );

        if (assignments.isEmpty()) {
            return ErrorAnalysisVO.builder()
                .errorByType(new ArrayList<>())
                .errorByCourse(new ArrayList<>())
                .errorByAssignment(new ArrayList<>())
                .build();
        }

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .in(Question::getAssignmentId, assignmentIds)
        );

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .in(StudentAnswer::getAssignmentId, assignmentIds)
                .eq(StudentAnswer::getIsDraft, 0)
        );

        Map<Long, Question> questionMap = questions.stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<String, Integer> errorByTypeMap = new HashMap<>();
        Map<Long, Integer> errorByCourseMap = new HashMap<>();
        Map<Long, Integer> errorByAssignmentMap = new HashMap<>();

        for (StudentAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;

            boolean isError = answer.getFinalScore() == null || 
                              answer.getFinalScore() < question.getScore();

            if (isError) {
                String typeName = getTypeName(question.getType());
                errorByTypeMap.merge(typeName, 1, Integer::sum);

                Assignment assignment = assignmentMap.get(answer.getAssignmentId());
                if (assignment != null) {
                    errorByCourseMap.merge(assignment.getCourseId(), 1, Integer::sum);
                    errorByAssignmentMap.merge(assignment.getId(), 1, Integer::sum);
                }
            }
        }

        List<ErrorAnalysisVO.ErrorItem> errorByType = errorByTypeMap.entrySet().stream()
            .map(e -> ErrorAnalysisVO.ErrorItem.builder()
                .name(e.getKey())
                .value(e.getValue())
                .build())
            .sorted(Comparator.comparing(ErrorAnalysisVO.ErrorItem::getValue).reversed())
            .collect(Collectors.toList());

        List<ErrorAnalysisVO.ErrorItem> errorByCourse = errorByCourseMap.entrySet().stream()
            .map(e -> {
                Course course = courseMap.get(e.getKey());
                return ErrorAnalysisVO.ErrorItem.builder()
                    .id(e.getKey())
                    .name(course != null ? course.getName() : "未知课程")
                    .value(e.getValue())
                    .build();
            })
            .sorted(Comparator.comparing(ErrorAnalysisVO.ErrorItem::getValue).reversed())
            .collect(Collectors.toList());

        List<ErrorAnalysisVO.ErrorItem> errorByAssignment = errorByAssignmentMap.entrySet().stream()
            .map(e -> {
                Assignment assignment = assignmentMap.get(e.getKey());
                return ErrorAnalysisVO.ErrorItem.builder()
                    .id(e.getKey())
                    .name(assignment != null ? assignment.getTitle() : "未知作业")
                    .value(e.getValue())
                    .build();
            })
            .sorted(Comparator.comparing(ErrorAnalysisVO.ErrorItem::getValue).reversed())
            .limit(10)
            .collect(Collectors.toList());

        return ErrorAnalysisVO.builder()
            .errorByType(errorByType)
            .errorByCourse(errorByCourse)
            .errorByAssignment(errorByAssignment)
            .build();
    }

    @Override
    public Page<FrequentErrorVO> getFrequentErrors(Long teacherId, Integer page, Integer size, Long courseId) {
        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        if (courses.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> courseIds = courseId != null 
            ? Collections.singletonList(courseId)
            : courses.stream().map(Course::getId).collect(Collectors.toList());

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, courseIds)
                .eq(Assignment::getStatus, "published")
        );

        if (assignments.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        List<Question> questions = questionMapper.selectList(
            new LambdaQueryWrapper<Question>()
                .in(Question::getAssignmentId, assignmentIds)
        );

        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .in(StudentAnswer::getAssignmentId, assignmentIds)
                .eq(StudentAnswer::getIsDraft, 0)
        );

        Map<Long, Question> questionMap = questions.stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Map<Long, Course> courseMap = courses.stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Integer> errorCountMap = new HashMap<>();
        Map<Long, Integer> totalCountMap = new HashMap<>();

        for (StudentAnswer answer : answers) {
            Question question = questionMap.get(answer.getQuestionId());
            if (question == null) continue;

            totalCountMap.merge(question.getId(), 1, Integer::sum);

            boolean isError = answer.getFinalScore() == null || 
                              answer.getFinalScore() < question.getScore();

            if (isError) {
                errorCountMap.merge(question.getId(), 1, Integer::sum);
            }
        }

        List<FrequentErrorVO> allErrors = questions.stream()
            .map(q -> {
                Integer errorCount = errorCountMap.getOrDefault(q.getId(), 0);
                Integer totalCount = totalCountMap.getOrDefault(q.getId(), 0);
                Double errorRate = totalCount > 0 
                    ? Math.round(errorCount * 100.0 / totalCount * 100) / 100.0 
                    : 0.0;

                Assignment assignment = assignmentMap.get(q.getAssignmentId());
                Course course = assignment != null ? courseMap.get(assignment.getCourseId()) : null;

                return FrequentErrorVO.builder()
                    .questionId(q.getId())
                    .questionType(q.getType())
                    .questionContent(q.getContent())
                    .assignmentId(q.getAssignmentId())
                    .assignmentTitle(assignment != null ? assignment.getTitle() : "未知作业")
                    .courseId(assignment != null ? assignment.getCourseId() : null)
                    .courseName(course != null ? course.getName() : "未知课程")
                    .errorCount(errorCount)
                    .totalCount(totalCount)
                    .errorRate(errorRate)
                    .build();
            })
            .sorted(Comparator.comparing(FrequentErrorVO::getErrorCount).reversed())
            .collect(Collectors.toList());

        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allErrors.size());

        List<FrequentErrorVO> pageData = fromIndex < allErrors.size()
            ? allErrors.subList(fromIndex, toIndex)
            : new ArrayList<>();

        Page<FrequentErrorVO> resultPage = new Page<>(page, size, allErrors.size());
        resultPage.setRecords(pageData);
        return resultPage;
    }

    @Override
    public void exportFrequentErrors(Long teacherId, Long courseId, HttpServletResponse response) {
        System.out.println("=== DEBUG: exportFrequentErrors called, teacherId=" + teacherId + ", courseId=" + courseId);
        try {
            Page<FrequentErrorVO> page = getFrequentErrors(teacherId, 1, 10000, courseId);
            System.out.println("=== DEBUG: getFrequentErrors completed, records=" + (page != null ? page.getRecords().size() : "null"));
            List<FrequentErrorVO> list = page.getRecords();

            if (list == null) {
                System.out.println("=== DEBUG: list is null, creating empty list");
                list = new ArrayList<>();
            }

            System.out.println("=== DEBUG: creating workbook");
            Workbook workbook = new XSSFWorkbook();
            try {
                Sheet sheet = workbook.createSheet("高频错题");

                Row headerRow = sheet.createRow(0);
                String[] headers = {"序号", "课程", "作业", "题型", "题目内容", "错误次数", "总答题数", "错误率"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                int rowNum = 1;
                for (FrequentErrorVO vo : list) {
                    Row row = sheet.createRow(rowNum);
                    row.createCell(0).setCellValue(rowNum);
                    row.createCell(1).setCellValue(vo.getCourseName());
                    row.createCell(2).setCellValue(vo.getAssignmentTitle());
                    row.createCell(3).setCellValue(getTypeName(vo.getQuestionType()));
                    row.createCell(4).setCellValue(vo.getQuestionContent());
                    row.createCell(5).setCellValue(vo.getErrorCount());
                    row.createCell(6).setCellValue(vo.getTotalCount());
                    row.createCell(7).setCellValue(vo.getErrorRate() + "%");
                    rowNum++;
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                String fileName = URLEncoder.encode("高频错题分析.xlsx", StandardCharsets.UTF_8);
                response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

                workbook.write(response.getOutputStream());
            } finally {
                if (workbook != null) {
                    workbook.close();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    @Override
    public StudentTrendVO getStudentTrend(Long teacherId, Long studentId) {
        User student = userMapper.selectById(studentId);
        if (student == null) {
            return null;
        }

        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId)
        );

        if (courses.isEmpty()) {
            return StudentTrendVO.builder()
                .studentId(studentId)
                .studentName(student.getRealName())
                .trends(new ArrayList<>())
                .build();
        }

        List<Long> courseIds = courses.stream()
            .map(Course::getId)
            .collect(Collectors.toList());

        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .in(CourseSelection::getCourseId, courseIds)
        );

        if (selections.isEmpty()) {
            return StudentTrendVO.builder()
                .studentId(studentId)
                .studentName(student.getRealName())
                .trends(new ArrayList<>())
                .build();
        }

        List<Long> studentCourseIds = selections.stream()
            .map(CourseSelection::getCourseId)
            .collect(Collectors.toList());

        List<Assignment> assignments = assignmentMapper.selectList(
            new LambdaQueryWrapper<Assignment>()
                .in(Assignment::getCourseId, studentCourseIds)
                .eq(Assignment::getStatus, "published")
                .orderByAsc(Assignment::getDeadline)
        );

        if (assignments.isEmpty()) {
            return StudentTrendVO.builder()
                .studentId(studentId)
                .studentName(student.getRealName())
                .trends(new ArrayList<>())
                .build();
        }

        List<Long> assignmentIds = assignments.stream()
            .map(Assignment::getId)
            .collect(Collectors.toList());

        Map<Long, Assignment> assignmentMap = assignments.stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        List<Submission> allSubmissions = submissionMapper.selectList(
            new LambdaQueryWrapper<Submission>()
                .in(Submission::getAssignmentId, assignmentIds)
                .isNotNull(Submission::getFinalTotalScore)
        );

        Map<Long, Double> classAverageMap = allSubmissions.stream()
            .collect(Collectors.groupingBy(
                Submission::getAssignmentId,
                Collectors.averagingInt(s -> s.getFinalTotalScore() != null ? s.getFinalTotalScore() : 0)
            ));

        List<Submission> studentSubmissions = submissionMapper.selectList(
            new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStudentId, studentId)
                .in(Submission::getAssignmentId, assignmentIds)
        );
        
        Map<Long, Submission> studentSubmissionMap = studentSubmissions.stream()
            .collect(Collectors.toMap(Submission::getAssignmentId, Function.identity(), (a, b) -> a));

        List<StudentTrendVO.ScoreTrend> trends = assignments.stream()
            .map(assignment -> {
                Submission submission = studentSubmissionMap.get(assignment.getId());

                Integer studentScore = submission != null ? submission.getFinalTotalScore() : null;
                Integer assignmentTotalScore = assignment.getTotalScore() != null ? assignment.getTotalScore() : 100;
                Double studentPercentage = studentScore != null 
                    ? Math.round(studentScore * 100.0 / assignmentTotalScore * 100) / 100.0 
                    : null;

                Double classAverage = classAverageMap.getOrDefault(assignment.getId(), 0.0);
                classAverage = Math.round(classAverage * 100) / 100.0;

                return StudentTrendVO.ScoreTrend.builder()
                    .assignmentId(assignment.getId())
                    .assignmentTitle(assignment.getTitle())
                    .studentScore(studentScore)
                    .totalScore(assignmentTotalScore)
                    .studentPercentage(studentPercentage)
                    .classAverage(classAverage)
                    .submitTime(submission != null && submission.getSubmitTime() != null 
                        ? submission.getSubmitTime().toString() : null)
                    .build();
            })
            .collect(Collectors.toList());

        return StudentTrendVO.builder()
            .studentId(studentId)
            .studentName(student.getRealName())
            .trends(trends)
            .build();
    }

    private String getTypeName(String type) {
        return switch (type) {
            case "SINGLE" -> "单选题";
            case "MULTIPLE" -> "多选题";
            case "JUDGE" -> "判断题";
            case "SUBJECTIVE" -> "主观题";
            default -> type;
        };
    }

}
