/**
 * Encounter 遭遇 — React Frontend (Backend-driven)
 * All game logic runs on the Java Spring Boot server.
 * This frontend displays state from WebSocket and sends actions via REST.
 */

import React, { useState, useCallback, useEffect, useMemo } from 'react';
import { Activity, Swords } from 'lucide-react';
import { motion, AnimatePresence } from 'motion/react';
import { gameApi } from './api/game';
import { useGameWebSocket } from './hooks/useWebSocket';
import type { GameStateDTO, AttributeType, RoomId, ItemType, RoomDTO } from './types/game';

// ============================================================
// Local constants (mirror backend domain for UI rendering)
// ============================================================

const ATTR_NAMES: Record<string, string> = {
  STAMINA: '耐力', STRENGTH: '力量', PATIENCE: '耐心',
  INTELLIGENCE: '智力', FOCUS: '注意力',
};

const ATTR_ORDER: string[] = ['STAMINA', 'STRENGTH', 'PATIENCE', 'INTELLIGENCE', 'FOCUS'];

const ITEM_NAMES: Record<string, string> = {
  ETHER_POTION: '隐世药剂', HOURGLASS: '时光沙漏', STRAW_DOLL: '厄运稻草人',
};

const TAROT_NAMES: Record<string, string> = {
  HERMIT: '《 隐 者 》', WHEEL: '《 命 运 之 轮 》',
  HANGED: '《 倒 吊 人 》', TOWER: '《 高 塔 》',
};

// ============================================================
// Audio (stays client-side for low latency)
// ============================================================

const playSound = (type: 'start' | 'reveal' | 'tower' | 'read_start' | 'read_click' | 'read_clear' | 'read_corrupt') => {
  try {
    const actx = new (window.AudioContext || (window as any).webkitAudioContext)();
    if (actx.state === 'suspended') actx.resume();
    const osc = actx.createOscillator();
    const gain = actx.createGain();

    if (type === 'start') {
      osc.type = 'sine'; osc.frequency.setValueAtTime(440, actx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(880, actx.currentTime + 1.5);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.3, actx.currentTime + 0.1);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 1.5);
      osc.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 1.5);
    } else if (type === 'reveal') {
      osc.type = 'triangle'; osc.frequency.setValueAtTime(600, actx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(1200, actx.currentTime + 0.5);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.5, actx.currentTime + 0.1);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 0.5);
      osc.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 0.5);
    } else if (type === 'tower') {
      osc.type = 'sawtooth'; osc.frequency.setValueAtTime(100, actx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(50, actx.currentTime + 2);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.8, actx.currentTime + 0.1);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 2);
      osc.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 2);
    } else if (type === 'read_start') {
      osc.type = 'sine'; osc.frequency.setValueAtTime(300, actx.currentTime);
      osc.frequency.linearRampToValueAtTime(600, actx.currentTime + 1);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.1, actx.currentTime + 0.1);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 1);
      osc.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 1);
    } else if (type === 'read_click') {
      const osc2 = actx.createOscillator(); const gain2 = actx.createGain();
      osc.type = 'sine'; osc.frequency.setValueAtTime(1600, actx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(2400, actx.currentTime + 0.15);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.4, actx.currentTime + 0.01);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 0.2);
      osc2.type = 'triangle'; osc2.frequency.setValueAtTime(400, actx.currentTime);
      osc2.frequency.exponentialRampToValueAtTime(100, actx.currentTime + 0.1);
      gain2.gain.setValueAtTime(0, actx.currentTime);
      gain2.gain.linearRampToValueAtTime(0.3, actx.currentTime + 0.01);
      gain2.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 0.15);
      osc.connect(gain); gain.connect(actx.destination);
      osc2.connect(gain2); gain2.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 0.2);
      osc2.start(); osc2.stop(actx.currentTime + 0.15);
    } else if (type === 'read_clear') {
      osc.type = 'sine'; osc.frequency.setValueAtTime(600, actx.currentTime);
      osc.frequency.setValueAtTime(800, actx.currentTime + 0.1);
      osc.frequency.setValueAtTime(1000, actx.currentTime + 0.2);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.3, actx.currentTime + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 0.5);
      osc.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 0.5);
    } else if (type === 'read_corrupt') {
      const osc2 = actx.createOscillator(); const lpf = actx.createBiquadFilter();
      lpf.type = 'lowpass'; lpf.frequency.setValueAtTime(800, actx.currentTime);
      lpf.frequency.exponentialRampToValueAtTime(100, actx.currentTime + 1.5);
      osc.type = 'sawtooth'; osc.frequency.setValueAtTime(80, actx.currentTime);
      osc.frequency.exponentialRampToValueAtTime(20, actx.currentTime + 1.5);
      osc2.type = 'square'; osc2.frequency.setValueAtTime(76, actx.currentTime);
      osc2.frequency.exponentialRampToValueAtTime(18, actx.currentTime + 1.5);
      gain.gain.setValueAtTime(0, actx.currentTime);
      gain.gain.linearRampToValueAtTime(0.6, actx.currentTime + 0.05);
      gain.gain.exponentialRampToValueAtTime(0.01, actx.currentTime + 1.5);
      osc.connect(lpf); osc2.connect(lpf); lpf.connect(gain); gain.connect(actx.destination);
      osc.start(); osc.stop(actx.currentTime + 1.5);
      osc2.start(); osc2.stop(actx.currentTime + 1.5);
    }
  } catch (e) { console.error('Audio block', e); }
};

// ============================================================
// Attribute math helpers (client-side for draft UI)
// ============================================================

function snapVal(val: number): number {
  if (val < 1) return 0;
  return Math.round(val * 10) / 10;
}

function calcHP(attrs: Record<string, number>): number {
  return Number(Object.values(attrs).reduce((sum, v) => sum + v, 0).toFixed(1));
}

// ============================================================
// Setup Screen (local drafting, then POST to backend)
// ============================================================

