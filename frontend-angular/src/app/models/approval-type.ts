export interface ApprovalType {
  id?: number;
  releaseId: number;
  approverEmail: string;
  outcome: 'APPROVED' | 'REJECTED' | string;
  notes?: string;
  timestamp?: string;
}
