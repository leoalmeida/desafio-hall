import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { signal } from '@angular/core';
import { AuditlogList } from './auditlog-list';
import { AuditlogService } from '../../../services/auditlog.service';
import { LoadingService } from '../../core/loading-indicator/loading.service';
import { NotificationService } from '../../../services/notification.service';
import { AuditLogType } from '../../../models/auditlog-type';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('AuditlogList', () => {
  let component: AuditlogList;
  let fixture: ComponentFixture<AuditlogList>;
  let auditlogServiceSpy: SpyObj<AuditlogService, 'getAll'>;
  let loadingServiceSpy: SpyObj<LoadingService>;
  let notificationSpy: SpyObj<NotificationService, 'showSuccess' | 'showError'>;

  const mockLogs: AuditLogType[] = [
    {
      id: 1,
      actor: 'admin@example.com',
      action: 'CREATE',
      entity: 'Release',
      entityId: 10,
      payload: '{}',
      timestamp: '2026-01-01T12:00:00',
    },
    {
      id: 2,
      actor: 'user@example.com',
      action: 'UPDATE',
      entity: 'Application',
      entityId: 5,
      payload: '{"name":"app"}',
      timestamp: '2026-01-02T09:00:00',
    },
    {
      id: 3,
      actor: 'admin@example.com',
      action: 'DELETE',
      entity: 'Release',
      entityId: 20,
      payload: '{}',
      timestamp: '2026-01-03T10:00:00',
    },
  ];

  beforeEach(async () => {
    auditlogServiceSpy = createSpyObj<AuditlogService>(['getAll'], {
      items: signal(mockLogs),
    } as Partial<AuditlogService>);
    loadingServiceSpy = createSpyObj<LoadingService>([
      'loadingOn',
      'loadingOff',
    ]);
    notificationSpy = createSpyObj<NotificationService>([
      'showSuccess',
      'showError',
    ]);

    await TestBed.configureTestingModule({
      imports: [AuditlogList, NoopAnimationsModule],
      providers: [
        { provide: AuditlogService, useValue: auditlogServiceSpy },
        { provide: LoadingService, useValue: loadingServiceSpy },
        { provide: NotificationService, useValue: notificationSpy },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AuditlogList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('deve criar o componente', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar getAll e loadingOn/loadingOff no constructor', () => {
    expect(auditlogServiceSpy.getAll).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOn).toHaveBeenCalled();
    expect(loadingServiceSpy.loadingOff).toHaveBeenCalled();
  });

  it('deve retornar todos os itens quando não há filtro', () => {
    expect(component.filteredAuditLogs().length).toBe(3);
  });

  it('deve filtrar por searchQuery (actor)', () => {
    component.searchQuery.set('user@example.com');
    fixture.detectChanges();

    const filtered = component.filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].actor).toBe('user@example.com');
  });

  it('deve filtrar por action', () => {
    component.actionFilter.set('CREATE');
    fixture.detectChanges();

    const filtered = component.filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].action).toBe('CREATE');
  });

  it('deve filtrar por entity', () => {
    component.entityFilter.set('Application');
    fixture.detectChanges();

    const filtered = component.filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].entity).toBe('Application');
  });

  it('deve combinar filtro de action e entity', () => {
    component.actionFilter.set('DELETE');
    component.entityFilter.set('Release');
    fixture.detectChanges();

    const filtered = component.filteredAuditLogs();
    expect(filtered.length).toBe(1);
    expect(filtered[0].id).toBe(3);
  });

  it('deve calcular uniqueActions corretamente', () => {
    const actions = component.uniqueActions();
    expect(actions).toContain('CREATE');
    expect(actions).toContain('UPDATE');
    expect(actions).toContain('DELETE');
  });

  it('deve calcular uniqueEntities corretamente', () => {
    const entities = component.uniqueEntities();
    expect(entities).toContain('Release');
    expect(entities).toContain('Application');
  });

  it('deve atualizar searchQuery ao chamar handleMessage', () => {
    component.handleMessage('nova busca');
    expect(component.searchQuery()).toBe('nova busca');
  });

  it('deve limpar todos os filtros ao chamar clearFilters', () => {
    component.searchQuery.set('admin');
    component.actionFilter.set('CREATE');
    component.entityFilter.set('Release');

    component.clearFilters();

    expect(component.searchQuery()).toBe('');
    expect(component.actionFilter()).toBe('');
    expect(component.entityFilter()).toBe('');
  });

  it('deve retornar lista vazia quando filtro não encontrar nada', () => {
    component.searchQuery.set('ninguem-xyz');
    fixture.detectChanges();

    expect(component.filteredAuditLogs().length).toBe(0);
  });
});
