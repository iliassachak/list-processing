package com.iachak.listprocessing.dto;

public record CellUpdateRequest(
        String columnName,
        Object value
) {
}
