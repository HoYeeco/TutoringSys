package com.tutoringsys.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class StudentIdsDto {
    private List<Long> studentIds;
}