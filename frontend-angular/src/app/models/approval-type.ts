export interface ApprovalType {
  id?: string;
  releaseId: string;
  approverEmail: string;
  outcome: 'APPROVED' | 'REJECTED' | string;
  notes?: string;
  timestamp?: string;
}
