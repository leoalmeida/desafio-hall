export interface FreezeWindowSnapshot {
  env: 'DEV' | 'PREPROD' | 'PROD';
  start: string;
  end: string;
  timezone: string;
}

export interface ReleasePolicySnapshot {
  minApprovals: number;
  minScore: number;
  freezeWindows: FreezeWindowSnapshot[];
}

export const releasePolicySnapshot: ReleasePolicySnapshot = {
  minApprovals: 1,
  minScore: 70,
  freezeWindows: [
    {
      env: 'PROD',
      start: '22:00',
      end: '23:59',
      timezone: 'America/Sao_Paulo',
    },
  ],
};