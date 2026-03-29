package com.tutoring.event;

import com.tutoring.service.GradingAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionEventListener {

    private final GradingAsyncService gradingAsyncService;

    @Async("taskExecutor")
    @EventListener
    public void handleSubmissionEvent(SubmissionEvent event) {
        log.info("收到作业提交事件，submissionId: {}, studentId: {}, assignmentId: {}, 线程: {}", 
            event.getSubmissionId(), event.getStudentId(), event.getAssignmentId(), Thread.currentThread().getName());

        gradingAsyncService.processGrading(
            event.getSubmissionId(), 
            event.getStudentId(), 
            event.getAssignmentId()
        );
    }
}
