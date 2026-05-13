import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomePage } from './home-page';
import { TitleService } from 'src/app/services/title.service';
import { createSpyObj, SpyObj } from '../../../test-helpers/spy-utils';

describe('HomePage', () => {
  let component: HomePage;
  let fixture: ComponentFixture<HomePage>;
  let titleServiceSpy: SpyObj<TitleService>;

  beforeEach(async () => {
    titleServiceSpy = createSpyObj<TitleService>(['setTitle']);

    await TestBed.configureTestingModule({
      imports: [HomePage],
      providers: [{ provide: TitleService, useValue: titleServiceSpy }],
    }).compileComponents();

    fixture = TestBed.createComponent(HomePage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar setTitle no ngOnInit', () => {
    expect(titleServiceSpy.setTitle).toHaveBeenCalled();
  });

  it('deve renderizar conteúdo principal da página inicial', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.textContent).toContain('Bem-vindo');
  });
});
