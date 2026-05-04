// Types matching the Java backend GameStateDTO

export type GameStatus = 'SETUP' | 'PLAYING' | 'COMBAT' | 'READING' | 'DIVINATION' | 'GAMEOVER';
export type AttributeType = 'STAMINA' | 'STRENGTH' | 'PATIENCE' | 'INTELLIGENCE' | 'FOCUS';
export type RoomId =
  | 'LIVING_ROOM' | 'GREAT_HALL' | 'THRONE_ROOM'
  | 'MAIN_GATE' | 'ARMORY' | 'WATCHTOWER' | 'YARD'
  | 'GRAND_LIBRARY' | 'ALCHEMY_LAB' | 'OBSERVATORY'
  | 'DRESSING_ROOM' | 'GUEST_QUARTERS' | 'CHAPEL' | 'KITCHEN' | 'LORDS_CHAMBER'
  | 'DUNGEON' | 'BELL_TOWER' | 'SHADOW_CORRIDOR' | 'GREENHOUSE' | 'WINE_CELLAR';
export type ItemType = 'ETHER_POTION' | 'HOURGLASS' | 'STRAW_DOLL';
export type CombatPhase = 'STARTING' | 'COMPARING' | 'RESULT';
export type TarotCard = 'HERMIT' | 'WHEEL' | 'HANGED' | 'TOWER';

export interface Attributes {
  STAMINA: number;
  STRENGTH: number;
  PATIENCE: number;
  INTELLIGENCE: number;
  FOCUS: number;
}

export interface PendingRealloc {
  attrs: Record<string, number>;
  progress: number; // 0.0 to 1.0
  remainingSeconds: number;
}

export interface BeastDTO {
  satiety: number;
  state: 'CONTAINED' | 'ESCAPED';
  loc: string;
}

export interface CombatUpdateDTO {
  phase: string;
  roomId: string;
  roomName: string;
  attrsCompared: string[];
  playerPreAttrs: Record<string, number>;
  npcPreAttrs: Record<string, number>;
  playerSum: number;
  npcSum: number;
  winner: string;
  stealTotal: number;
  stolenValues: Record<string, number>;
  isExecution: boolean;
  timer: number;
}

export interface WordDTO {
  id: number;
  text: string;
  isCorrupt: boolean;
  rot: number;
}

export interface ReadingDTO {
  bookType: number;
  timer: number;
  corruption: number;
  words: WordDTO[];
}

export interface DivinationDTO {
  card: string;
  timer: number;
  displayName: string;
}

export interface TimersDTO {
  invisibility: number;
  trapped: number;
  divinationCooldown: number;
  showWarning: number;
  reallocProgress: number;
}

export interface GameStateDTO {
  status: GameStatus;
  playerLoc: string;
  playerAttrs: Record<string, number>;
  pendingRealloc: PendingRealloc | null;
  npcLoc: string;
  npcAttrs: Record<string, number>;
  beast: BeastDTO;
  combat: CombatUpdateDTO | null;
  reading: ReadingDTO | null;
  divination: DivinationDTO | null;
  inventory: string[];
  traps: string[];
  logs: string[];
  timers: TimersDTO;
  completedBooks: number[];
  instantReallocActive: boolean;
}

// Room data from backend
export interface RoomDTO {
  id: string;
  name: string;
  attrs: string[];
  adj: string[];
  x: number;
  y: number;
}

export interface RoomsResponse {
  rooms: RoomDTO[];
  edges: [string, string][];
}

export interface AdjustDistributedResponse {
  attributes: Record<string, number>;
}

// Action request types
export interface StartGameRequest {
  attributes: Record<string, number>;
}

export interface MoveRequest {
  targetRoomId: string;
}

export interface ReallocateRequest {
  attributes: Record<string, number>;
}

export interface ReadRequest {
  bookType: number;
}

export interface PurifyRequest {
  wordId: number;
}

export interface ItemUseRequest {
  itemType: string;
}

export interface AdjustDistributedRequest {
  attributes: Record<string, number>;
  targetKey: string;
  targetDelta: number;
}
