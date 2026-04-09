package com.iachak.listprocessing.dto;

import java.util.UUID;

public record AssignRowsRequest(
        UUID userId,
        Integer startRow,
        Integer endRow
) {
}
