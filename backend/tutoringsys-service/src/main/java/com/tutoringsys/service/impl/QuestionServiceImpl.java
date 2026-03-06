package com.tutoringsys.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tutoringsys.common.dto.QuestionCreateDto;
import com.tutoringsys.common.dto.QuestionExportVo;
import com.tutoringsys.common.dto.QuestionImportDto;
import com.tutoringsys.common.dto.QuestionUpdateDto;
import com.tutoringsys.common.exception.BusinessException;
import com.tutoringsys.dao.entity.Question;
import com.tutoringsys.dao.mapper.QuestionMapper;
import com.tutoringsys.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QuestionServiceImpl implements QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Override
    public IPage<Question> pageQuestions(int page, int size, Map<String, Object> params) {
        Page<Question> questionPage = new Page<>(page, size);
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();

        // 只查询独立题库的题目（assignmentId为空）
        queryWrapper.isNull(Question::getAssignmentId);

        if (params.containsKey("type")) {
            queryWrapper.eq(Question::getType, params.get("type"));
        }
        if (params.containsKey("content")) {
            queryWrapper.like(Question::getContent, params.get("content"));
        }

        return questionMapper.selectPage(questionPage, queryWrapper);
    }

    @Override
    public Question createQuestion(QuestionCreateDto dto) {
        Question question = new Question();
        question.setType(dto.getType());
        question.setContent(dto.getContent());
        question.setOptions(dto.getOptions());
        question.setAnswer(dto.getAnswer());
        question.setScore(dto.getScore());
        question.setAnalysis(dto.getAnalysis());
        question.setSortOrder(dto.getSortOrder());
        // assignmentId为空，表示独立题库的题目

        questionMapper.insert(question);
        return question;
    }

    @Override
    public Question updateQuestion(Long id, QuestionUpdateDto dto) {
        Question question = getQuestionById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        if (dto.getType() != null) {
            question.setType(dto.getType());
        }
        if (dto.getContent() != null) {
            question.setContent(dto.getContent());
        }
        if (dto.getOptions() != null) {
            question.setOptions(dto.getOptions());
        }
        if (dto.getAnswer() != null) {
            question.setAnswer(dto.getAnswer());
        }
        if (dto.getScore() != null) {
            question.setScore(dto.getScore());
        }
        if (dto.getAnalysis() != null) {
            question.setAnalysis(dto.getAnalysis());
        }
        if (dto.getSortOrder() != null) {
            question.setSortOrder(dto.getSortOrder());
        }

        questionMapper.updateById(question);
        return question;
    }

    @Override
    public void deleteQuestion(Long id) {
        Question question = getQuestionById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        // 直接物理删除，因为题库题目与作业题目是独立的
        questionMapper.deleteById(id);
    }

    @Override
    public Question getQuestionById(Long id) {
        return questionMapper.selectById(id);
    }

    @Override
    public Map<String, Object> importQuestions(MultipartFile file) {
        List<QuestionImportDto> questionList = new ArrayList<>();
        List<String> errorList = new ArrayList<>();
        int successCount = 0;

        try {
            // 读取Excel文件
            EasyExcel.read(file.getInputStream(), QuestionImportDto.class, new com.alibaba.excel.read.listener.ReadListener<QuestionImportDto>() {
                @Override
                public void invoke(QuestionImportDto data, com.alibaba.excel.context.AnalysisContext context) {
                    questionList.add(data);
                }

                @Override
                public void doAfterAllAnalysed(com.alibaba.excel.context.AnalysisContext context) {
                }
            }).sheet().doRead();

            // 处理导入的数据
            for (int i = 0; i < questionList.size(); i++) {
                QuestionImportDto dto = questionList.get(i);
                try {
                    Question question = new Question();
                    question.setType(dto.getType());
                    question.setContent(dto.getContent());
                    question.setOptions(dto.getOptions());
                    question.setAnswer(dto.getAnswer());
                    question.setScore(dto.getScore());
                    question.setAnalysis(dto.getAnalysis());
                    // assignmentId为空，表示独立题库的题目

                    questionMapper.insert(question);
                    successCount++;
                } catch (Exception e) {
                    errorList.add("第" + (i + 2) + "行导入失败：" + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new BusinessException("文件读取失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("errorCount", errorList.size());
        result.put("errors", errorList);
        return result;
    }

    @Override
    public void exportQuestions(HttpServletResponse response, Map<String, Object> params) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题库题目", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            // 查询数据
            LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.isNull(Question::getAssignmentId);

            if (params.containsKey("type")) {
                queryWrapper.eq(Question::getType, params.get("type"));
            }
            if (params.containsKey("content")) {
                queryWrapper.like(Question::getContent, params.get("content"));
            }

            List<Question> questions = questionMapper.selectList(queryWrapper);

            // 转换为导出对象
            List<QuestionExportVo> exportList = new ArrayList<>();
            for (Question question : questions) {
                QuestionExportVo vo = new QuestionExportVo();
                vo.setType(question.getType());
                vo.setContent(question.getContent());
                vo.setOptions(question.getOptions());
                vo.setAnswer(question.getAnswer());
                vo.setScore(question.getScore());
                vo.setAnalysis(question.getAnalysis());
                exportList.add(vo);
            }

            // 导出Excel
            EasyExcel.write(response.getOutputStream(), QuestionExportVo.class)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .sheet("题库题目")
                    .doWrite(exportList);
        } catch (IOException e) {
            throw new BusinessException("导出失败：" + e.getMessage());
        }
    }
}
