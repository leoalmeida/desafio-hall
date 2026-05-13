import { UserType } from 'src/app/models/user-type';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { TokenStorageService } from './token-storage.service';
import { TokenType } from '../models/token-type';


@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private baseUrl = '';
  private http: HttpClient = inject(HttpClient);
  private tokenStorageService = inject(TokenStorageService);

  loggedUser$ = this.tokenStorageService.loggedUser$;

  constructor() {
    this.baseUrl = environment.authApi || '/api/auth'; // Default to a fallback URL if not define
  }

  login(email: string, password: string): Observable<UserType> {
    this.http.post<TokenType>(`${this.baseUrl}/login`, { email, password })
      .subscribe((response: TokenType) => {
        this.tokenStorageService.saveJsonWebToken(response);
      });
    return this.loggedUser$;
  }

  logout(): void {
    this.http.post<any>(`${this.baseUrl}/signout`, { email: this.tokenStorageService.getUser().email }).subscribe();
    this.tokenStorageService.signOut();
  }
}
