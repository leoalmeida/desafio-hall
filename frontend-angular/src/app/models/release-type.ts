export interface ReleaseType {
  id?: number;
  applicationId: number;
  version: string;
  env: 'DEV' | 'PREPROD' | 'PROD';
  status: 'CREATED' | 'PENDING_PREPROD' | 'PENDING_PROD' | 'APPROVED_PREPROD' | 'APPROVED_PROD' | 'REJECTED' | 'DEPLOYED';
  evidenceUrl?: string;
  createdAt?: string;
  deployedAt?: string;
}
