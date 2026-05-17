export interface AuditLogType {
  id?: string;
  actor: string;
  action: string;
  entity: string;
  entityId?: string;
  payload?: string;
  timestamp?: string;
}
