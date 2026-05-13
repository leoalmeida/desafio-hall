export interface AuditLogType {
  id?: number;
  actor: string;
  action: string;
  entity: string;
  entityId?: number;
  payload?: string;
  timestamp?: string;
}
