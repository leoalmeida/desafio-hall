import { MatFormFieldModule } from '@angular/material/form-field';
import { Component, inject } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ApplicationType } from '../../../models/application-type';
import {
  MAT_DIALOG_DATA,
  MatDialogModule,
  MatDialogRef,
} from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';
import { ApplicationService } from 'src/app/services/application.service';

@Component({
  selector: 'app-application-details',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
  ],
  templateUrl: './application-details.html',
  styleUrls: ['./application-details.css'],
})
export class ApplicationDetails {
  private formBuilder: FormBuilder = inject(FormBuilder);
  private dialogRef: MatDialogRef<ApplicationDetails> = inject(MatDialogRef);
  public data: ApplicationType = inject(MAT_DIALOG_DATA) as ApplicationType;
  private applicationService = inject(ApplicationService);
  openType: 'create' | 'edit' = this.data && this.data.id ? 'edit' : 'create';

  formApplication = this.formBuilder.group({    
    name: ["", Validators.required],
    ownerTeam: ["", Validators.required],
    repoUrl: ["", Validators.required]
  });

  constructor() {
    if (this.openType === 'edit') {
      this.formApplication.patchValue(this.data);
    }
  }

  onSubmit(): void {
    if (this.formApplication.valid) {
      const application = {
        ...this.data,
        ...this.formApplication.value,
      } as ApplicationType;
      this.applicationService.createOne(application).subscribe({
        next: (created) => {
          this.dialogRef.close(created);
        },
      });
    }
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
