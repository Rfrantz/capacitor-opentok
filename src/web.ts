import { WebPlugin } from '@capacitor/core';
import type {
  AudioStatus,
  CapacitorOpenTokPlugin,
  OpentokConnectOptions,
} from './definitions';

export class CapacitorOpenTokWeb
  extends WebPlugin
  implements CapacitorOpenTokPlugin {
  async connect(
    options: OpentokConnectOptions,
  ): Promise<OpentokConnectOptions> {
    return options;
  }
  async disconnect(): Promise<{ value: string }> {
    return new Promise(value => value);
  }

  async mute(): Promise<AudioStatus> {
    return {} as AudioStatus;
  }

  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
