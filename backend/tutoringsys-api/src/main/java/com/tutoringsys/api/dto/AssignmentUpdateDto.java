package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentUpdateDto {
    private String title;
    private String description;
    private Date deadline;
    private List<AssignmentCreateDto.QuestionDto> questions;
}