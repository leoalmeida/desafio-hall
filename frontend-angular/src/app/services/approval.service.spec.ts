import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { ApprovalService } from './approval.service';
import { ApprovalType } from '../models/approval-type';
import { NotificationService } from './notification.service';
import { environment } from '../../environments/environment';
import { firstValueFrom } from 'rxjs';

describe('ApprovalService', () => {
  let service: ApprovalService;
  let httpMock: HttpTestingController;
  let notificationSpy: {
    showSuccess: ReturnType<typeof vi.fn>;
    showError: ReturnType<typeof vi.fn>;
  };

  const mockApproval: ApprovalType = {
    id: 1,
    releaseId: 10,
    approverEmail: 'approver@example.com',
    outcome: 'APPROVED',
    notes: 'Aprovado sem ressalvas',
    timestamp: '2026-01-01T12:00:00',
  };

  beforeEach(() => {
    notificationSpy = { showSuccess: vi.fn(), showError: vi.fn() };

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ApprovalService,
        { provide: NotificationService, useValue: notificationSpy },
      ],
    });

    service = TestBed.inject(ApprovalService);
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

  it('deve buscar todas as aprovações (getAll) e atualizar a signal', () => {
    const mockList = [mockApproval];

    service.getAll();

    const req = httpMock.expectOne(environment.approvalsApi);
    expect(req.request.method).toBe('GET');
    req.flush(mockList);

    expect(service.items()).toEqual(mockList);
    expect(notificationSpy.showSuccess).toHaveBeenCalled();
  });

  it('deve chamar showError se getAll falhar', () => {
    service.getAll();

    const req = httpMock.expectOne(environment.approvalsApi);
    req.flush(
      { message: 'Erro interno' },
      { status: 500, statusText: 'Server Error' },
    );

    expect(notificationSpy.showError).toHaveBeenCalled();
  });

  it('deve buscar aprovações por approverEmail via searchApprovals', async () => {
    const resultPromise = firstValueFrom(
      service.searchApprovals('approver@example.com', '', ''),
    );

    const req = httpMock.expectOne(
      `${environment.approvalsApi}/approver?aprovador=approver%40example.com`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockApproval]);

    const result = await resultPromise;
    expect(result).toEqual([mockApproval]);
  });

  it('deve buscar aprovações por releaseId via searchApprovals', async () => {
    const resultPromise = firstValueFrom(
      service.searchApprovals('', '10', ''),
    );

    const req = httpMock.expectOne(
      `${environment.approvalsApi}/release?releaseId=10`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockApproval]);

    const result = await resultPromise;
    expect(result).toEqual([mockApproval]);
  });

  it('deve buscar aprovações por outcome via searchApprovals', async () => {
    const resultPromise = firstValueFrom(
      service.searchApprovals('', '', 'APPROVED'),
    );

    const req = httpMock.expectOne(
      `${environment.approvalsApi}/outcome?outcome=APPROVED`,
    );
    expect(req.request.method).toBe('GET');
    req.flush([mockApproval]);

    const result = await resultPromise;
    expect(result).toEqual([mockApproval]);
  });

  it('deve usar endpoint base se searchApprovals for chamado sem filtros', async () => {
    const resultPromise = firstValueFrom(service.searchApprovals('', '', ''));

    const req = httpMock.expectOne(environment.approvalsApi);
    expect(req.request.method).toBe('GET');
    req.flush([mockApproval]);

    const result = await resultPromise;
    expect(result).toEqual([mockApproval]);
  });

  it('deve propagar erro em searchApprovals via showError', async () => {
    const resultPromise = firstValueFrom(
      service.searchApprovals('', '', 'INVALID'),
    );

    const req = httpMock.expectOne(
      `${environment.approvalsApi}/outcome?outcome=INVALID`,
    );
    req.flush(
      { message: 'Erro' },
      { status: 400, statusText: 'Bad Request' },
    );

    await expect(resultPromise).rejects.toThrow();
    expect(notificationSpy.showError).toHaveBeenCalled();
  });
});
