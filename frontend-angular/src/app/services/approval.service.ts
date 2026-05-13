import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApprovalType } from '../models/approval-type';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class ApprovalService {
  private baseUrl = '/api/approvals';
  private approvalsList = signal<ApprovalType[]>([]);

  private http: HttpClient = inject(HttpClient);
  private notify = inject(NotificationService);

  constructor() {
    this.baseUrl = environment.approvalsApi;
  }

  items = this.approvalsList.asReadonly();

  getAll(): void {
    this.http.get<ApprovalType[]>(`${this.baseUrl}`).subscribe({
      next: (lista: ApprovalType[]) => {
        this.notify.showSuccess(
          `Aprovações carregadas com sucesso. Total de registros: ${lista.length}.`,
        );
        this.approvalsList.set(lista);
      },
      error: (error) => this.handleError(error),
    });
  }

  searchApprovals(
    approverEmail: string,
    releaseId: string,
    outcome: string,
  ): Observable<ApprovalType[]> {
    let endpoint = `${this.baseUrl}`;
    let params = {};

    if (approverEmail) {
      endpoint = `${this.baseUrl}/approver`;
      params = { aprovador: approverEmail };
    } else if (releaseId) {
      endpoint = `${this.baseUrl}/release`;
      params = { releaseId };
    } else if (outcome) {
      endpoint = `${this.baseUrl}/outcome`;
      params = { outcome };
    }

    return this.http
      .get<ApprovalType[]>(endpoint, { params })
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let msg = 'Falha ao processar solicitação de aprovações.';

    if (error.error instanceof ErrorEvent) {
      msg = `Erro no cliente: ${error.error.message}`;
    } else {
      msg = `Erro ${error.status}: ${error.message || 'Erro desconhecido do servidor.'}`;
    }

    this.notify.showError(msg);
    return throwError(() => new Error(msg));
  }
}
