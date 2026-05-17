import { inject, Injectable, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { catchError, map, Observable, throwError } from 'rxjs';
import { environment } from '../../environments/environment';
import { ReleaseType } from '../models/release-type';
import { NotificationService } from './notification.service';
import { EvidenceScoreType } from '../models/evidence-score-type';

export interface LocalReviewNote {
  releaseId: string;
  action: 'APPROVED' | 'REJECTED';
  notes: string;
  actor?: string;
  createdAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class ReleaseService {
  private baseUrl = '/api/releases';
  private readonly localNotesStorageKey = 'release-review-notes';
  private releasesList = signal<ReleaseType[]>([]);

  private http: HttpClient = inject(HttpClient);
  private notify = inject(NotificationService);
  constructor() {
    this.baseUrl = environment.releasesApi;
  }

  items = this.releasesList.asReadonly();

  getAll(): void {
    this.http.get<ReleaseType[]>(`${this.baseUrl}`).subscribe({
      next: (lista: ReleaseType[]) => {
        this.notify.showSuccess(
          `Releases carregados com sucesso. Total de releases: ${lista.length}.`,
        );
        this.releasesList.set(lista);
        return true;
      },
      error: (error) => this.handleError(error),
    });
  }
  getAllAndReturn(): Observable<boolean> {
    return this.http.get<ReleaseType[]>(`${this.baseUrl}`).pipe(
      map((lista: ReleaseType[]) => {
        this.notify.showSuccess(
          `Releases carregados com sucesso. Total de releases: ${lista.length}.`,
        );
        this.releasesList.set(lista);
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  searchReleases(applicationId: string, version: string, env: string, status: string): Observable<ReleaseType[]> {
    return this.http
      .get<ReleaseType[]>(`${this.baseUrl}`, {
        params: {
          applicationId,
          version,
          environment: env,
          status,
        },
      })
      .pipe(catchError((error) => this.handleError(error)));
  }

  //POST - "/"
  createOne(release: ReleaseType): Observable<boolean> {
    this.notify.showSuccess(
      `Solicitando a criação de novo release: versão: ${release.version}.`,
    );
    return this.http.post<ReleaseType>(`${this.baseUrl}`, release).pipe(
      map((added) => {
        if (!added) return false;
        const lista = this.releasesList();
        lista.push(added); // Adiciona o novo item à lista
        this.releasesList.set([...lista]); // Atualiza a signal para refletir as mudanças
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  approveRelease(id: string): Observable<boolean> {
    return this.http.post<void>(`${this.baseUrl}/${id}/approve`, {}).pipe(
      map(() => {
        this.notify.showSuccess(`Release aprovada com sucesso: ID ${id}.`);
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  disapproveRelease(id: string): Observable<boolean> {
    return this.http.post<void>(`${this.baseUrl}/${id}/disapprove`, {}).pipe(
      map(() => {
        this.notify.showSuccess(`Release reprovada com sucesso: ID ${id}.`);
        return true;
      }),
      catchError((error) => this.handleError(error)),
    );
  }

  promoteRelease(id: string, idempotencyKey: string): Observable<boolean> {
    return this.http
      .post<void>(`${this.baseUrl}/${id}/promote`, {}, {
        headers: new HttpHeaders({
          'Idempotency-Key': idempotencyKey,
        }),
      })
      .pipe(
        map(() => {
          this.notify.showSuccess(`Release promovida com sucesso: ID ${id}.`);
          return true;
        }),
        catchError((error) => this.handleError(error)),
      );
  }

  getEvidenceScore(id: string): Observable<EvidenceScoreType> {
    return this.http
      .get<EvidenceScoreType>(`${this.baseUrl}/${id}/evidence-score`)
      .pipe(catchError((error) => this.handleError(error)));
  }

  buildIdempotencyKey(releaseId: string): string {
    if (typeof crypto !== 'undefined' && 'randomUUID' in crypto) {
      return `release-${releaseId}-${crypto.randomUUID()}`;
    }

    return `release-${releaseId}-${Date.now()}`;
  }

  saveLocalReviewNote(note: LocalReviewNote): void {
    const notes = this.readLocalReviewNotes();
    notes.push(note);
    window.sessionStorage.setItem(this.localNotesStorageKey, JSON.stringify(notes));
  }

  getLocalReviewNotes(releaseId: string): LocalReviewNote[] {
    return this.readLocalReviewNotes().filter((item) => item.releaseId === releaseId);
  }

  private readLocalReviewNotes(): LocalReviewNote[] {
    const raw = window.sessionStorage.getItem(this.localNotesStorageKey);
    if (!raw) {
      return [];
    }

    try {
      return JSON.parse(raw) as LocalReviewNote[];
    } catch {
      return [];
    }
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
