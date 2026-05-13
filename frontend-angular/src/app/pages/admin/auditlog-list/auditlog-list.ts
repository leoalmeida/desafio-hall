import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { AuditlogService } from '../../../services/auditlog.service';
import { Searchbar } from '../../../components/searchbar/searchbar';
import { LoadingService } from '../../../components/loading-indicator/loading.service';
import { NotificationService } from 'src/app/services/notification.service';

@Component({
  selector: 'app-auditlog-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTableModule,
    MatPaginatorModule,
    Searchbar,
  ],
  templateUrl: './auditlog-list.html',
  styleUrls: ['./auditlog-list.css'],
})
export class AuditlogList {
  private auditlogService: AuditlogService = inject(AuditlogService);
  private loadingService: LoadingService = inject(LoadingService);
  private notify: NotificationService = inject(NotificationService);

  displayedColumns: string[] = [
    'id',
    'actor',
    'action',
    'entity',
    'entityId',
    'payload',
    'timestamp',
  ];

  searchQuery = signal<string>('');
  actionFilter = signal<string>('');
  entityFilter = signal<string>('');
  pageIndex = signal(0);
  pageSize = signal(10);
  readonly pageSizeOptions = [10, 25, 50];

  get actionFilterValue(): string { return this.actionFilter(); }
  set actionFilterValue(val: string) {
    this.actionFilter.set(val);
    this.pageIndex.set(0);
  }

  get entityFilterValue(): string { return this.entityFilter(); }
  set entityFilterValue(val: string) {
    this.entityFilter.set(val);
    this.pageIndex.set(0);
  }

  constructor() {
    try {
      this.loadingService.loadingOn();
      this.auditlogService.getAll();
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao carregar logs de auditoria.');
    } finally {
      this.loadingService.loadingOff();
    }
  }

  uniqueActions = computed(() =>
    [...new Set(this.auditlogService.items().map((i) => i.action).filter(Boolean))].sort(),
  );

  uniqueEntities = computed(() =>
    [...new Set(this.auditlogService.items().map((i) => i.entity).filter(Boolean))].sort(),
  );

  filteredAuditLogs = computed(() => {
    try {
      const normalizedQuery = this.searchQuery()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
      const action = this.actionFilter();
      const entity = this.entityFilter();

      return this.auditlogService.items().filter((item) => {
        if (action && item.action !== action) return false;
        if (entity && item.entity !== entity) return false;

        if (!normalizedQuery) return true;

        const candidate = [
          item.id,
          item.actor,
          item.action,
          item.entity,
          item.entityId,
          item.payload,
          item.timestamp,
        ]
          .join(' ')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .toLowerCase();

        return candidate.includes(normalizedQuery);
      });
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao filtrar logs de auditoria.');
      return [];
    }
  });

  paginatedAuditLogs = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredAuditLogs().slice(start, start + this.pageSize());
  });

  filteredAuditLogCount = computed(() => this.filteredAuditLogs().length);

  handleMessage(message: string): void {
    this.searchQuery.set(message);
    this.pageIndex.set(0);
  }

  handlePageChange(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  clearFilters(): void {
    this.searchQuery.set('');
    this.actionFilter.set('');
    this.entityFilter.set('');
    this.pageIndex.set(0);
  }
}
