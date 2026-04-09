package com.iachak.listprocessing.dto;

import com.iachak.listprocessing.entity.ListRow;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record RowDTO(
        UUID id, Integer rowIndex,
        Map<String,Object> data,
        LocalDateTime lastModifiedAt,
        String lastModifiedBy
) {
    public static RowDTO from(ListRow r){
        return new RowDTO(
                r.getId(),
                r.getRowIndex(),
                r.getData(),
                r.getLastModifiedAt(),
                r.getLastModifiedBy()!=null?r.getLastModifiedBy().getUsername():null);
    }
}