const SetupScreen: React.FC<{ onStart: (attrs: Record<string, number>) => void }> = ({ onStart }) => {
  const [draft, setDraft] = useState<Record<string, number>>({
    STAMINA: 20, STRENGTH: 20, PATIENCE: 20, INTELLIGENCE: 20, FOCUS: 20,
  });
  const draftRef = React.useRef(draft);
  React.useEffect(() => { draftRef.current = draft; }, [draft]);

  const handleAdjust = (key: string, delta: number) => {
    gameApi.adjustDistributed(draftRef.current, key, delta)
      .then(r => { setDraft(r.attributes); draftRef.current = r.attributes; })
      .catch(console.error);
  };

  const totHP = calcHP(draft);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-6 font-mono">
      <div className="max-w-md w-full bg-theme-card border border-theme-border p-8 shadow-[0_0_20px_rgba(0,0,0,0.5)]">
        <h1 className="text-[24px] font-bold mb-2 text-center text-theme-cyan tracking-[4px] uppercase">
          ENCOUNTER // 遭遇
        </h1>
        <p className="text-[#8b949e] text-xs mb-6 text-center tracking-widest">
          系统检测到你的存在，请分配初始属性点
        </p>

        <div className="flex justify-between items-center bg-black border border-theme-border p-4 mb-6 relative overflow-hidden">
          <div className="absolute left-0 top-0 h-full bg-[linear-gradient(90deg,var(--color-theme-red),#ff9999)] opacity-30 transition-all"
            style={{ width: `${(totHP / 100) * 100}%` }} />
          <span className="font-semibold text-theme-text text-sm z-10 relative">总血量 (HP)</span>
          <span className={`text-xl font-bold z-10 relative ${totHP > 100 ? 'text-theme-red' : 'text-theme-green'}`}>
            {totHP.toFixed(1)} / 100.0
          </span>
        </div>

        <div className="space-y-4">
          {ATTR_ORDER.map(key => (
            <div key={key} className="flex items-center gap-2">
              <label className="w-[80px] font-medium text-[14px] text-theme-text">{ATTR_NAMES[key]}</label>
              <div className="flex-1 flex items-center gap-1 min-w-0">
                <button onClick={() => handleAdjust(key, -10)}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 w-9 h-[32px] shrink-0 text-[11px] font-bold">-10</button>
                <button onClick={() => handleAdjust(key, -1)}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 w-8 h-[32px] shrink-0 font-bold">-1</button>
                <div className="flex-1 min-w-[30px] border-b border-theme-border px-1 py-1 text-center text-theme-green font-bold h-[32px]">
                  {draft[key].toFixed(1)}
                </div>
                <button onClick={() => handleAdjust(key, 1)}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 w-8 h-[32px] shrink-0 font-bold">+1</button>
                <button onClick={() => handleAdjust(key, 10)}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 w-9 h-[32px] shrink-0 text-[11px] font-bold">+10</button>
              </div>
            </div>
          ))}
        </div>

        <button onClick={() => onStart(draft)} disabled={totHP > 100 || totHP <= 0}
          className="mt-8 w-full bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed uppercase font-bold py-3 transition">
          启动序列
        </button>
      </div>
    </div>
  );
};

// ============================================================
// Player State Panel
// ============================================================

const PlayerStatePanel: React.FC<{ s: GameStateDTO }> = ({ s }) => {
  const isReallocating = s.pendingRealloc !== null;
  const currentHP = calcHP(s.playerAttrs);
  const [draft, setDraft] = useState<Record<string, number>>(s.playerAttrs);
  // Track last server-side attrs to avoid resetting draft on every WebSocket tick
  const lastServerAttrsRef = React.useRef(JSON.stringify(s.playerAttrs));

  // Only reset draft when server-side attrs actually changed (combat, realloc complete, etc.)
  React.useEffect(() => {
    const key = JSON.stringify(s.playerAttrs);
    if (key !== lastServerAttrsRef.current) {
      lastServerAttrsRef.current = key;
      if (!isReallocating) setDraft(s.playerAttrs);
    }
  }, [s.playerAttrs, isReallocating]);

  const draftHP = calcHP(draft);
  const progressPercent = isReallocating ? s.pendingRealloc!.progress * 100 : 0;

  const draftRef = React.useRef(draft);
  React.useEffect(() => { draftRef.current = draft; }, [draft]);

  const handleAdjust = (attr: string, delta: number) => {
    gameApi.adjustDistributed(draftRef.current, attr, delta)
      .then(r => { setDraft(r.attributes); draftRef.current = r.attributes; })
      .catch(console.error);
  };

  const handleApply = () => {
    if (calcHP(draft) <= currentHP) {
      gameApi.reallocate(draft).catch(console.error);
    }
  };

  const handleCancel = () => {
    gameApi.cancelReallocate().catch(console.error);
  };

  const getEffAttr = (key: string): number => {
    let val = s.playerAttrs[key] ?? 0;
    if (key === 'FOCUS' && s.beast.state === 'CONTAINED' && s.beast.satiety < 30) {
      val *= 0.5;
    }
    return snapVal(val);
  };

  return (
    <>
      <div className="text-[12px] text-theme-cyan uppercase border-b border-theme-border pb-1 mb-3">神经重组 [属性分配]</div>
      <div className="space-y-3 flex-1 overflow-y-auto">
        {ATTR_ORDER.map(key => {
          const baseVal = s.playerAttrs[key] ?? 0;
          const effVal = getEffAttr(key);
          const isDebuffed = effVal < baseVal;
          return (
            <div key={key} className="flex items-center gap-1 sm:gap-2 mb-3">
              <span className="w-[50px] sm:w-[60px] text-[13px] text-theme-text shrink-0">{ATTR_NAMES[key]}</span>
              <div className="w-[40px] flex flex-col justify-center items-start shrink-0 leading-[1.1]">
                {isDebuffed ? (
                  <>
                    <span className="line-through opacity-50 text-[9px] text-theme-red">{baseVal.toFixed(1)}</span>
                    <span className="text-[14px] font-bold text-purple-400">{effVal.toFixed(1)}</span>
                  </>
                ) : (
                  <span className="text-[13px] font-bold text-theme-green">{baseVal.toFixed(1)}</span>
                )}
              </div>
              <div className="flex-1 flex items-center gap-[2px] sm:gap-1 min-w-0">
                <button onClick={() => handleAdjust(key, -10)} disabled={isReallocating}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed w-7 sm:w-8 h-[30px] shrink-0 text-[10px] font-bold">-10</button>
                <button onClick={() => handleAdjust(key, -1)} disabled={isReallocating}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed w-6 sm:w-7 h-[30px] shrink-0 font-bold">-</button>
                <div className="flex-1 w-0 min-w-[30px] bg-transparent border-b border-theme-border px-1 py-1 text-center text-theme-green h-[30px] text-[12px] flex items-center justify-center font-bold">
                  {draft[key]?.toFixed(1) ?? '0.0'}
                </div>
                <button onClick={() => handleAdjust(key, 1)} disabled={isReallocating}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed w-6 sm:w-7 h-[30px] shrink-0 font-bold">+</button>
                <button onClick={() => handleAdjust(key, 10)} disabled={isReallocating}
                  className="bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed w-7 sm:w-8 h-[30px] shrink-0 text-[10px] font-bold">+10</button>
              </div>
            </div>
          );
        })}
      </div>

      <div className="mt-auto pt-4 flex flex-col">
        <div className="text-[12px] mb-2 flex items-center gap-2">
          <span>剩余分配能力:</span>
          <span className={`font-bold ${draftHP > currentHP ? 'text-theme-red' : 'text-theme-cyan'}`}>
            {(currentHP - draftHP).toFixed(1)}
          </span>
        </div>
        <button onClick={handleApply} disabled={draftHP > currentHP || isReallocating}
          className="w-full bg-transparent border border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10 disabled:opacity-30 disabled:cursor-not-allowed uppercase text-[12px] py-[10px] transition cursor-pointer mb-2">
          确认属性重组
        </button>
        <div className="h-[40px] border border-dashed border-theme-border flex items-center justify-center relative">
          <div className="absolute left-0 top-0 h-full bg-theme-cyan/20 transition-all duration-75" style={{ width: `${progressPercent}%` }} />
          <span className="relative z-10 text-[10px] text-theme-text shadow-sm">
            {isReallocating ? `重组中... ${Math.abs(s.pendingRealloc!.remainingSeconds).toFixed(1)}s (点击取消)` : '就绪'}
          </span>
          {isReallocating && (
            <button onClick={handleCancel} className="absolute inset-0 w-full h-full opacity-0 cursor-pointer z-20" title="点击取消重组" />
          )}
        </div>
      </div>

      {/* Inventory */}
      <div className="mt-4 border-t border-theme-border pt-4">
        <div className="text-[10px] text-theme-cyan/70 uppercase mb-2">以太背包 (Inventory)</div>
        <div className="flex flex-wrap gap-2">
          {s.inventory.length === 0 ? (
            <span className="text-[10px] text-theme-text/30">空无一物...</span>
          ) : (
            s.inventory.map((item, idx) => (
              <button key={idx} onClick={() => gameApi.useItem(item).catch(console.error)}
                className="border border-yellow-500/50 bg-black text-yellow-500 p-1 px-2 text-[10px] hover:bg-yellow-500/20 uppercase">
                {ITEM_NAMES[item] ?? item}
              </button>
            ))
          )}
        </div>
      </div>
    </>
  );
};

