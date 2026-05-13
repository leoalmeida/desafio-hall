import { TestBed } from '@angular/core/testing';
import { LoggerService } from './logger.service';

describe('LoggerService', () => {
  let service: LoggerService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [LoggerService],
    });
    service = TestBed.inject(LoggerService);
  });

  it('deve ser criado', () => {
    expect(service).toBeTruthy();
  });

  it('deve executar log() sem lançar erro', () => {
    expect(() => service.log('Teste de log', { data: 1 })).not.toThrow();
  });

  it('deve executar error() sem lançar erro', () => {
    expect(() => service.error('Erro crítico')).not.toThrow();
  });

  it('deve executar warn() sem lançar erro', () => {
    expect(() => service.warn('Aviso de sistema')).not.toThrow();
  });
});
