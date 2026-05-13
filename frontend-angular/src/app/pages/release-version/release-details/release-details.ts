import { CommonModule, CurrencyPipe } from '@angular/common';
import { ReleaseType } from '../../../models/release-type';
import { Component, computed, inject } from '@angular/core';
import {
  ReactiveFormsModule,
  FormGroup,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApplicationType } from '../../../models/application-type';
import { ReleaseService } from '../../../services/release.service';
import { NotificationService } from '../../../services/notification.service';

@Component({
  selector: 'app-release-details',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    MatSelectModule,
  ],
  providers: [CurrencyPipe],
  templateUrl: './release-details.html',
})
export class ReleaseDetails {
  private releaseService = inject(ReleaseService);
  private notify = inject(NotificationService);
  private formBuilder: FormBuilder = inject(FormBuilder);
  private dialogRef: MatDialogRef<ReleaseDetails> = inject(MatDialogRef);
  public data: ApplicationType = inject(MAT_DIALOG_DATA) as ApplicationType;

  formRelease: FormGroup = this.formBuilder.group({
    applicationId: [0, Validators.required],
    version: ['', Validators.required],
    env: ['DEV', Validators.required],
    status: ['CREATED', Validators.required],
    evidenceUrl: [''],
  });

  envTypeList = computed(() => [
    { key: 'DEV', value: 'Desenvolvimento' },
    { key: 'PREPROD', value: 'Pré-Produção' },
    { key: 'PROD', value: 'Produção' },
  ]);

  statusTypeList = computed(() => [
    { key: 'CREATED', value: 'Criada' },
    { key: 'PENDING_PREPROD', value: 'Pendente Pré-Produção' },
    { key: 'PENDING_PROD', value: 'Pendente Produção' },
    { key: 'APPROVED_PREPROD', value: 'Aprovada Pré-Produção' },
    { key: 'APPROVED_PROD', value: 'Aprovada Produção' },
    { key: 'REJECTED', value: 'Rejeitada' },
    { key: 'DEPLOYED', value: 'Deploy Realizado' },
  ]);

  constructor() {
    this.formRelease.patchValue({ applicationId: this.data?.id ?? 0 });
  }

  onSubmit(): void {
    if (this.formRelease.valid) {
      const release: ReleaseType = {
        ...this.formRelease.value,
      };

      this.releaseService.createOne(release).subscribe({
        next: () => {
          this.notify.showSuccess('Release criada com sucesso!');
          this.dialogRef.close();
        },
      });
    } else {
      this.notify.showError('Dados da release inválidos ou incompletos!');
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
