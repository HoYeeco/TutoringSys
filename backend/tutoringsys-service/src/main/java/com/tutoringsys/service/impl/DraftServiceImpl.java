package com.tutoringsys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tutoringsys.dao.entity.Draft;
import com.tutoringsys.dao.mapper.DraftMapper;
import com.tutoringsys.service.DraftService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
public class DraftServiceImpl implements DraftService {

    @Resource
    private DraftMapper draftMapper;

    @Override
    public boolean saveDraft(Long studentId, Long assignmentId, Map<String, Object> draftData) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) draftData.get("answers");
        
        if (answers != null) {
            for (Map<String, Object> answer : answers) {
                Long questionId = Long.valueOf(answer.get("questionId").toString());
                String answerContent = (String) answer.get("answer");
                
                LambdaQueryWrapper<Draft> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Draft::getStudentId, studentId);
                wrapper.eq(Draft::getAssignmentId, assignmentId);
                wrapper.eq(Draft::getQuestionId, questionId);
                
                Draft existingDraft = draftMapper.selectOne(wrapper);
                
                if (existingDraft != null) {
                    existingDraft.setAnswer(answerContent);
                    existingDraft.setUpdateTime(new Date());
                    draftMapper.updateById(existingDraft);
                } else {
                    Draft draft = new Draft();
                    draft.setStudentId(studentId);
                    draft.setAssignmentId(assignmentId);
                    draft.setQuestionId(questionId);
                    draft.setAnswer(answerContent);
                    draft.setCreateTime(new Date());
                    draft.setUpdateTime(new Date());
                    draftMapper.insert(draft);
                }
            }
        }
        
        return true;
    }

    @Override
    public Map<String, Object> getDraft(Long studentId, Long assignmentId) {
        Map<String, Object> result = new HashMap<>();
        
        LambdaQueryWrapper<Draft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Draft::getStudentId, studentId);
        wrapper.eq(Draft::getAssignmentId, assignmentId);
        List<Draft> drafts = draftMapper.selectList(wrapper);
        
        List<Map<String, Object>> answers = new ArrayList<>();
        for (Draft draft : drafts) {
            Map<String, Object> answer = new HashMap<>();
            answer.put("questionId", draft.getQuestionId());
            answer.put("answer", draft.getAnswer());
            answers.add(answer);
        }
        
        result.put("answers", answers);
        result.put("updateTime", drafts.isEmpty() ? null : drafts.get(0).getUpdateTime());
        
        return result;
    }

    @Override
    public boolean deleteDraft(Long studentId, Long assignmentId) {
        LambdaQueryWrapper<Draft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Draft::getStudentId, studentId);
        wrapper.eq(Draft::getAssignmentId, assignmentId);
        draftMapper.delete(wrapper);
        return true;
    }

    @Override
    public List<Map<String, Object>> getDraftList(Long studentId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        LambdaQueryWrapper<Draft> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Draft::getStudentId, studentId);
        wrapper.orderByDesc(Draft::getUpdateTime);
        List<Draft> drafts = draftMapper.selectList(wrapper);
        
        Map<Long, List<Draft>> groupedByAssignment = new HashMap<>();
        for (Draft draft : drafts) {
            groupedByAssignment.computeIfAbsent(draft.getAssignmentId(), k -> new ArrayList<>()).add(draft);
        }
        
        for (Map.Entry<Long, List<Draft>> entry : groupedByAssignment.entrySet()) {
            Map<String, Object> draftInfo = new HashMap<>();
            draftInfo.put("assignmentId", entry.getKey());
            draftInfo.put("questionCount", entry.getValue().size());
            draftInfo.put("updateTime", entry.getValue().get(0).getUpdateTime());
            result.add(draftInfo);
        }
        
        return result;
    }
}
