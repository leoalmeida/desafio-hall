import { Routes } from '@angular/router';
import { ApplicationList } from './pages/application/application-list/application-list';
import { LoginPage } from './pages/login/login-page';
import { canActivateAdmin } from './guards/can-activate-admin';
import { canActivateViewer } from './guards/can-activate-viewer';
import { AdminBoard } from './pages/admin/admin-board/admin-board';
import { HomePage } from './pages/home-page/home-page';
import { AcessoNegado } from './pages/acesso-negado/acesso-negado';
import { ReleaseList } from './pages/release-version/release-list/release-list';
import { ApprovalList } from './pages/admin/approval-list/approval-list';
import { AuditlogList } from './pages/admin/auditlog-list/auditlog-list';

export const routes: Routes = [
  {
    path: '',
    component: HomePage,
    canActivate: [canActivateViewer],
    data: { title: 'Home' },
  },
  {
    path: 'admin',
    component: AdminBoard,
    canActivate: [canActivateAdmin],
    data: { title: 'Painel Administrativo' },
  },
  {
    path: 'applications',
    component: ApplicationList,
    canActivate: [canActivateViewer],
    data: { title: 'Gestão de Aplicações' },
  },
  {
    path: 'releases',
    component: ReleaseList,
    canActivate: [canActivateViewer],
    data: { title: 'Gestão de Releases' },
  },
  {
    path: 'approvals',
    component: ApprovalList,
    canActivate: [canActivateAdmin],
    data: { title: 'Gestão de Aprovações' },
  },
  {
    path: 'auditlogs',
    component: AuditlogList,
    canActivate: [canActivateAdmin],
    data: { title: 'Logs de Auditoria' },
  },
  { path: 'login', component: LoginPage, data: { title: 'Autenticação' } },
  {
    path: 'acesso-negado',
    component: AcessoNegado,
    data: { title: 'Acesso Negado' },
  },
  { path: '**', redirectTo: '' },
];
