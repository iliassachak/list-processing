package com.iachak.listprocessing.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record WsEvent(
        String type,       // CELL_UPDATED | ROW_ADDED | ROW_DELETED | ASSIGNMENT_CHANGED
        UUID rowId,
        Integer rowIndex,
        String columnName,
        Object value,
        Map<String,Object> data,
        String by,
        LocalDateTime at
) {
    public static WsEvent cellUpdated(UUID rowId,int idx,String col,Object val,String by){
        return new WsEvent("CELL_UPDATED",rowId,idx,col,val,null,by,LocalDateTime.now());
    }
    public static WsEvent rowAdded(UUID rowId, int idx, Map<String,Object> data, String by){
        return new WsEvent("ROW_ADDED",rowId,idx,null,null,data,by, LocalDateTime.now());
    }
    public static WsEvent rowDeleted(UUID rowId,int idx,String by){
        return new WsEvent("ROW_DELETED",rowId,idx,null,null,null,by,LocalDateTime.now());
    }
}
