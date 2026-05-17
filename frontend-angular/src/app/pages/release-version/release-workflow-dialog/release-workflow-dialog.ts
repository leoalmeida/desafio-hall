import { CommonModule, DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { releasePolicySnapshot } from '../../../config/release-policy';
import { ApprovalType } from '../../../models/approval-type';
import { AuditLogType } from '../../../models/auditlog-type';
import { EvidenceScoreType } from '../../../models/evidence-score-type';
import { ReleaseType } from '../../../models/release-type';
import { ApprovalService } from '../../../services/approval.service';
import { AuditlogService } from '../../../services/auditlog.service';
import { LocalReviewNote, ReleaseService } from '../../../services/release.service';
import { TokenStorageService } from '../../../services/token-storage.service';

type ReleaseWorkflowMode = 'timeline' | 'approve' | 'disapprove' | 'promote';

interface ReleaseWorkflowDialogData {
  mode: ReleaseWorkflowMode;
  release: ReleaseType;
}

interface TimelineItem {
  timestamp: string;
  title: string;
  description: string;
  category: 'release' | 'approval' | 'audit' | 'note';
}

@Component({
  selector: 'app-release-workflow-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatButtonModule,
    MatDialogModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
  ],
  providers: [DatePipe],
  templateUrl: './release-workflow-dialog.html',
  styleUrl: './release-workflow-dialog.css',
})
export class ReleaseWorkflowDialog {
  private readonly dialogRef = inject(MatDialogRef<ReleaseWorkflowDialog>);
  private readonly approvalService = inject(ApprovalService);
  private readonly auditlogService = inject(AuditlogService);
  private readonly releaseService = inject(ReleaseService);
  private readonly tokenStorageService = inject(TokenStorageService);
  private readonly formBuilder = inject(FormBuilder);

  readonly data = inject(MAT_DIALOG_DATA) as ReleaseWorkflowDialogData;
  readonly loading = signal(true);
  readonly loadWarnings = signal<string[]>([]);
  readonly approvals = signal<ApprovalType[]>([]);
  readonly auditEntries = signal<AuditLogType[]>([]);
  readonly evidenceScore = signal<EvidenceScoreType | null>(null);
  readonly localNotes = signal<LocalReviewNote[]>([]);

  readonly reviewForm = this.formBuilder.group({
    notes: ['', this.requiresNote() ? [Validators.required, Validators.maxLength(1000)] : []],
  });

  readonly headerTitle = computed(() => {
    switch (this.data.mode) {
      case 'approve':
        return 'Aprovar release';
      case 'disapprove':
        return 'Rejeitar release';
      case 'promote':
        return 'Promover release';
      default:
        return 'Timeline e checklist da release';
    }
  });

  readonly policySummary = computed(() => {
    const targetEnv = this.getTargetEnvironment();
    return `Policy atual: minApprovals=${releasePolicySnapshot.minApprovals}, minScore=${releasePolicySnapshot.minScore}, target=${targetEnv}.`;
  });

  readonly checklistItems = computed(() => {
    const approvedCount = this.approvals().filter((item) => item.outcome === 'APPROVED').length;
    const score = this.evidenceScore()?.score ?? 0;
    const targetEnv = this.getTargetEnvironment();
    const promotionToProd = targetEnv === 'PROD';
    const requiredStatusOk = this.isPromotionStatusAllowed();
    const freezeOk = !this.isFreezeWindowActive(targetEnv);

    return [
      {
        label: 'Status permite a transição',
        ok: requiredStatusOk,
        detail: `Status atual ${this.data.release.status}.`,
      },
      {
        label: 'Aprovações mínimas',
        ok: !promotionToProd || approvedCount >= releasePolicySnapshot.minApprovals,
        detail: `${approvedCount}/${releasePolicySnapshot.minApprovals} aprovações válidas.`,
      },
      {
        label: 'Evidence URL preenchida',
        ok: !promotionToProd || Boolean(this.data.release.evidenceUrl?.trim()),
        detail: this.data.release.evidenceUrl?.trim() || 'Nenhuma evidência informada.',
      },
      {
        label: 'Evidence score mínimo',
        ok: !promotionToProd || score >= releasePolicySnapshot.minScore,
        detail: `Score atual ${score}/${releasePolicySnapshot.minScore}.`,
      },
      {
        label: 'Sem freeze window ativa',
        ok: freezeOk,
        detail: freezeOk ? `Sem bloqueio ativo para ${targetEnv}.` : `Freeze ativo para ${targetEnv}.`,
      },
    ];
  });

