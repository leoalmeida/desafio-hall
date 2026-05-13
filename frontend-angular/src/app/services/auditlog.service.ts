import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuditLogType } from '../models/auditlog-type';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class AuditlogService {
  private baseUrl = '/api/audit';
  private auditLogsList = signal<AuditLogType[]>([]);

  private http: HttpClient = inject(HttpClient);
  private notify = inject(NotificationService);

  constructor() {
    this.baseUrl = environment.auditApi;
  }

  items = this.auditLogsList.asReadonly();

  getAll(): void {
    this.http.get<AuditLogType[]>(`${this.baseUrl}`).subscribe({
      next: (lista: AuditLogType[]) => {
        this.notify.showSuccess(
          `Logs de auditoria carregados com sucesso. Total de registros: ${lista.length}.`,
        );
        this.auditLogsList.set(lista);
      },
      error: (error) => this.handleError(error),
    });
  }

  searchByActor(actor: string): Observable<AuditLogType[]> {
    return this.http
      .get<AuditLogType[]>(`${this.baseUrl}/actor`, {
        params: { ator: actor },
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let msg = 'Falha ao processar solicitação de auditoria.';

    if (error.error instanceof ErrorEvent) {
      msg = `Erro no cliente: ${error.error.message}`;
    } else {
      msg = `Erro ${error.status}: ${error.message || 'Erro desconhecido do servidor.'}`;
    }

    this.notify.showError(msg);
    return throwError(() => new Error(msg));
  }
}
