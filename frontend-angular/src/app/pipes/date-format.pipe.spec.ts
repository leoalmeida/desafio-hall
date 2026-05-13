import { registerLocaleData } from '@angular/common';
import localePt from '@angular/common/locales/pt';
import { DateFormatPipe } from './date-format.pipe';

describe('DateFormatPipe', () => {
  let pipe: DateFormatPipe;
  const locale = 'pt-BR';

  beforeEach(() => {
    registerLocaleData(localePt);
    pipe = new DateFormatPipe(locale);
  });

  it('deve criar a instância', () => {
    expect(pipe).toBeTruthy();
  });

  it('deve formatar uma data válida corretamente no padrão dd/MM/yyyy HH:mm:ss', () => {
    // 2023-12-25 10:30:00
    const dateStr = '2023-12-25T10:30:00';
    const result = pipe.transform(dateStr);

    // Verifica se contém os elementos básicos da data formatada
    expect(result).toBe('25/12/2023 10:30:00');
  });

  it('deve retornar string vazia se o valor for inválido ou vazio', () => {
    expect(pipe.transform('')).toBe('');
  });

  it('deve lidar com diferentes formatos de entrada de data', () => {
    const date = new Date(2024, 0, 1, 15, 0, 0); // 01/01/2024 15:00:00
    expect(pipe.transform(date.toISOString())).toBe('01/01/2024 15:00:00');
  });

  it('deve retornar string vazia para datas mal formatadas', () => {
    const result = pipe.transform('data-invalida');
    expect(result).toBe('');
  });
});