  readonly timelineItems = computed(() => {
    const items: TimelineItem[] = [];
    const release = this.data.release;

    if (release.createdAt) {
      items.push({
        timestamp: release.createdAt,
        title: 'Release criada',
        description: `${release.version} em ${release.env}.`,
        category: 'release',
      });
    }

    if (release.deployedAt) {
      items.push({
        timestamp: release.deployedAt,
        title: 'Deploy registrado',
        description: `Deploy concluído em ${release.env}.`,
        category: 'release',
      });
    }

    for (const approval of this.approvals()) {
      items.push({
        timestamp: approval.timestamp || '',
        title: `Approval ${approval.outcome}`,
        description: `${approval.approverEmail}${approval.notes ? ` • ${approval.notes}` : ''}`,
        category: 'approval',
      });
    }

    for (const note of this.localNotes()) {
      items.push({
        timestamp: note.createdAt,
        title: `Nota operacional ${note.action}`,
        description: `${note.actor || 'usuário atual'} • ${note.notes}`,
        category: 'note',
      });
    }

    for (const audit of this.auditEntries()) {
      items.push({
        timestamp: audit.timestamp || '',
        title: audit.action,
        description: `${audit.actor} • ${audit.entity}${audit.payload ? ` • ${audit.payload}` : ''}`,
        category: 'audit',
      });
    }

    return items.sort((left, right) => this.parseTimestamp(right.timestamp) - this.parseTimestamp(left.timestamp));
  });

  readonly canSubmit = computed(() => {
    if (this.data.mode === 'timeline') {
      return false;
    }

    if (this.requiresNote()) {
      return this.reviewForm.valid;
    }

    if (this.data.mode === 'promote') {
      return this.checklistItems().every((item) => item.ok);
    }

    return true;
  });

  readonly notePersistenceWarning = computed(() =>
    this.requiresNote()
      ? 'A API atual de approve/disapprove não recebe notes. A observação será mantida na sessão do navegador e exibida na timeline local.'
      : '',
  );

  constructor() {
    this.loadWorkflowData();
  }

  close(): void {
    this.dialogRef.close();
  }

  confirm(): void {
    if (!this.canSubmit()) {
      this.reviewForm.markAllAsTouched();
      return;
    }

    this.dialogRef.close({
      action: this.data.mode,
      notes: this.reviewForm.getRawValue().notes?.trim() || '',
    });
  }

  private loadWorkflowData(): void {
    const releaseId = this.data.release.id;
    if (!releaseId) {
      this.loading.set(false);
      return;
    }

    forkJoin({
      approvals: this.approvalService
        .searchApprovals('', releaseId, '')
        .pipe(catchError(() => {
          this.appendWarning('Não foi possível carregar aprovações da release.');
          return of([] as ApprovalType[]);
        })),
      evidenceScore: this.releaseService
        .getEvidenceScore(releaseId)
        .pipe(catchError(() => {
          this.appendWarning('Score de evidência indisponível no momento.');
          return of(null);
        })),
      auditEntries: this.tokenStorageService.hasRole('ROLE_ADMIN')
        ? this.auditlogService.findReleaseTimeline(releaseId).pipe(catchError(() => {
            this.appendWarning('Timeline de auditoria indisponível.');
            return of([] as AuditLogType[]);
          }))
        : of([] as AuditLogType[]),
    }).subscribe(({ approvals, evidenceScore, auditEntries }) => {
      this.approvals.set(approvals);
      this.evidenceScore.set(evidenceScore);
      this.auditEntries.set(auditEntries);
      this.localNotes.set(this.releaseService.getLocalReviewNotes(releaseId));
      this.loading.set(false);
    });
  }

  private appendWarning(message: string): void {
    this.loadWarnings.update((warnings) => [...warnings, message]);
  }

  requiresNote(): boolean {
    return this.data.mode === 'approve' || this.data.mode === 'disapprove';
  }

  private getTargetEnvironment(): 'DEV' | 'PREPROD' | 'PROD' {
    if (this.data.release.env === 'DEV') {
      return 'PREPROD';
    }

    return 'PROD';
  }

  private isPromotionStatusAllowed(): boolean {
    if (this.data.mode !== 'promote') {
      return true;
    }

    if (this.data.release.env === 'DEV') {
      return this.data.release.status === 'CREATED';
    }

    if (this.data.release.env === 'PREPROD') {
      return this.data.release.status === 'APPROVED_PREPROD';
    }

    return false;
  }

  private isFreezeWindowActive(targetEnv: 'DEV' | 'PREPROD' | 'PROD'): boolean {
    const window = releasePolicySnapshot.freezeWindows.find((item) => item.env === targetEnv);
    if (!window) {
      return false;
    }

    const formatter = new Intl.DateTimeFormat('en-GB', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
      timeZone: window.timezone,
    });
    const currentTime = formatter.format(new Date());

    return this.isBetweenTimeRange(currentTime, window.start, window.end);
  }

  private isBetweenTimeRange(time: string, start: string, end: string): boolean {
    const currentMinutes = this.toMinutes(time);
    const startMinutes = this.toMinutes(start);
    const endMinutes = this.toMinutes(end);

    if (startMinutes <= endMinutes) {
      return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
    }

    return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
  }

  private toMinutes(value: string): number {
    const [hours, minutes] = value.split(':').map((item) => Number(item));
    return hours * 60 + minutes;
  }

  private parseTimestamp(timestamp?: string): number {
    if (!timestamp) {
      return 0;
    }

    const parsed = Date.parse(timestamp);
    return Number.isNaN(parsed) ? 0 : parsed;
  }
}