import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { AuditlogService } from './auditlog.service';
import { AuditLogType } from '../models/auditlog-type';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

describe('AuditlogService', () => {
  let service: AuditlogService;
  let httpMock: HttpTestingController;
  let notificationSpy: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  const mockLog: AuditLogType = {
    id: 1,
    actor: 'admin@example.com',
    action: 'CREATE',
    entity: 'Release',
    entityId: 42,
    payload: '{"version":"V1.0"}',
    timestamp: '2026-01-01T12:00:00',
  };

  beforeEach(() => {
    notificationSpy = { showSuccess: vi.fn(), showError: vi.fn() };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuditlogService,
        { provide: NotificationService, useValue: notificationSpy },
      ],
    });

    service = TestBed.inject(AuditlogService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve expor items como signal readonly vazio inicialmente', () => {
    expect(service.items()).toEqual([]);
  });

  it('deve buscar todos os logs de auditoria (getAll) e atualizar a signal', () => {
    const mockList = [mockLog];

    service.getAll();

    const req = httpMock.expectOne(environment.auditApi);
    expect(req.request.method).toBe('GET');
    req.flush(mockList);

    expect(service.items()).toEqual(mockList);
    expect(notificationSpy.showSuccess).toHaveBeenCalled();
  });

  it('deve chamar showError se getAll falhar', () => {
    service.getAll();

    const req = httpMock.expectOne(environment.auditApi);
    req.flush(
      { message: 'Erro interno' },
      { status: 500, statusText: 'Server Error' },
    );

    expect(notificationSpy.showError).toHaveBeenCalled();
  });

  it('deve buscar logs por ator via searchByActor', async () => {
    const resultPromise = firstValueFrom(service.searchByActor('admin@example.com'));

    const req = httpMock.expectOne(
      `${environment.auditApi}/actor?ator=admin%40example.com`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockLog]);

    const result = await resultPromise;
    expect(result).toEqual([mockLog]);
  });

  it('deve propagar erro em searchByActor via showError', async () => {
    const resultPromise = firstValueFrom(service.searchByActor('ninguem'));

    const req = httpMock.expectOne(
      `${environment.auditApi}/actor?ator=ninguem`,
    );
    req.flush(
      { message: 'Não encontrado' },
      { status: 404, statusText: 'Not Found' },
    );

    await expect(resultPromise).rejects.toThrow();
    expect(notificationSpy.showError).toHaveBeenCalled();
  });
});
