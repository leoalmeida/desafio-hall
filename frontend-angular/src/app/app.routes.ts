import { Routes } from '@angular/router';
import { ApplicationList } from './pages/application/application-list/application-list';
import { LoginPage } from './pages/login/login-page';
import { canActivateAdmin } from './guards/can-activate-admin';
import { canActivateApprover } from './guards/can-activate-approver';
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
    data: {
      title: 'Home',
      requiredRoles: ['ROLE_VIEWER', 'ROLE_APPROVER', 'ROLE_ADMIN'],
    },
  },
  {
    path: 'admin',
    component: AdminBoard,
    canActivate: [canActivateAdmin],
    data: { title: 'Painel Administrativo', requiredRoles: ['ROLE_ADMIN'] },
  },
  {
    path: 'applications',
    component: ApplicationList,
    canActivate: [canActivateViewer],
    data: {
      title: 'Gestão de Aplicações',
      requiredRoles: ['ROLE_VIEWER', 'ROLE_APPROVER', 'ROLE_ADMIN'],
    },
  },
  {
    path: 'releases',
    component: ReleaseList,
    canActivate: [canActivateViewer],
    data: {
      title: 'Gestão de Releases',
      requiredRoles: ['ROLE_VIEWER', 'ROLE_APPROVER', 'ROLE_ADMIN'],
    },
  },
  {
    path: 'approvals',
    component: ApprovalList,
    canActivate: [canActivateApprover],
    data: {
      title: 'Gestão de Aprovações',
      requiredRoles: ['ROLE_APPROVER', 'ROLE_ADMIN'],
    },
  },
  {
    path: 'audit',
    component: AuditlogList,
    canActivate: [canActivateAdmin],
    data: { title: 'Logs de Auditoria', requiredRoles: ['ROLE_ADMIN'] },
  },
  { path: 'auditlogs', redirectTo: 'audit' },
  { path: 'login', component: LoginPage, data: { title: 'Autenticação' } },
  {
    path: 'acesso-negado',
    component: AcessoNegado,
    data: { title: 'Acesso Negado' },
  },
  { path: '**', redirectTo: '' },
];
