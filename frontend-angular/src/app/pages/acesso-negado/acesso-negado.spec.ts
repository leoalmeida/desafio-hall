import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AcessoNegado } from './acesso-negado';
import { TitleService } from 'src/app/services/title.service';
import { createSpyObj, SpyObj } from '../../../../test-helpers/spy-utils';

describe('AcessoNegado', () => {
  let component: AcessoNegado;
  let fixture: ComponentFixture<AcessoNegado>;
  let titleServiceSpy: SpyObj<TitleService>;

  beforeEach(async () => {
    titleServiceSpy = createSpyObj<TitleService>(['setTitle']);

    await TestBed.configureTestingModule({
      imports: [AcessoNegado],
      providers: [{ provide: TitleService, useValue: titleServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(AcessoNegado);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve renderizar a mensagem de acesso negado', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Acesso Negado');
    expect(compiled.textContent).toContain(
      'Você não tem permissão para acessar esta página.',
    );
  });
});
