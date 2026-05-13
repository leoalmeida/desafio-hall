import { Injectable, signal } from '@angular/core';
import { UserType } from '../models/user-type';
import { BehaviorSubject } from 'rxjs';
import { TokenType, TokenValueType } from '../models/token-type';

@Injectable({
  providedIn: 'root',
})
export class TokenStorageService {
  private usuario = new BehaviorSubject<UserType>({} as UserType);
  private autenticado = new BehaviorSubject<boolean>(false);
  private loggedIn = signal<boolean>(false);

  constructor() {}

  isAuthenticated = this.loggedIn.asReadonly();
  loggedUser = this.usuario.getValue();
  loggedUser$ = this.usuario.asObservable();
  autenticado$ = this.autenticado.asObservable();

  signOut(): void {
    window.sessionStorage.clear();
    this.usuario.next({} as UserType);
    this.autenticado.next(false);
    this.loggedIn.set(false);
  }

  public saveJsonWebToken(accessToken: TokenType): UserType {
    if (accessToken && accessToken.tokenType === 'Bearer') {
      const userToken: TokenValueType = JSON.parse(
        atob(accessToken.token.split('.')[1]),
      );

      const user: UserType = {
        name: userToken.name,
        email: userToken.email,
        token: accessToken,
        userData: userToken,
        role: userToken.role,
      };

      this.saveUser(user);

      this.usuario.next(user);
      this.autenticado.next(true);
      this.loggedIn.set(true);

      return user;
    }
    return {} as UserType;
  }
  
  public hasRole(role: string): boolean {
    return this.usuario.getValue().userData?.role === role || false;
  }

  public saveUser(user: UserType): void {
    window.sessionStorage.removeItem('user'); // Clear previous user
    window.sessionStorage.setItem('user', JSON.stringify(user));
  }

  public getUser(): UserType {
    const user = window.sessionStorage.getItem('user');
    return user ? JSON.parse(user) : ({} as UserType);
  }
}
