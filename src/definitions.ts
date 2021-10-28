export interface CapacitorOpenTokPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  connect(options: OpentokConnectOptions): Promise<OpentokConnectOptions>;
  disconnect(): Promise<{ value: string }>;
  mute(enabled: boolean): Promise<AudioStatus>;
}

export interface AudioStatus {
  enabled: boolean;
}
export interface OpentokConnectOptions {
  api_key: string;
  session_id: string;
  token: string;
}
