import type {
  StartGameRequest, MoveRequest, ReallocateRequest,
  ReadRequest, PurifyRequest, ItemUseRequest,
  RoomsResponse, AdjustDistributedRequest, AdjustDistributedResponse,
} from '../types/game';

const BASE_URL = 'http://localhost:8088/api/game';

async function post<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${BASE_URL}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const errText = await res.text();
    try {
      const err = JSON.parse(errText);
      throw new Error(((err as { error?: string }).error ?? errText) || res.statusText);
    } catch {
      throw new Error(errText || res.statusText);
    }
  }
  return res.json();
}

export const gameApi = {
  startGame(attributes: Record<string, number>) {
    return post<{ status: string }>('/start', { attributes } as StartGameRequest);
  },

  move(targetRoomId: string) {
    return post<{ status: string }>('/move', { targetRoomId } as MoveRequest);
  },

  reallocate(attributes: Record<string, number>) {
    return post<{ status: string }>('/reallocate', { attributes } as ReallocateRequest);
  },

  cancelReallocate() {
    return post<{ status: string }>('/reallocate/cancel', {});
  },

  startReading(bookType: number) {
    return post<{ status: string }>('/read', { bookType } as ReadRequest);
  },

  purifyWord(wordId: number) {
    return post<{ status: string }>('/reading/purify', { wordId } as PurifyRequest);
  },

  startDivination() {
    return post<{ status: string }>('/divination', {});
  },

  useItem(itemType: string) {
    return post<{ status: string }>('/item/use', { itemType } as ItemUseRequest);
  },

  startFeeding() {
    return post<{ status: string }>('/beast/feed/start', {});
  },

  stopFeeding() {
    return post<{ status: string }>('/beast/feed/stop', {});
  },

  async getState() {
    const res = await fetch(`${BASE_URL}/state`);
    return res.json();
  },

  async getRooms(): Promise<RoomsResponse> {
    const res = await fetch(`${BASE_URL}/rooms`);
    if (!res.ok) {
      const errText = await res.text();
      throw new Error(errText || 'Failed to fetch rooms');
    }
    return res.json();
  },

  adjustDistributed(attributes: Record<string, number>, targetKey: string, targetDelta: number) {
    return post<AdjustDistributedResponse>('/adjust-distributed', {
      attributes, targetKey, targetDelta,
    } as AdjustDistributedRequest);
  },
};
