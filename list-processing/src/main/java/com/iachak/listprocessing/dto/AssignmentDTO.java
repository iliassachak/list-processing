package com.iachak.listprocessing.dto;

import com.iachak.listprocessing.entity.RowAssignment;

import java.util.UUID;

public record AssignmentDTO(
        UUID id,
        UUID userId,
        String username,
        Integer startRow,
        Integer endRow
) {
    public static AssignmentDTO from(RowAssignment ra) {
        return new AssignmentDTO(
                ra.getId(),
                ra.getUser().getId(),
                ra.getUser().getUsername(),
                ra.getStartRow(),
                ra.getEndRow());
    }
}
