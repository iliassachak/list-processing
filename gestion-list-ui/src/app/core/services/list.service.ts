import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Assignment, ListMeta, Row} from '../models/models';

const API = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root',
})
export class ListService {
  private http = inject(HttpClient);

  getLists() {
    return this.http.get<ListMeta[]>(`${API}/lists`);
  }

  getList(id: string) {
    return this.http.get<ListMeta>(`${API}/lists/${id}`);
  }

  deleteList(listId: string) {
    return this.http.delete(`${API}/lists/${listId}`);
  }

  getRows(id: string) {
    return this.http.get<Row[]>(`${API}/lists/${id}/rows`);
  }

  getEditableCols(id: string) {
    return this.http.get<string[]>(`${API}/lists/${id}/editable-columns`);
  }

  uploadList(file: File, name: string) {
    const form = new FormData();
    form.append('file', file);
    form.append('name', name);
    return this.http.post<ListMeta>(`${API}/lists`, form);
  }

  updateCell(listId: string, rowId: string, columnName: string, value: any) {
    return this.http.patch<Row>(`${API}/lists/${listId}/rows/${rowId}/cell`, {columnName, value});
  }

  addRow(listId: string, data: Record<string, any>, assignToUserId: string | null) {
    return this.http.post<Row>(`${API}/lists/${listId}/rows`, {data, assignToUserId});
  }

  deleteRow(listId: string, rowId: string) {
    return this.http.delete(`${API}/lists/${listId}/rows/${rowId}`);
  }

  downloadList(listId: string, name: string) {
    return this.http.get(`${API}/lists/${listId}/download`, {responseType: 'blob'});
  }

  // Admin
  getUsers() {
    return this.http.get<any[]>(`${API}/admin/users`);
  }

  getAssignments(listId: string) {
    return this.http.get<Assignment[]>(`${API}/admin/lists/${listId}/assignments`);
  }

  assign(listId: string, userId: string, start: number, end: number) {
    return this.http.post<Assignment>(`${API}/admin/lists/${listId}/assignments`, {
      userId,
      startRow: start,
      endRow: end
    });
  }

  deleteAssignment(listId: string, id: string) {
    return this.http.delete(`${API}/admin/lists/${listId}/assignments/${id}`);
  }

  getPermissions(listId: string) {
    return this.http.get<Record<string, Record<string, boolean>>>(`${API}/admin/lists/${listId}/permissions`);
  }

  setPermission(listId: string, col: string, userId: string, canEdit: boolean, canView: boolean) {
    return this.http.post(`${API}/admin/lists/${listId}/permissions/${col}`, {userId, canEdit, canView});
  }
}
