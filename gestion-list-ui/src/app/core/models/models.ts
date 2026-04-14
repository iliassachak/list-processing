export interface User {
  id: string;
  username: string;
  email: string;
  roles: string[];
}

export interface AuthResponse {
  token: string;
  username: string;
  roles: string[];
  userId: string;
}

export interface Column {
  id: string;
  name: string;
  index: number;
  type: string;
}

export interface ListMeta {
  id: string;
  name: string;
  description: string;
  totalRows: number;
  createdAt: string;
  createdBy: string;
  columns: Column[];
}

export interface Row {
  id: string;
  rowIndex: number;
  data: Record<string, any>;
  lastModifiedAt: string;
  lastModifiedBy: string;
}

export interface Assignment {
  id: string;
  userId: string;
  username: string;
  startRow: number;
  endRow: number;
}

export interface WsEvent {
  type: 'CELL_UPDATED' | 'ROW_ADDED' | 'ROW_DELETED' | 'ASSIGNMENT_CHANGED';
  rowId: string;
  rowIndex: number;
  columnName: string;
  value: any;
  data: Record<string, any>;
  by: string;
  at: string;
}

export interface WsGlobalEvent {
  type: 'LIST_ADDED' | 'LIST_DELETED' | 'ASSIGNMENT_CHANGED' | 'USER_REGISTERED' | 'PERMISSION_CHANGED';
  listId: string;
  by: string;
  at: string;
  editableCols: string[];
}
