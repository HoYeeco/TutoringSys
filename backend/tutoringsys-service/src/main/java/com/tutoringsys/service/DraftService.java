package com.tutoringsys.service;

import java.util.List;
import java.util.Map;

public interface DraftService {
    boolean saveDraft(Long studentId, Long assignmentId, Map<String, Object> draftData);
    Map<String, Object> getDraft(Long studentId, Long assignmentId);
    boolean deleteDraft(Long studentId, Long assignmentId);
    List<Map<String, Object>> getDraftList(Long studentId);
}
