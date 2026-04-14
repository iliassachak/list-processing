package com.iachak.listprocessing.dto;

import java.time.LocalDateTime;
import java.util.List;

public record WsGlobalEvent(
        String type,   // LIST_ADDED | LIST_DELETED | ASSIGNMENT_CHANGED | USER_REGISTERED
        String listId, // null pour USER_REGISTERED
        String by,
        LocalDateTime at,
        List<String>editableCols
) {
    public static WsGlobalEvent listAdded(String listId, String by) {
        return new WsGlobalEvent("LIST_ADDED", listId, by, LocalDateTime.now(), null);
    }
    public static WsGlobalEvent listDeleted(String listId, String by) {
        return new WsGlobalEvent("LIST_DELETED", listId, by, LocalDateTime.now(), null);
    }
    public static WsGlobalEvent assignmentChanged(String listId, String by) {
        return new WsGlobalEvent("ASSIGNMENT_CHANGED", listId, by, LocalDateTime.now(), null);
    }
    public static WsGlobalEvent userRegistered(String by) {
        return new WsGlobalEvent("USER_REGISTERED", null, by, LocalDateTime.now(), null);
    }
    public static WsGlobalEvent permissionChanged(String listId, String by, List<String> editableCols) {
        return new WsGlobalEvent("PERMISSION_CHANGED", listId, by, LocalDateTime.now(), editableCols);
    }
}
