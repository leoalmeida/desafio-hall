import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { TitleService } from './services/title.service';
import { provideRouter } from '@angular/router';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { createSpyObj, SpyObj } from '../test-helpers/spy-utils';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let component: AppComponent;
  let titleServiceSpy: SpyObj<TitleService>;

  beforeEach(async () => {
    titleServiceSpy = createSpyObj<TitleService>(['setTitle']);

    TestBed.overrideComponent(AppComponent, {
      set: { template: '' },
    });

    await TestBed.configureTestingModule({
      imports: [AppComponent, NoopAnimationsModule, HttpClientTestingModule],
      providers: [
        { provide: TitleService, useValue: titleServiceSpy },
        provideRouter([]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
  });

  it('deve criar o app', () => {
    expect(component).toBeTruthy();
  });

  it('deve chamar setTitle do TitleService no ngOnInit', () => {
    fixture.detectChanges();
    expect(titleServiceSpy.setTitle).toHaveBeenCalled();
  });

  it('deve iniciar o título como string vazia', () => {
    expect(component['title']()).toBe('');
  });

  it('deve executar updateViewByRole sem lançar erro', () => {
    component.updateViewByRole();
  });
});