// ============================================================
// Map Panel
// ============================================================

interface RoomData {
  roomsMap: Record<string, RoomDTO>;
  edges: [string, string][];
}

const MapPanel: React.FC<{ s: GameStateDTO; roomData: RoomData | null }> = ({ s, roomData }) => {
  const roomsMap: Record<string, RoomDTO> = roomData?.roomsMap ?? {};
  const edges: [string, string][] = roomData?.edges ?? [];
  const room = roomsMap[s.playerLoc];
  const [zoom, setZoom] = useState(1);
  const G_SPACING = 140;

  const handleWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    setZoom(z => Math.max(0.3, Math.min(z - e.deltaY * 0.001, 2.5)));
  };

  const handleMove = (targetId: string) => {
    if (s.timers.trapped > 0) return;
    gameApi.move(targetId).catch(console.error);
  };

  const handleRead = (bookType: number) => {
    gameApi.startReading(bookType).catch(console.error);
  };

  const handleDivination = () => {
    gameApi.startDivination().catch(console.error);
  };

  return (
    <>
      <div className="text-[12px] text-theme-cyan uppercase border-b border-theme-border pb-1 mb-6 flex items-center justify-between shrink-0">
        <span>战术地图终端</span>
        <div className="flex items-center gap-2">
          <span className="text-[10px] text-theme-cyan/50 font-mono">ZOOM: {(zoom * 100).toFixed(0)}%</span>
          <span className="text-[10px] text-[#8b949e]">区域全息投影</span>
        </div>
      </div>

      <div className="flex-1 flex flex-col items-center justify-center w-full relative">
        {s.status === 'COMBAT' && (
          <div className="absolute inset-0 bg-theme-bg/85 z-30 flex flex-col items-center justify-center backdrop-blur-[2px] shadow-[0_0_80px_var(--color-theme-red)_inset] animate-[pulse_2s_ease-in-out_infinite]">
            <Swords className="text-theme-red w-16 h-16 mb-4 animate-bounce" />
            <div className="text-[32px] sm:text-[44px] font-bold text-theme-red tracking-widest uppercase shadow-[0_0_20px_var(--color-theme-red)]">
              战斗中
            </div>
            <div className="text-theme-red/90 text-[12px] sm:text-[14px] mt-2 font-mono tracking-widest">
              / 系统强行锁定物理位移 /
            </div>
          </div>
        )}

        <div className="relative w-full h-[360px] overflow-hidden bg-black/40 border border-theme-border/50 shadow-[0_0_20px_rgba(0,242,255,0.05)_inset] rounded-sm flex-shrink-0 cursor-crosshair"
          onWheel={handleWheel}>
          <div className="absolute w-0 h-0 transition-transform duration-700 ease-out"
            style={{
              left: '50%', top: '50%',
              transform: `scale(${zoom}) translate(${-(roomsMap[s.playerLoc]?.x ?? 0) * G_SPACING}px, ${-(roomsMap[s.playerLoc]?.y ?? 0) * G_SPACING}px)`
            }}>
            {/* Connections */}
            <svg className="absolute inset-0 overflow-visible pointer-events-none">
              {edges.map(([u, v]) => {
                const isActive = u === s.playerLoc || v === s.playerLoc;
                return (
                  <line key={`${u}-${v}`}
                    x1={(roomsMap[u]?.x ?? 0) * G_SPACING} y1={(roomsMap[u]?.y ?? 0) * G_SPACING}
                    x2={(roomsMap[v]?.x ?? 0) * G_SPACING} y2={(roomsMap[v]?.y ?? 0) * G_SPACING}
                    stroke={isActive ? 'var(--color-theme-cyan)' : 'var(--color-theme-border)'}
                    strokeWidth={isActive ? 2 : 1} strokeDasharray={isActive ? 'none' : '4 4'}
                    opacity={isActive ? 0.6 : 0.2} />
                );
              })}
            </svg>

            {/* Rooms */}
            {Object.values(roomsMap).map((r) => {
              const isCurrent = s.playerLoc === r.id;
              const isAdj = roomsMap[s.playerLoc]?.adj?.includes(r.id);
              const isNpc = s.npcLoc === r.id;
              const isBeast = s.beast.state === 'ESCAPED' && s.beast.loc === r.id;

              let boxCls = 'border-theme-border/30 text-[#8b949e]';
              if (isCurrent) boxCls = 'border-theme-cyan bg-theme-cyan/5 shadow-[0_0_15px_rgba(0,242,255,0.15)] z-20 text-theme-cyan';
              else if (isAdj) boxCls = 'border-theme-border hover:border-theme-cyan/80 hover:bg-theme-cyan/10 cursor-pointer z-10 text-theme-text';

              return (
                <button key={r.id} onClick={() => isAdj && handleMove(r.id)}
                  disabled={!isAdj && !isCurrent}
                  className={`absolute flex flex-col items-center justify-center transition-all bg-[var(--color-theme-bg)] ${boxCls}`}
                  style={{
                    left: `${r.x * G_SPACING}px`, top: `${r.y * G_SPACING}px`,
                    transform: 'translate(-50%, -50%)', width: '84px', height: '56px', borderWidth: '1px'
                  }}>
                  <span className="text-[12px] font-bold tracking-widest leading-tight">{r.name}</span>
                  {r.attrs.length > 0 ? (
                    <span className={`text-[9px] mt-[2px] leading-tight ${isCurrent ? 'text-theme-cyan/80' : 'text-[#8b949e]/80'}`}>
                      {r.attrs.map(a => ATTR_NAMES[a]).join(' ')}
                    </span>
                  ) : (
                    <span className="text-[9px] text-[#8b949e]/50 mt-[2px] leading-tight">安全区</span>
                  )}
                  <div className="flex gap-[6px] mt-[4px] h-[6px] items-center">
                    {isCurrent && <div className="w-[6px] h-[6px] bg-theme-cyan shadow-[0_0_8px_var(--color-theme-cyan)]" title="Player" />}
                    {isNpc && <div className="w-[6px] h-[6px] bg-theme-red rounded-full shadow-[0_0_8px_var(--color-theme-red)] animate-[pulse_1s_ease-in-out_infinite]" title="NPC" />}
                    {isBeast && <div className="w-[6px] h-[6px] bg-purple-500 rounded-full shadow-[0_0_8px_purple] animate-ping" title="Beast" />}
                    {s.traps.includes(r.id) && <div className="w-[6px] h-[6px] bg-yellow-500 rounded-sm shadow-[0_0_8px_yellow]" title="Trap" />}
                  </div>
                </button>
              );
            })}
          </div>
        </div>

        {/* Current Room Info */}
        {room && (
          <div className="w-full max-w-[360px] flex flex-col gap-2 mt-[15px] shrink-0">
            <div className="bg-theme-cyan/5 border border-theme-border p-3 text-center">
              <div className="flex items-center justify-center gap-2 mb-[4px]">
                <div className="w-2 h-2 bg-theme-cyan shadow-[0_0_5px_var(--color-theme-cyan)]" />
                <div className="text-[16px] text-theme-text font-bold uppercase">{room.name}</div>
              </div>
              <div className="text-[12px] opacity-80 text-theme-cyan font-mono">
                {room.attrs.length > 0 ? `当前环境辐射 | ${room.attrs.map(a => ATTR_NAMES[a]).join(' · ')}` : '绝对安全区域 | 属性无变动'}
              </div>
            </div>

            {s.playerLoc === 'DUNGEON' && s.beast.state === 'CONTAINED' && (
              <div className="bg-[#1a0000] border border-theme-red p-3 flex flex-col justify-center">
                <div className="flex justify-between items-center text-[10px] text-theme-red/80 mb-2 font-bold uppercase tracking-widest">
                  <span>⚠️ 收容实体饱食度</span>
                  <span>{Math.floor(s.beast.satiety)}%</span>
                </div>
                <div className="w-full bg-black h-1.5 mb-3 border border-theme-red/30 flex overflow-hidden">
                  <div className="bg-theme-red h-full" style={{ width: `${Math.max(0, s.beast.satiety)}%` }} />
                </div>
                <button
                  onPointerDown={() => gameApi.startFeeding().catch(console.error)}
                  onPointerUp={() => gameApi.stopFeeding().catch(console.error)}
                  onPointerLeave={() => gameApi.stopFeeding().catch(console.error)}
                  onPointerCancel={() => gameApi.stopFeeding().catch(console.error)}
                  className="w-full bg-transparent border border-theme-red text-theme-red hover:bg-theme-red/20 h-[36px] text-[12px] uppercase font-bold active:bg-theme-red active:text-white transition-colors cursor-pointer select-none">
                  [长按] 强制输送生体精华
                </button>
              </div>
            )}

            {s.playerLoc === 'GRAND_LIBRARY' && (
              <div className="flex flex-col gap-2 p-3 bg-theme-cyan/5 border border-theme-border">
                <div className="text-[10px] text-theme-cyan/80 uppercase font-bold text-center border-b border-theme-border/30 pb-1 mb-1">禁忌书架</div>
                {s.completedBooks.includes(20) ? (
                  <button disabled className="w-full h-[36px] text-[12px] border border-theme-cyan/20 text-theme-cyan/30 uppercase cursor-not-allowed">[ 已掌握 ] 《活体演化》</button>
                ) : (
                  <button disabled={(s.playerAttrs['INTELLIGENCE'] ?? 0) < 20}
                    onClick={() => handleRead(20)}
                    className={`w-full h-[36px] text-[12px] border border-theme-cyan uppercase transition-colors ${(s.playerAttrs['INTELLIGENCE'] ?? 0) >= 20 ? 'text-theme-cyan hover:bg-theme-cyan/10 cursor-pointer' : 'text-theme-cyan/30 border-theme-cyan/20 cursor-not-allowed'}`}>
                    {(s.playerAttrs['INTELLIGENCE'] ?? 0) >= 20 ? '[ 研读 ] 《活体演化》' : `需要智力 20.0 (${(s.playerAttrs['INTELLIGENCE'] ?? 0).toFixed(1)})`}
                  </button>
                )}
                {s.completedBooks.includes(50) ? (
                  <button disabled className="w-full h-[36px] text-[12px] border border-theme-cyan/20 text-theme-cyan/30 uppercase cursor-not-allowed">[ 已掌握 ] 《拉莱耶残卷》</button>
                ) : (
                  <button disabled={(s.playerAttrs['INTELLIGENCE'] ?? 0) < 50}
                    onClick={() => handleRead(50)}
                    className={`w-full h-[36px] text-[12px] border border-theme-cyan uppercase transition-colors ${(s.playerAttrs['INTELLIGENCE'] ?? 0) >= 50 ? 'text-theme-cyan hover:bg-theme-cyan/10 cursor-pointer' : 'text-theme-cyan/30 border-theme-cyan/20 cursor-not-allowed'}`}>
                    {(s.playerAttrs['INTELLIGENCE'] ?? 0) >= 50 ? '[ 研读 ] 《拉莱耶残卷》' : `需要智力 50.0 (${(s.playerAttrs['INTELLIGENCE'] ?? 0).toFixed(1)})`}
                  </button>
                )}
              </div>
            )}

            {s.playerLoc === 'OBSERVATORY' && (
              <div className="flex flex-col gap-2 p-3 bg-[#111] border border-[#555]">
                <div className="text-[10px] text-yellow-500/80 uppercase font-bold text-center border-b border-[#555]/50 pb-1 mb-1">星象仪占卜</div>
                <button disabled={s.timers.divinationCooldown > 0}
                  onClick={handleDivination}
                  className={`w-full h-[36px] text-[12px] border border-yellow-500 uppercase transition-colors ${s.timers.divinationCooldown <= 0 ? 'text-yellow-500 hover:bg-yellow-500/10 cursor-pointer' : 'text-yellow-500/30 border-yellow-500/20 cursor-not-allowed'}`}>
                  {s.timers.divinationCooldown > 0 ? `星象紊乱 [ ${Math.ceil(s.timers.divinationCooldown)}s ]` : '[ 凝注群星 ] 进行占卜'}
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </>
  );
};

// ============================================================
// Combat Panel
// ============================================================

const CombatSidebarPanel: React.FC<{ s: GameStateDTO; roomData: RoomData | null }> = ({ s, roomData }) => {
  const cd = s.combat;
  if (!cd) return null;
  const roomsMap = roomData?.roomsMap ?? {};
  const roomName = roomsMap[cd.roomId]?.name ?? cd.roomId;

  return (
    <div className="flex flex-col shrink-0 mb-4 pb-4 border-b border-theme-red/50 relative">
      <div className="absolute inset-0 bg-theme-red/5 animate-pulse rounded pointer-events-none" />
      <div className="text-[12px] text-theme-red font-bold uppercase border-b border-theme-red/40 pb-1 mb-3 shrink-0 flex items-center justify-between px-1 relative">
        <span className="flex items-center gap-1"><Swords size={16} /> 遭遇战分析模块</span>
        <span className="text-[10px] animate-pulse">处理中...</span>
      </div>
      <div className="text-[11px] text-theme-text mb-2 px-1 text-center bg-black/40 py-1 relative">
        交战区域：<span className="text-theme-red font-bold tracking-widest">[{roomName}]</span>
      </div>
      <div className="flex justify-between items-stretch text-[10px] mb-3 px-1 relative">
        <div className="flex flex-col flex-[2]">
          <span className="text-theme-cyan mb-1 font-bold border-b border-theme-cyan/30 text-center">您 (Player)</span>
          {cd.attrsCompared.map((a: string) => (
            <div key={`p-${a}`} className="flex justify-between mt-1 text-theme-text">
              <span>{a}</span>
              <span className="font-mono text-theme-cyan font-bold">{(cd.playerPreAttrs?.[a] ?? 0).toFixed(1)}</span>
            </div>
          ))}
          {cd.phase !== 'STARTING' && (
            <div className="flex justify-between mt-2 pt-1 border-t border-theme-cyan/50 text-theme-cyan font-bold text-[12px]">
              <span>合计</span>
              <span className="font-mono text-[14px]">{cd.playerSum.toFixed(1)}</span>
            </div>
          )}
        </div>
        <div className="flex flex-col items-center justify-center flex-1">
          <span className="text-theme-red font-bold text-[16px] italic">VS</span>
        </div>
        <div className="flex flex-col flex-[2] text-right">
          <span className="text-theme-red mb-1 font-bold border-b border-theme-red/30 text-center">敌意实体 (NPC)</span>
          {cd.attrsCompared.map((a: string) => (
            <div key={`n-${a}`} className="flex justify-between mt-1 text-theme-text">
              <span className="font-mono text-theme-red font-bold">{(cd.npcPreAttrs?.[a] ?? 0).toFixed(1)}</span>
              <span>{a}</span>
            </div>
          ))}
          {cd.phase !== 'STARTING' && (
            <div className="flex justify-between mt-2 pt-1 border-t border-theme-red/50 text-theme-red font-bold text-[12px]">
              <span className="font-mono text-[14px]">{cd.npcSum.toFixed(1)}</span>
              <span>合计</span>
            </div>
          )}
        </div>
      </div>
      {cd.phase === 'RESULT' && (
        <div className="bg-theme-bg/90 border border-theme-text/20 p-2 text-center text-[12px] font-bold mt-2 shadow-[0_0_10px_rgba(0,0,0,0.5)] relative">
          {cd.winner === 'player' ? (
            <div className="text-theme-cyan">
              {cd.isExecution ? '⭐ 目标被制裁拔除！' : '战胜目标！'}<br />
              <span className="text-[10px] font-normal leading-tight block mt-1">
                {cd.isExecution ? `已全功率吸收全部残余能量 ${cd.stealTotal.toFixed(1)} 点。` : `成功剥夺 ${cd.stealTotal.toFixed(1)} 点。`}
              </span>
            </div>
          ) : cd.winner === 'npc' ? (
            <div className="text-theme-red">
              战力不敌！<br />
              <span className="text-[10px] font-normal leading-tight block mt-1">被强行夺走 {cd.stealTotal.toFixed(1)} 点。</span>
            </div>
          ) : (
            <div className="text-[#8b949e]">
              势均力敌<br />
              <span className="text-[10px] font-normal leading-tight block mt-1">未能分出胜负。</span>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

// ============================================================
// NPC Panel
// ============================================================

const NpcStatePanel: React.FC<{ s: GameStateDTO; roomData: RoomData | null }> = ({ s, roomData }) => {
  const hp = calcHP(s.npcAttrs);
  const roomsMap = roomData?.roomsMap ?? {};
  const room = roomsMap[s.npcLoc];

  return (
    <div className="flex flex-col shrink-0 mb-4 pb-4 border-b border-theme-border/50">
      <div className="text-[12px] text-theme-red uppercase border-b border-theme-red/40 pb-1 mb-3 shrink-0 flex items-center justify-between">
        <span className="flex items-center gap-1"><Activity size={14} /> 敌对目标探测</span>
        {s.status === 'PLAYING' ? (<span className="text-[10px] animate-pulse">实时追踪</span>) : (<span className="text-[10px] opacity-50">信号丢失</span>)}
      </div>
      <div className="flex justify-between items-center bg-black border border-theme-red/30 p-2 mb-3 relative overflow-hidden">
        <div className="absolute left-0 top-0 h-full bg-[linear-gradient(90deg,var(--color-theme-red),transparent)] opacity-20 transition-all duration-200"
          style={{ width: `${Math.min(100, Math.max(0, hp))}%` }} />
        <span className="font-semibold text-theme-red text-[10px] z-10 relative">威胁总值 (HP)</span>
        <span className="text-[14px] font-bold z-10 relative text-theme-red">{hp.toFixed(1)}</span>
      </div>
      <div className="grid grid-cols-2 gap-2 mb-3">
        {ATTR_ORDER.map(key => (
          <div key={key} className="flex flex-col text-[11px] p-1.5 border border-theme-red/10 bg-[var(--color-theme-bg)]">
            <span className="text-theme-red/60 text-[9px] mb-[2px]">{ATTR_NAMES[key]}</span>
            <span className="text-theme-red font-bold font-mono">{(s.npcAttrs[key] ?? 0).toFixed(1)}</span>
          </div>
        ))}
      </div>
      <div className="text-[10px] text-theme-red/70 bg-theme-red/5 px-2 py-1.5 border-l-2 border-theme-red/40 flex justify-between items-center">
        <span>当前所在区域:</span>
        <span className="font-bold tracking-widest">{room?.name ?? s.npcLoc}</span>
      </div>
    </div>
  );
};

// ============================================================
// Logs Panel
// ============================================================

const LogsPanel: React.FC<{ s: GameStateDTO }> = ({ s }) => {
  const logsEndRef = React.useRef<HTMLDivElement>(null);
  React.useEffect(() => {
    logsEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [s.logs]);

  return (
    <>
      <div className="text-[12px] text-theme-cyan uppercase border-b border-theme-border pb-1 mb-3 shrink-0 flex items-center gap-2">通信日志</div>
      <div className="flex-1 overflow-y-auto pr-1 text-[12px] leading-[1.6] text-[#8b949e]">
        {s.logs.map((log, idx) => {
          const isDanger = log.includes('⚔️') || log.includes('💀') || log.includes('🔴');
          const isCombatSystem = log.includes('警告') || log.includes('遭遇战');
          let colorCls = 'border-theme-border', txtCls = 'text-[#8b949e]';
          if (isDanger || isCombatSystem) { colorCls = 'border-theme-red'; txtCls = 'text-theme-red'; }
          else if (log.includes('系统') || log.includes('NPC') || log.includes('跳跃')) { colorCls = 'border-theme-cyan'; txtCls = 'text-theme-cyan'; }
          return (
            <div key={idx} className={`mb-2 pl-[10px] border-l-2 ${colorCls} break-words`}>
              <span className={txtCls}>{log}</span>
            </div>
          );
        })}
        <div ref={logsEndRef} />
      </div>
    </>
  );
};

// ============================================================
// Reading Overlay
// ============================================================

const ReadingOverlay: React.FC<{ s: GameStateDTO }> = ({ s }) => {
  const rd = s.reading;
  if (!rd) return null;

  const handleWordClick = (wordId: number) => {
    const word = rd.words.find(w => w.id === wordId);
    if (word && word.isCorrupt) {
      playSound('read_click');
      gameApi.purifyWord(wordId).catch(console.error);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/90 p-4 font-mono">
      <div className="absolute inset-0 bg-[#0a0000] opacity-30 pointer-events-none" />
      <div className="relative w-full max-w-2xl bg-[#0f0a05] border-2 border-[#3d2b1f] p-8 sm:p-12 shadow-[0_0_50px_rgba(0,0,0,1)] overflow-hidden">
        <div className="absolute top-0 left-0 w-full h-1 bg-theme-border/20">
          <div className="bg-theme-cyan h-full transition-all duration-300" style={{ width: `${(rd.timer / 10) * 100}%` }} />
        </div>
        <div className="absolute top-2 right-4 text-[10px] text-theme-red uppercase tracking-widest font-bold">
          腐蚀度: {rd.corruption.toFixed(0)}%
        </div>
        <div className="absolute top-1 right-0 w-full h-[2px] bg-theme-red/10 overflow-hidden">
          <div className="bg-theme-red h-full transition-all duration-100" style={{ width: `${rd.corruption}%` }} />
        </div>
        <div className="text-[12px] text-[#4d3a2b] uppercase mb-8 border-b border-[#3d2b1f] pb-2 flex justify-between items-center">
          <span>{rd.bookType === 50 ? '禁断典籍: 《拉莱耶残卷》' : '生体导论: 《活体演化》'}</span>
          <span className="text-theme-cyan animate-pulse">智力等级: {rd.bookType}</span>
        </div>
        <div className="relative leading-[1.8] text-[16px] sm:text-[18px] text-[#a08b7a] flex flex-wrap gap-x-2 gap-y-1 select-none">
          {rd.words.map((w) => (
            <span key={w.id} onClick={() => handleWordClick(w.id)}
              className={`transition-all duration-200 cursor-pointer relative ${w.isCorrupt ? 'text-theme-red font-bold animate-[pulse_0.4s_infinite]' : 'hover:text-theme-cyan'}`}
              style={{
                display: 'inline-block',
                transform: w.isCorrupt ? `translate(${Math.random() * 4 - 2}px, ${Math.random() * 4 - 2}px) rotate(${w.rot}deg)` : 'none',
                filter: w.isCorrupt ? `blur(${Math.random() * 2}px)` : 'none',
              }}>
              {w.isCorrupt ? 'ERR_CORRUPT' : w.text}
            </span>
          ))}
        </div>
        <div className="mt-12 text-center text-[11px] text-[#4d3a2b] uppercase tracking-[4px]">
          在文字崩溃前点击它们执行 [ 净化 ]
        </div>
      </div>
    </div>
  );
};

// ============================================================
// Divination Overlay
// ============================================================

const DivinationOverlay: React.FC<{ s: GameStateDTO }> = ({ s }) => {
  if (s.status !== 'DIVINATION' || !s.divination) return null;
  const res = s.divination;
  const isRevealed = res.timer > 1.0;
  const isTower = res.card === 'TOWER';
  const colorPrimary = isTower ? '#ef4444' : '#eab308';

  return (
    <div className="fixed inset-0 z-[100] flex flex-col items-center justify-center font-mono bg-black/95 overflow-hidden">
      {isRevealed && Array.from({ length: 40 }).map((_, i) => {
        const angle = Math.random() * Math.PI * 2;
        const dist = 100 + Math.random() * (isTower ? 500 : 300);
        return (
          <motion.div key={i}
            initial={{ x: 0, y: 0, opacity: 1, scale: 1 }}
            animate={{ x: Math.cos(angle) * dist, y: Math.sin(angle) * dist, opacity: 0, scale: Math.random() * 2 }}
            transition={{ duration: 1 + Math.random() * 1.5, ease: 'easeOut' }}
            className="absolute w-2 h-2 rounded-full z-10"
            style={{ backgroundColor: colorPrimary, top: '50%', left: '50%', marginTop: '-4px', marginLeft: '-4px' }} />
        );
      })}
      <div className="relative text-center flex flex-col items-center w-full max-w-sm z-20">
        <motion.div initial={{ opacity: 0, y: -20 }} animate={{ opacity: 1, y: 0 }}
          className="text-[14px] sm:text-[18px] mb-8 tracking-[1rem] uppercase animate-[pulse_2s_ease-out_infinite]"
          style={{ color: colorPrimary }}>星象剥落</motion.div>
        <motion.div
          initial={{ rotateY: 180, scale: 0 }}
          animate={{ rotateY: isRevealed ? 0 : 180, scale: isRevealed ? 1.1 : 0.9 }}
          transition={{ duration: 1.0, type: 'spring', bounce: 0.4 }}
          className="w-[180px] sm:w-[220px] aspect-[2/3] border-2 relative flex flex-col items-center justify-center"
          style={{
            transformStyle: 'preserve-3d',
            borderColor: isRevealed ? colorPrimary : '#333',
            boxShadow: isRevealed ? `0 0 80px ${colorPrimary}40` : 'none',
            background: isRevealed
              ? (isTower ? 'linear-gradient(to bottom, #2a0000, black)' : 'linear-gradient(to bottom, #1a1400, black)')
              : '#111'
          }}>
          <div className="absolute inset-0 flex flex-col items-center justify-center p-4 transition-opacity duration-300"
            style={{ backfaceVisibility: 'hidden', transform: 'rotateY(0deg)', opacity: isRevealed ? 1 : 0 }}>
            <h1 className="text-[28px] sm:text-[36px] font-bold tracking-widest leading-[1.2]"
              style={{ writingMode: 'vertical-rl', textOrientation: 'upright', color: colorPrimary }}>
              {TAROT_NAMES[res.card] ?? res.card}
            </h1>
          </div>
          <div className="absolute inset-0 bg-[#111] flex items-center justify-center transition-opacity duration-300"
            style={{ backfaceVisibility: 'hidden', transform: 'rotateY(180deg)', opacity: isRevealed ? 0 : 1 }}>
            <div className="w-[60px] h-[60px] border border-white/20 rounded-full flex flex-col items-center justify-center relative">
              <div className="absolute inset-[10px] border border-white/10 rotate-45" />
              <div className="absolute inset-[10px] border border-white/10" />
              <div className="w-2 h-2 rounded-full bg-white/30 animate-pulse" />
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
};

// ============================================================
// Warning Overlay
// ============================================================

const WarningOverlay: React.FC<{ s: GameStateDTO }> = ({ s }) => (
  <AnimatePresence>
    {s.timers.showWarning > 0 && (
      <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0, transition: { duration: 1.5 } }}
        className="fixed inset-0 z-[200] flex items-center justify-center bg-black pointer-events-none">
        <motion.div
          initial={{ opacity: 0, scale: 0.8, filter: 'blur(10px)' }}
          animate={{ opacity: 1, scale: 1, filter: 'blur(0px)' }}
          exit={{ opacity: 0, scale: 1.1, filter: 'blur(20px)' }}
          transition={{ duration: 2.0, ease: 'easeOut' }}
          className="text-theme-red font-bold text-center tracking-[0.5rem] sm:tracking-[1.5rem] text-[20px] sm:text-[32px]"
          style={{ textShadow: '0 0 30px rgba(255,0,0,1)' }}>
          入此门者<br /><br />当舍弃一切希望
        </motion.div>
      </motion.div>
    )}
  </AnimatePresence>
);

// ============================================================
// Main App
// ============================================================

export default function App() {
  const { gameState, connected } = useGameWebSocket();
  const [phase, setPhase] = useState<'setup' | 'playing' | 'gameover'>('setup');
  const [roomData, setRoomData] = useState<RoomData | null>(null);

  // Fetch room map data from backend on mount
  useEffect(() => {
    gameApi.getRooms().then(data => {
      const roomsMap: Record<string, RoomDTO> = {};
      for (const r of data.rooms) {
        roomsMap[r.id] = r;
      }
      setRoomData({ roomsMap, edges: data.edges });
    }).catch(console.error);
  }, []);

  const handleStart = useCallback(async (attrs: Record<string, number>) => {
    try {
      playSound('start');
      await gameApi.startGame(attrs);
      setPhase('playing');
    } catch (e) {
      console.error('Failed to start game:', e);
    }
  }, []);

  // Detect gameover from WebSocket state
  React.useEffect(() => {
    if (gameState?.status === 'GAMEOVER') setPhase('gameover');
  }, [gameState?.status]);

  // Sound effects based on state changes (simple version)
  React.useEffect(() => {
    if (!gameState) return;
    if (gameState.status === 'COMBAT') playSound('start');
    if (gameState.status === 'DIVINATION' && gameState.divination?.card === 'TOWER') playSound('tower');
    if (gameState.status === 'DIVINATION' && gameState.divination?.card !== 'TOWER') playSound('reveal');
  }, [gameState?.status, gameState?.combat?.phase]);

  // Setup phase
  if (phase === 'setup') {
    return (
      <>
        <SetupScreen onStart={handleStart} />
        {!connected && (
          <div className="fixed bottom-4 right-4 bg-yellow-500/20 border border-yellow-500 text-yellow-500 text-[10px] px-3 py-1 animate-pulse font-mono">
            等待后端连接...
          </div>
        )}
      </>
    );
  }

  // No game state yet
  if (!gameState) {
    return (
      <div className="flex items-center justify-center min-h-screen font-mono text-theme-cyan">
        <div className="text-center">
          <div className="animate-pulse text-[24px] mb-4">连接中...</div>
          <div className="text-[12px] text-[#8b949e]">
            {connected ? '正在同步游戏状态' : '正在连接游戏服务器'}
          </div>
        </div>
      </div>
    );
  }

  const s = gameState;

  // Gameover screen
  if (phase === 'gameover' || s.status === 'GAMEOVER') {
    const pHP = calcHP(s.playerAttrs);
    const won = pHP > 0;
    const titleText = won ? 'TERMINATED_NPC' : 'TERMINATED';
    const descText = won ? '系统已接管洋馆。生存任务达成。' : '生命维持系统崩溃。游戏结束。';

    return (
      <div className="flex flex-col items-center justify-center min-h-screen p-6 font-mono">
        <div className="max-w-md w-full bg-[#1a0000] border border-theme-red/50 p-8 shadow-[0_0_20px_rgba(255,0,0,0.2)] text-center relative overflow-hidden">
          {won ? (
            <div className="absolute inset-0 bg-[linear-gradient(45deg,transparent_20%,var(--color-theme-cyan)_50%,transparent_80%)] opacity-[0.03] pointer-events-none" />
          ) : (
            <div className="absolute inset-0 bg-[repeating-linear-gradient(45deg,transparent,transparent_10px,var(--color-theme-red)_10px,var(--color-theme-red)_20px)] opacity-[0.03] pointer-events-none" />
          )}
          <h1 className={`text-[40px] sm:text-[48px] mb-6 tracking-[4px] sm:tracking-[8px] uppercase font-bold relative z-10 ${won ? 'text-theme-cyan' : 'text-theme-red'}`}>
            {titleText}
          </h1>
          <p className="text-[#8b949e] mb-8 text-[14px] relative z-10">{descText}</p>
          <button onClick={() => { setPhase('setup'); }}
            className={`w-full bg-transparent border uppercase text-[12px] py-3 transition cursor-pointer font-bold relative z-10 ${won ? 'border-theme-cyan text-theme-cyan hover:bg-theme-cyan/10' : 'border-theme-red text-theme-red hover:bg-theme-red/10'}`}>
            重启终端
          </button>
        </div>
      </div>
    );
  }

  const currentHP = calcHP(s.playerAttrs);

  return (
    <div className="min-h-screen lg:h-screen bg-theme-bg p-2 sm:p-3 font-mono antialiased text-theme-text flex flex-col overflow-y-auto lg:overflow-hidden">
      {/* Header */}
      <header className="bg-theme-card border border-theme-border flex items-center justify-between px-3 sm:px-5 py-0 h-[60px] shadow-[0_0_20px_rgba(0,0,0,0.5)] shrink-0 mb-[12px]">
        <div className="flex items-center gap-3">
          <div className="text-[18px] sm:text-[24px] font-bold tracking-[2px] sm:tracking-[4px] text-theme-cyan uppercase">ENCOUNTER</div>
          {s.timers.invisibility > 0 && (
            <div className="flex bg-cyan-900/50 border border-theme-cyan text-theme-cyan text-[10px] px-2 py-0.5 animate-pulse items-center gap-1 shadow-[0_0_10px_var(--color-theme-cyan)_inset]">
              ✨ 虚无状态 (隐身) {Math.ceil(s.timers.invisibility)}s
            </div>
          )}
          {s.beast.state === 'CONTAINED' && s.beast.satiety < 30 && (
            <div className="hidden sm:flex bg-purple-900/50 border border-purple-500 text-purple-200 text-[10px] px-2 py-0.5 animate-pulse items-center gap-1 shadow-[0_0_10px_purple_inset]">
              📢 震耳嘶吼 (-50% 注意力)
            </div>
          )}
        </div>
        <div className="flex items-center gap-[15px]">
          <span className="text-[12px] hidden sm:block">生命体征</span>
          <div className="w-[120px] sm:w-[300px] md:w-[400px] h-[24px] bg-black border border-theme-border relative">
            <div className="h-full bg-[linear-gradient(90deg,var(--color-theme-red),#ff9999)] transition-all ease-out duration-300"
              style={{ width: `${Math.min(100, currentHP)}%` }} />
            <div className="absolute inset-0 w-full text-center text-[12px] leading-[24px] text-white pointer-events-none">
              {currentHP.toFixed(1)} / 100
            </div>
          </div>
        </div>
      </header>

      {/* Main Grid */}
      <div className="flex-1 lg:grid lg:grid-cols-[320px_1fr_280px] gap-[12px] relative w-full flex flex-col lg:min-h-0 lg:overflow-hidden pb-4 lg:pb-0">
        {/* Left: Player State */}
        <aside className="bg-theme-card border border-theme-border p-3 sm:p-4 flex flex-col relative min-h-[420px] lg:h-full lg:overflow-hidden shrink-0">
          <PlayerStatePanel s={s} />
        </aside>

        {/* Center: Map */}
        <main className="bg-[radial-gradient(circle_at_center,#1a202c_0%,#0a0b10_100%)] border border-theme-border p-3 sm:p-4 flex flex-col min-h-[500px] lg:h-full lg:overflow-y-auto shrink-0">
          <MapPanel s={s} roomData={roomData} />
        </main>

        {/* Overlays */}
        <ReadingOverlay s={s} />
        <DivinationOverlay s={s} />
        <WarningOverlay s={s} />

        {/* Right: NPC/Combat + Logs */}
        <aside className="bg-theme-card border border-theme-border p-3 sm:p-4 flex flex-col min-h-[450px] lg:h-full lg:overflow-hidden shrink-0 relative">
          {s.status === 'COMBAT' ? (
            <CombatSidebarPanel s={s} roomData={roomData} />
          ) : (
            <NpcStatePanel s={s} roomData={roomData} />
          )}
          <LogsPanel s={s} />
        </aside>
      </div>
    </div>
  );
}
