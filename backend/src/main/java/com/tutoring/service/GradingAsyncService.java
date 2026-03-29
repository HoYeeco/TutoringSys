package com.tutoring.service;

public interface GradingAsyncService {

    void processGrading(Long submissionId, Long studentId, Long assignmentId);
}
