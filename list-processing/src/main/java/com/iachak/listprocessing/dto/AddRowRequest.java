package com.iachak.listprocessing.dto;

import java.util.Map;
import java.util.UUID;

public record AddRowRequest(
        Map<String,Object> data,
        UUID assignToUserId
) {
}
