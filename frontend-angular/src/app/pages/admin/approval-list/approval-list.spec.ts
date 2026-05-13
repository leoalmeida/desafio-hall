import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { signal } from '@angular/core';
import { ApprovalList } from './approval-list';
import { ApprovalService } from '../../../services/approval.service';
import { LoadingService } from '../../core/loading-indicator/loading.service';
import { NotificationService } from '../../../services/notification.service';
import { ApprovalType } from '../../../models/approval-type';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('ApprovalList', () => {
  let component: ApprovalList;
  let fixture: ComponentFixture<ApprovalList>;
  let approvalServiceSpy: SpyObj<ApprovalService, 'getAll'>;
  let loadingServiceSpy: SpyObj<LoadingService>;
  let notificationSpy: SpyObj<NotificationService, 'showSuccess' | 'showError'>;

  const mockApprovals: ApprovalType[] = [
    {
      id: 1,
      releaseId: 10,
      approverEmail: 'alice@example.com',
      outcome: 'APPROVED',
      notes: 'OK',
      timestamp: '2026-01-01T12:00:00',
    },
    {
      id: 2,
      releaseId: 10,
      approverEmail: 'bob@example.com',
      outcome: 'REJECTED',
      notes: 'Falha na evidência',
      timestamp: '2026-01-02T09:00:00',
    },
    {
      id: 3,
      releaseId: 20,
      approverEmail: 'carol@example.com',
      outcome: 'APPROVED',
      notes: '',
      timestamp: '2026-01-03T10:00:00',
    },
  ];

  beforeEach(async () => {
    approvalServiceSpy = createSpyObj<ApprovalService>(['getAll'], {
      items: signal(mockApprovals),
    } as Partial<ApprovalService>);
    loadingServiceSpy = createSpyObj<LoadingService>([
      'loadingOn',
      'loadingOff',
    ]);
    notificationSpy = createSpyObj<NotificationService>([
      'showSuccess',
      'showError',
    ]);

    await TestBed.configureTestingModule({
      imports: [ApprovalList, NoopAnimationsModule],
      providers: [
        { provide: ApprovalService, useValue: approvalServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ApprovalList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar getAll e loadingOn/loadingOff no constructor', () => {
    expect(approvalServiceSpy.getAll).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOn).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOff).toHaveBeenCalled();
  });

  it('deve retornar todos os itens quando não há filtro', () => {
    expect(component.filteredApprovals().length).toBe(3);
  });

  it('deve filtrar por searchQuery (approverEmail)', () => {
    component.searchQuery.set('alice');
    fixture.detectChanges();

    const filtered = component.filteredApprovals();
    expect(filtered.length).toBe(1);
    expect(filtered[0].approverEmail).toBe('alice@example.com');
  });

  it('deve filtrar por outcome', () => {
    component.outcomeFilter.set('REJECTED');
    fixture.detectChanges();

    const filtered = component.filteredApprovals();
    expect(filtered.length).toBe(1);
    expect(filtered[0].outcome).toBe('REJECTED');
  });

  it('deve combinar filtro de outcome e searchQuery', () => {
    component.outcomeFilter.set('APPROVED');
    component.searchQuery.set('carol');
    fixture.detectChanges();

    const filtered = component.filteredApprovals();
    expect(filtered.length).toBe(1);
    expect(filtered[0].approverEmail).toBe('carol@example.com');
  });

  it('deve atualizar searchQuery ao chamar handleMessage', () => {
    component.handleMessage('nova busca');
    expect(component.searchQuery()).toBe('nova busca');
  });

  it('deve limpar todos os filtros ao chamar clearFilters', () => {
    component.searchQuery.set('alice');
    component.outcomeFilter.set('APPROVED');

    component.clearFilters();

    expect(component.searchQuery()).toBe('');
    expect(component.outcomeFilter()).toBe('');
  });

  it('deve retornar lista vazia quando filtro não encontrar nada', () => {
    component.searchQuery.set('inexistente-xyz');
    fixture.detectChanges();

    expect(component.filteredApprovals().length).toBe(0);
  });
});
