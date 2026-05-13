import { CommonModule } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { ApprovalService } from '../../../services/approval.service';
import { Searchbar } from '../../../components/searchbar/searchbar';
import { LoadingService } from '../../../components/loading-indicator/loading.service';
import { NotificationService } from 'src/app/services/notification.service';

@Component({
  selector: 'app-approval-list',
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
  templateUrl: './approval-list.html',
  styleUrls: ['./approval-list.css'],
})
export class ApprovalList {
  private approvalService: ApprovalService = inject(ApprovalService);
  private loadingService: LoadingService = inject(LoadingService);
  private notify: NotificationService = inject(NotificationService);

  displayedColumns: string[] = [
    'id',
    'releaseId',
    'approverEmail',
    'outcome',
    'notes',
    'timestamp',
  ];

  readonly outcomeOptions = ['APPROVED', 'REJECTED'];

  searchQuery = signal<string>('');
  outcomeFilter = signal<string>('');
  pageIndex = signal(0);
  pageSize = signal(10);
  readonly pageSizeOptions = [10, 25, 50];

  get outcomeFilterValue(): string {
    return this.outcomeFilter();
  }
  set outcomeFilterValue(val: string) {
    this.outcomeFilter.set(val);
    this.pageIndex.set(0);
  }

  constructor() {
    try {
      this.loadingService.loadingOn();
      this.approvalService.getAll();
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao carregar aprovações.');
    } finally {
      this.loadingService.loadingOff();
    }
  }

  filteredApprovals = computed(() => {
    try {
      const normalizedQuery = this.searchQuery()
        .normalize('NFD')
        .replace(/[\u0300-\u036f]/g, '')
        .toLowerCase()
        .trim();
      const outcome = this.outcomeFilter();

      return this.approvalService.items().filter((item) => {
        if (outcome && item.outcome !== outcome) return false;

        if (!normalizedQuery) return true;

        const candidate = [
          item.id,
          item.releaseId,
          item.approverEmail,
          item.outcome,
          item.notes,
          item.timestamp,
        ]
          .join(' ')
          .normalize('NFD')
          .replace(/[\u0300-\u036f]/g, '')
          .toLowerCase();

        return candidate.includes(normalizedQuery);
      });
    } catch (error: any) {
      this.notify.showError(error.message || 'Erro ao filtrar aprovações.');
      return [];
    }
  });

  paginatedApprovals = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filteredApprovals().slice(start, start + this.pageSize());
  });

  filteredApprovalCount = computed(() => this.filteredApprovals().length);

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
    this.outcomeFilter.set('');
    this.pageIndex.set(0);
  }
}
