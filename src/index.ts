import { registerPlugin } from '@capacitor/core';

import type { CapacitorOpenTokPlugin } from './definitions';

const CapacitorOpenTok = registerPlugin<CapacitorOpenTokPlugin>(
  'CapacitorOpenTok',
  {
    web: () => import('./web').then(m => new m.CapacitorOpenTokWeb()),
  },
);

export * from './definitions';
export { CapacitorOpenTok };
