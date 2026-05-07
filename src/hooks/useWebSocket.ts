import { useEffect, useRef, useState } from 'react';
import type { GameStateDTO } from '../types/game';

const WS_URL = 'ws://localhost:8088/ws/game';

export function useGameWebSocket() {
  const [gameState, setGameState] = useState<GameStateDTO | null>(null);
  const [connected, setConnected] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectTimerRef = useRef<number | null>(null);
  const mountedRef = useRef(true);

  function clearReconnectTimer() {
    if (reconnectTimerRef.current !== null) {
      window.clearTimeout(reconnectTimerRef.current);
      reconnectTimerRef.current = null;
    }
  }

  function connect() {
    setError(null);
    const ws = new WebSocket(WS_URL);
    wsRef.current = ws;

    ws.onopen = () => {
      if (!mountedRef.current) return;
      setConnected(true);
      setError(null);
      console.log('[WS] Connected');
    };

    ws.onmessage = (event) => {
      if (!mountedRef.current) return;
      try {
        const data = JSON.parse(event.data);
        if (data.status !== undefined) {
          setGameState(data as GameStateDTO);
        }
      } catch (e) {
        console.warn('[WS] Malformed messages received', e);
      }
    };

    ws.onclose = () => {
      if (!mountedRef.current) return;
      setConnected(false);
      setError('WebSocket 已断开，正在重连');
      console.log('[WS] Disconnected, reconnecting in 2s...');
      clearReconnectTimer();
      reconnectTimerRef.current = window.setTimeout(connect, 2000);
    };

    ws.onerror = (err) => {
      if (!mountedRef.current) return;
      setError('WebSocket 连接发生错误');
      console.log('[WS] Error, will retry...', err);
      ws.close();
    };
  }

  function reconnect() {
    if (!mountedRef.current) return;
    clearReconnectTimer();
    wsRef.current?.close();
    connect();
  }

  useEffect(() => {
    connect();

    return () => {
      mountedRef.current = false;
      clearReconnectTimer();
      wsRef.current?.close();
    };
  }, []);

  return { gameState, connected, error, reconnect };
}
