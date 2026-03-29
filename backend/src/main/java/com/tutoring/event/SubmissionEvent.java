package com.tutoring.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SubmissionEvent extends ApplicationEvent {

    private final Long submissionId;
    private final Long studentId;
    private final Long assignmentId;

    public SubmissionEvent(Long submissionId, Long studentId, Long assignmentId) {
        super(submissionId);
        this.submissionId = submissionId;
        this.studentId = studentId;
        this.assignmentId = assignmentId;
    }
}
