import { Component, inject, OnInit, signal } from '@angular/core';

import { MatCardModule } from '@angular/material/card';
import { TitleService } from 'src/app/services/title.service';
import { MatButtonModule } from '@angular/material/button';
import { Router } from '@angular/router';

@Component({
  selector: 'app-acesso-negado',
  standalone: true,
  imports: [MatCardModule, MatButtonModule],
  templateUrl: './acesso-negado.html',
  styleUrls: ['./acesso-negado.css'],
})
export class AcessoNegado implements OnInit {
  protected readonly title = signal('');
  private titleService: TitleService = inject(TitleService);
  private navigator = inject(Router);

  ngOnInit(): void {
    this.titleService.setTitle();
  }

  voltar() {
    this.navigator.navigate(['home']);
  }
}
