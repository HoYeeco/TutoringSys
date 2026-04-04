package com.tutoring.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoring.dto.ErrorBookListVO;
import com.tutoring.dto.ErrorBookStatVO;
import com.tutoring.entity.*;
import com.tutoring.exception.BusinessException;
import com.tutoring.mapper.*;
import com.tutoring.service.StudentErrorBookService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentErrorBookServiceImpl implements StudentErrorBookService {

    private final ErrorBookMapper errorBookMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final QuestionMapper questionMapper;
    private final AssignmentMapper assignmentMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;

    @Override
    @Transactional
    public void addToErrorBook(Long studentId, Long questionId, Long assignmentId) {
        StudentAnswer studentAnswer = studentAnswerMapper.selectOne(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getStudentId, studentId)
                .eq(StudentAnswer::getQuestionId, questionId)
                .eq(StudentAnswer::getAssignmentId, assignmentId)
                .eq(StudentAnswer::getIsDraft, 0)
        );

        if (studentAnswer == null) {
            throw new BusinessException("未找到对应的答题记录");
        }

        long exists = errorBookMapper.selectCount(
            new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getStudentId, studentId)
                .eq(ErrorBook::getStudentAnswerId, studentAnswer.getId())
                .eq(ErrorBook::getStatus, 1)
        );
        if (exists > 0) {
            throw new BusinessException("该题已在错题本中");
        }

        ErrorBook errorBook = new ErrorBook();
        errorBook.setStudentId(studentId);
        errorBook.setStudentAnswerId(studentAnswer.getId());
        errorBook.setStatus(1);
        errorBookMapper.insert(errorBook);
    }

    @Override
    @Transactional
    public Map<String, Object> batchAddToErrorBook(Long studentId, List<Map<String, Long>> questions) {
        int added = 0;
        int skipped = 0;

        for (Map<String, Long> item : questions) {
            Object qidObj = item.get("questionId");
            Object aidObj = item.get("assignmentId");

            if (qidObj == null || aidObj == null) continue;
            Long questionId = ((Number) qidObj).longValue();
            Long itemAssignmentId = ((Number) aidObj).longValue();

            try {
                StudentAnswer studentAnswer = studentAnswerMapper.selectOne(
                    new LambdaQueryWrapper<StudentAnswer>()
                        .eq(StudentAnswer::getStudentId, studentId)
                        .eq(StudentAnswer::getQuestionId, questionId)
                        .eq(StudentAnswer::getAssignmentId, itemAssignmentId)
                        .eq(StudentAnswer::getIsDraft, 0)
                );

                if (studentAnswer == null) {
                    skipped++;
                    continue;
                }

                long exists = errorBookMapper.selectCount(
                    new LambdaQueryWrapper<ErrorBook>()
                        .eq(ErrorBook::getStudentId, studentId)
                        .eq(ErrorBook::getStudentAnswerId, studentAnswer.getId())
                        .eq(ErrorBook::getStatus, 1)
                );

                if (exists > 0) {
                    skipped++;
                    continue;
                }

                ErrorBook errorBook = new ErrorBook();
                errorBook.setStudentId(studentId);
                errorBook.setStudentAnswerId(studentAnswer.getId());
                errorBook.setStatus(1);
                errorBookMapper.insert(errorBook);
                added++;
            } catch (Exception e) {
                skipped++;
            }
        }

        return Map.of("added", added, "skipped", skipped);
    }

    @Override
    public List<ErrorBookStatVO> getErrorBookStats(Long studentId) {
        List<CourseSelection> selections = courseSelectionMapper.selectList(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
        );

        if (selections.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> courseIds = selections.stream()
            .map(CourseSelection::getCourseId)
            .collect(Collectors.toList());

        List<ErrorBook> errorBooks = errorBookMapper.selectList(
            new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getStudentId, studentId)
                .eq(ErrorBook::getStatus, 1)
        );

        if (errorBooks.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> studentAnswerIds = errorBooks.stream()
            .map(ErrorBook::getStudentAnswerId)
            .collect(Collectors.toList());

        List<StudentAnswer> studentAnswers = studentAnswerMapper.selectBatchIds(studentAnswerIds);
        
        Map<Long, StudentAnswer> answerMap = studentAnswers.stream()
            .collect(Collectors.toMap(StudentAnswer::getId, Function.identity()));

        List<Long> assignmentIds = studentAnswers.stream()
            .map(StudentAnswer::getAssignmentId)
            .distinct()
            .collect(Collectors.toList());

        Map<Long, Assignment> assignmentMap = assignmentMapper.selectBatchIds(assignmentIds)
            .stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds)
            .stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, Long> courseErrorCount = new HashMap<>();
        for (ErrorBook eb : errorBooks) {
            StudentAnswer sa = answerMap.get(eb.getStudentAnswerId());
            if (sa != null) {
                Assignment assignment = assignmentMap.get(sa.getAssignmentId());
                if (assignment != null && courseIds.contains(assignment.getCourseId())) {
                    courseErrorCount.merge(assignment.getCourseId(), 1L, Long::sum);
                }
            }
        }

        return courseErrorCount.entrySet().stream()
            .map(entry -> {
                Course course = courseMap.get(entry.getKey());
                return ErrorBookStatVO.builder()
                    .courseId(entry.getKey())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .errorCount(entry.getValue())
                    .build();
            })
            .sorted(Comparator.comparing(ErrorBookStatVO::getErrorCount).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public Page<ErrorBookListVO> getErrorBookList(Long studentId, Integer page, Integer size,
            Long courseId, String type, String keyword) {

        List<ErrorBook> errorBooks = errorBookMapper.selectList(
            new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getStudentId, studentId)
                .eq(ErrorBook::getStatus, 1)
                .orderByDesc(ErrorBook::getCreateTime)
        );

        if (errorBooks.isEmpty()) {
            return new Page<>(page, size, 0);
        }

        List<Long> studentAnswerIds = errorBooks.stream()
            .map(ErrorBook::getStudentAnswerId)
            .collect(Collectors.toList());

        List<StudentAnswer> studentAnswers = studentAnswerMapper.selectBatchIds(studentAnswerIds);
        Map<Long, StudentAnswer> answerMap = studentAnswers.stream()
            .collect(Collectors.toMap(StudentAnswer::getId, Function.identity()));

        List<Long> questionIds = studentAnswers.stream()
            .map(StudentAnswer::getQuestionId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Question> questionMap = questionMapper.selectBatchIds(questionIds)
            .stream()
            .collect(Collectors.toMap(Question::getId, Function.identity()));

        List<Long> assignmentIds = studentAnswers.stream()
            .map(StudentAnswer::getAssignmentId)
            .distinct()
            .collect(Collectors.toList());
        Map<Long, Assignment> assignmentMap = assignmentMapper.selectBatchIds(assignmentIds)
            .stream()
            .collect(Collectors.toMap(Assignment::getId, Function.identity()));

        Set<Long> courseIds = assignmentMap.values().stream()
            .map(Assignment::getCourseId)
            .collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds)
            .stream()
            .collect(Collectors.toMap(Course::getId, Function.identity()));

        Map<Long, ErrorBook> errorBookMap = errorBooks.stream()
            .collect(Collectors.toMap(ErrorBook::getId, Function.identity()));

        List<ErrorBookListVO> allVOs = errorBooks.stream()
            .map(eb -> {
                StudentAnswer sa = answerMap.get(eb.getStudentAnswerId());
                if (sa == null) return null;

                Question question = questionMap.get(sa.getQuestionId());
                if (question == null) return null;

                Assignment assignment = assignmentMap.get(sa.getAssignmentId());
                if (assignment == null) return null;

                Course course = courseMap.get(assignment.getCourseId());

                return ErrorBookListVO.builder()
                    .id(eb.getId())
                    .questionId(question.getId())
                    .type(question.getType())
                    .content(question.getContent())
                    .options(question.getOptions())
                    .studentAnswer(sa.getAnswer())
                    .correctAnswer(question.getAnswer())
                    .analysis(question.getAnalysis())
                    .courseId(assignment.getCourseId())
                    .courseName(course != null ? course.getName() : "未知课程")
                    .assignmentId(assignment.getId())
                    .assignmentTitle(assignment.getTitle())
                    .createTime(eb.getCreateTime())
                    .build();
            })
            .filter(Objects::nonNull)
            .filter(vo -> {
                if (courseId != null && !courseId.equals(vo.getCourseId())) {
                    return false;
                }
                if (StringUtils.hasText(type) && !type.equals(vo.getType())) {
                    return false;
                }
                if (StringUtils.hasText(keyword)) {
                    return vo.getContent().contains(keyword) ||
                           vo.getAssignmentTitle().contains(keyword) ||
                           vo.getCourseName().contains(keyword);
                }
                return true;
            })
            .collect(Collectors.toList());

        long total = allVOs.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, allVOs.size());

        List<ErrorBookListVO> pageData = fromIndex < allVOs.size()
            ? allVOs.subList(fromIndex, toIndex)
            : new ArrayList<>();

        Page<ErrorBookListVO> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(pageData);
        return resultPage;
    }

    @Override
    @Transactional
    public void removeFromErrorBook(Long studentId, Long errorBookId) {
        ErrorBook errorBook = errorBookMapper.selectById(errorBookId);
        if (errorBook == null) {
            throw new BusinessException("错题记录不存在");
        }

        if (!errorBook.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作该记录");
        }

        errorBook.setStatus(0);
        errorBookMapper.updateById(errorBook);
    }

    @Override
    public void exportErrorBook(Long studentId, Long courseId, String type, String format,
            HttpServletResponse response) {

        Page<ErrorBookListVO> page = getErrorBookList(studentId, 1, Integer.MAX_VALUE, courseId, type, null);
        List<ErrorBookListVO> list = page.getRecords();

        if ("excel".equalsIgnoreCase(format)) {
            exportToExcel(list, response);
        } else {
            throw new BusinessException("暂不支持的导出格式: " + format);
        }
    }

    private void exportToExcel(List<ErrorBookListVO> list, HttpServletResponse response) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("错题本");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"序号", "课程", "作业", "题型", "题目内容", "我的答案", "正确答案", "解析"};
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
            for (ErrorBookListVO vo : list) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(vo.getCourseName());
                row.createCell(2).setCellValue(vo.getAssignmentTitle());
                row.createCell(3).setCellValue(getTypeName(vo.getType()));
                row.createCell(4).setCellValue(vo.getContent());
                row.createCell(5).setCellValue(vo.getStudentAnswer() != null ? vo.getStudentAnswer() : "");
                row.createCell(6).setCellValue(vo.getCorrectAnswer() != null ? vo.getCorrectAnswer() : "");
                row.createCell(7).setCellValue(vo.getAnalysis() != null ? vo.getAnalysis() : "");
                rowNum++;
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("错题本.xlsx", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");

            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            throw new BusinessException("导出Excel失败: " + e.getMessage());
        }
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

    @Override
    public Set<Long> checkInErrorBook(Long studentId, Long assignmentId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Set.of();
        }
        List<StudentAnswer> answers = studentAnswerMapper.selectList(
            new LambdaQueryWrapper<StudentAnswer>()
                .eq(StudentAnswer::getStudentId, studentId)
                .eq(StudentAnswer::getAssignmentId, assignmentId)
                .in(StudentAnswer::getQuestionId, questionIds)
                .eq(StudentAnswer::getIsDraft, 0)
                .select(StudentAnswer::getId, StudentAnswer::getQuestionId)
        );
        if (answers.isEmpty()) {
            return Set.of();
        }
        List<Long> answerIds = answers.stream().map(StudentAnswer::getId).toList();
        List<ErrorBook> errorBooks = errorBookMapper.selectList(
            new LambdaQueryWrapper<ErrorBook>()
                .eq(ErrorBook::getStudentId, studentId)
                .in(ErrorBook::getStudentAnswerId, answerIds)
                .eq(ErrorBook::getStatus, 1)
                .select(ErrorBook::getStudentAnswerId)
        );
        Set<Long> existingAnswerIds = errorBooks.stream()
            .map(ErrorBook::getStudentAnswerId)
            .collect(Collectors.toSet());
        Map<Long, Long> answerToQuestion = answers.stream()
            .collect(Collectors.toMap(StudentAnswer::getId, StudentAnswer::getQuestionId));
        return existingAnswerIds.stream()
            .map(answerToQuestion::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

}
