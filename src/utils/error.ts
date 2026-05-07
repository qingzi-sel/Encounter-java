export function reportError(err: unknown) {
  const message = err instanceof Error ? err.message : String(err);
  const payload = message || '发生未知错误';
  window.dispatchEvent(new CustomEvent('encounterAppError', { detail: payload }));
  console.error(err);
}
