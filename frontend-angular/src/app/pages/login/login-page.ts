import { Component, inject, OnInit, signal } from '@angular/core';
import {
  ReactiveFormsModule,
  FormGroup,
  FormBuilder,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatCardModule } from '@angular/material/card';
import { AuthService } from '../../services/auth.service';
import { TokenStorageService } from '../../services/token-storage.service';
import { Router } from '@angular/router';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

interface IUsuario {
  username: string;
  password: string;
}

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    MatInputModule,
    MatCardModule,
  ],
  templateUrl: './login-page.html',
  styleUrls: ['./login-page.css'],
})
export class LoginPage implements OnInit {
  isLoggedIn = signal(false);
  isLoginError = signal(false);

  private authService: AuthService = inject(AuthService);
  private tokenStorage: TokenStorageService = inject(TokenStorageService);
  private navigator = inject(Router);
  private formBuilder = inject(FormBuilder);
  formLogin: FormGroup = this.formBuilder.group({
    username: ['', [Validators.required]],
    password: ['', [Validators.required]],
  });

  ngOnInit() {
    this.reloadPage();
  }

  onSubmit() {
    if (this.formLogin.invalid) return;
    const usuario = this.formLogin.getRawValue() as IUsuario;
    this.authService
      .login(usuario.username, usuario.password)
      .subscribe((user) => {
        if (user) {
          this.reloadPage();
        }
      });
  }

  reloadPage() {
    this.isLoggedIn.set(this.tokenStorage.isAuthenticated());
    if (this.isLoggedIn()) {
      this.navigator.navigate(['app']);
    }
  }
}
