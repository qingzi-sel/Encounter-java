import { useEffect, useRef, useState } from 'react';
import type { GameStateDTO } from '../types/game';

const WS_URL = 'ws://localhost:8088/ws/game';

export function useGameWebSocket() {
  const [gameState, setGameState] = useState<GameStateDTO | null>(null);
  const [connected, setConnected] = useState(false);
  const wsRef = useRef<WebSocket | null>(null);

  useEffect(() => {
    let reconnectTimer: ReturnType<typeof setTimeout>;
    let mounted = true;

    function connect() {
      const ws = new WebSocket(WS_URL);
      wsRef.current = ws;

      ws.onopen = () => {
        if (mounted) {
          setConnected(true);
          console.log('[WS] Connected');
        }
      };

      ws.onmessage = (event) => {
        if (!mounted) return;
        try {
          const data = JSON.parse(event.data);
          if (data.status !== undefined) {
            setGameState(data as GameStateDTO);
          }
        } catch (e) {
          // ignore malformed messages
        }
      };

      ws.onclose = () => {
        if (mounted) {
          setConnected(false);
          console.log('[WS] Disconnected, reconnecting in 2s...');
          reconnectTimer = setTimeout(connect, 2000);
        }
      };

      ws.onerror = (err) => {
        console.log('[WS] Error, will retry...');
        ws.close();
      };
    }

    connect();

    return () => {
      mounted = false;
      clearTimeout(reconnectTimer);
      wsRef.current?.close();
    };
  }, []);

  return { gameState, connected };
}
