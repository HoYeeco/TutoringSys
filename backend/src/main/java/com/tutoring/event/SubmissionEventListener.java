package com.tutoring.event;

import com.tutoring.service.GradingAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionEventListener {

    private final GradingAsyncService gradingAsyncService;

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
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
