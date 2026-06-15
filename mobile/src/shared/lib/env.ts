const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api';

// STOMP WebSocket 엔드포인트(`/ws`)는 서버 루트(=apiBaseUrl 에서 `/api` 제거) 기준.
// dev: 상대 `/api` → 같은 출처 ws(Vite proxy). prod: 절대 URL → http→ws 치환.
function deriveWsUrl(api: string): string {
  if (api.startsWith('/')) {
    if (typeof window !== 'undefined') {
      const proto = window.location.protocol === 'https:' ? 'wss' : 'ws';
      return `${proto}://${window.location.host}/ws`;
    }
    return 'ws://localhost:8080/ws';
  }
  return api.replace(/\/api\/?$/, '').replace(/^http/, 'ws') + '/ws';
}

export const env = {
  apiBaseUrl,
  wsUrl: deriveWsUrl(apiBaseUrl),
  kakaoRestKey: import.meta.env.VITE_KAKAO_REST_KEY ?? '',
  naverClientId: import.meta.env.VITE_NAVER_CLIENT_ID ?? '',
  googleClientId: import.meta.env.VITE_GOOGLE_CLIENT_ID ?? '',
  appleClientId: import.meta.env.VITE_APPLE_CLIENT_ID ?? '',
  kakaoMapKey: import.meta.env.VITE_KAKAOMAP_APP_KEY ?? '',
} as const;
