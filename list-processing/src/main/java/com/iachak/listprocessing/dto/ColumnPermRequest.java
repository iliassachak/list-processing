package com.iachak.listprocessing.dto;

import java.util.UUID;

public record ColumnPermRequest(
        UUID userId,
        boolean canEdit,
        boolean canView
) {
}
