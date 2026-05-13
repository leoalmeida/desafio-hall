import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { catchError, map, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ApplicationType } from '../models/application-type';
import { NotificationService } from './notification.service';

@Injectable({
  providedIn: 'root',
})
export class ApplicationService {
  private baseUrl = '/api/applications';
  private applicationsList = signal<ApplicationType[]>([]);

  private http: HttpClient = inject(HttpClient);
  private notify = inject(NotificationService);
  constructor() {
    this.baseUrl = environment.applicationsApi;
  }

  items = this.applicationsList.asReadonly();

  getAll(): void {
    this.http.get<ApplicationType[]>(`${this.baseUrl}`).subscribe({
      next: (lista: ApplicationType[]) => {
        this.notify.showSuccess(
          `Aplicações carregadas com sucesso. Total de aplicações: ${lista.length}.`,
        );
        this.applicationsList.set(lista);
        return true;
      },
      error: (error) => this.handleError(error),
    });
  }
  getAllAndReturn(): Observable<boolean> {
    return this.http.get<ApplicationType[]>(`${this.baseUrl}`).pipe(
      map((lista: ApplicationType[]) => {
        this.notify.showSuccess(
          `Aplicações carregadas com sucesso. Total de aplicações: ${lista.length}.`,
        );
        this.applicationsList.set(lista);
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  //POST - "/"
  createOne(application: ApplicationType): Observable<boolean> {
    this.notify.showSuccess(
      `Solicitando a criação de nova aplicação: nome: ${application.name}.`,
    );
    return this.http.post<ApplicationType>(`${this.baseUrl}`, application).pipe(
      map((added) => {
        if (!added) return false;
        const lista = this.applicationsList();
        lista.push(added); // Adiciona o novo item à lista
        this.applicationsList.set([...lista]); // Atualiza a signal para refletir as mudanças
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'Erro desconhecido';

    if (error.error instanceof ErrorEvent) {
      // Erro do lado do cliente
      errorMessage = `Erro: ${error.error.message}`;
    }
    if (error.error instanceof ProgressEvent) {
      // Erro do lado do cliente
      errorMessage = `Erro: ${error.error}`;
    } else {
      // Erro do lado do servidor
      errorMessage =
        error.error?.message || `Erro ${error.status}: ${error.statusText}`;
    }

    this.notify.showError(`Erro na requisição: ${errorMessage}`);
    return throwError(() => new Error(errorMessage));
  }
}
