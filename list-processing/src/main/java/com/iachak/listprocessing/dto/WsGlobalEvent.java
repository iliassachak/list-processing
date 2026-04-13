package com.iachak.listprocessing.dto;

import java.time.LocalDateTime;

public record WsGlobalEvent(
        String type,   // LIST_ADDED | LIST_DELETED | ASSIGNMENT_CHANGED | USER_REGISTERED
        String listId, // null pour USER_REGISTERED
        String by,
        LocalDateTime at
) {
    public static WsGlobalEvent listAdded(String listId, String by) {
        return new WsGlobalEvent("LIST_ADDED", listId, by, LocalDateTime.now());
    }
    public static WsGlobalEvent listDeleted(String listId, String by) {
        return new WsGlobalEvent("LIST_DELETED", listId, by, LocalDateTime.now());
    }
    public static WsGlobalEvent assignmentChanged(String listId, String by) {
        return new WsGlobalEvent("ASSIGNMENT_CHANGED", listId, by, LocalDateTime.now());
    }
    public static WsGlobalEvent userRegistered(String by) {
        return new WsGlobalEvent("USER_REGISTERED", null, by, LocalDateTime.now());
    }
    public static WsGlobalEvent permissionChanged(String listId, String by) {
        return new WsGlobalEvent("PERMISSION_CHANGED", listId, by, LocalDateTime.now());
    }
}
