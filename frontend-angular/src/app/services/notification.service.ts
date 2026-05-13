import { inject, Injectable } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar';
import { LoggerService } from './logger.service';

/** * Lógica centralizada para exibir snackbars.
 *    Facilita a personalização de estilos e padrões.
 *    Pode ser chamado de qualquer lugar no aplicativo.
 */
@Injectable({
  providedIn: 'root', // Makes the service available app-wide
})
export class NotificationService {
  private logger: LoggerService = inject(LoggerService);
  private snackBar: MatSnackBar = inject(MatSnackBar);

  constructor() {}

  /**
   * Apresenta uma mensagem de informação em um snackbar
   * @param message - A mensagem a ser exibida
   * @param action - Texto opcional do botão de ação
   * @param config - Configuração opcional personalizada
   */
  show(message: string, action = 'OK', config?: MatSnackBarConfig): void {
    const defaultConfig: MatSnackBarConfig = {
      duration: 3000, // Auto close after 3 seconds
      horizontalPosition: 'right',
      verticalPosition: 'top',
      panelClass: ['snackbar-default'], // Custom CSS class
    };

    this.logger.log('Notificação ["INFO"]:', message);
    this.snackBar.open(message, action, { ...defaultConfig, ...config });
  }

  /**
   * Apresenta um snackbar de erro para o usuário
   * @param message - A mensagem de erro a ser exibida
   */
  showError(message: string, config?: MatSnackBarConfig): void {
    this.logger.error('Notificação ["ERRO"]:', message);
    this.show(message, 'Close', { panelClass: ['snackbar-error'], ...config });
  }

  /**
   * Apresenta um snackbar de aviso para o usuário
   * @param message - A mensagem de aviso a ser exibida
   */
  showWarning(message: string, config?: MatSnackBarConfig): void {
    this.logger.warn('Notificação ["AVISO"]:', message);
    this.show(message, 'Close', {
      panelClass: ['snackbar-warning'],
      ...config,
    });
  }

  /**
   * Apresenta um snackbar de sucesso para o usuário
   * @param message - A mensagem de sucesso a ser exibida
   */
  showSuccess(message: string, config?: MatSnackBarConfig): void {
    this.logger.log('Notificação ["SUCESSO"]:', message);
    this.show(message, 'Close', {
      panelClass: ['snackbar-success'],
      ...config,
    });
  }
}
