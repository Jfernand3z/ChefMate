const KEY = 'cm_token_expires_at';
const REFRESH_THRESHOLD_MS = 60_000; // refresh if < 60s remaining

export const TokenManager = {
    setExpiry(accessTokenExpiresInMinutes: number) {
        const expiresAt = Date.now() + accessTokenExpiresInMinutes * 60 * 1000;
        localStorage.setItem(KEY, String(expiresAt));
    },

    clear() {
        localStorage.removeItem(KEY);
    },

    isExpiringSoon(): boolean {
        const raw = localStorage.getItem(KEY);
        if (!raw) return false;
        const expiresAt = Number(raw);
        return Date.now() >= expiresAt - REFRESH_THRESHOLD_MS;
    },

    hasSession(): boolean {
        const raw = localStorage.getItem(KEY);
        if (!raw) return false;
        return Number(raw) > Date.now();
    },
};
