import { useState } from 'react';
import { SessionState } from '@/types/session';

interface UseSessionStateReturn {
    saveState: (state: SessionState) => Promise<void>;
    loadState: () => Promise<SessionState | null>;
    loading: boolean;
    error: string | null;
}

export const useSessionState = (): UseSessionStateReturn => {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const BASE_URL = import.meta.env.VITE_BACKEND_URL;

    const saveState = async (state: SessionState) => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${BASE_URL}/api/session-state`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(state),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to save state');
            }
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An unknown error occurred during save');
        } finally {
            setLoading(false);
        }
    };

    const loadState = async (): Promise<SessionState | null> => {
        setLoading(true);
        setError(null);
        try {
            const response = await fetch(`${BASE_URL}/api/session-state`, {
                method: 'GET',
                credentials: 'include',
            });

            if (response.status === 401) {
                // Unauthorized, session likely expired or not logged in
                return null;
            }

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to load state');
            }

            const data = await response.json();
            return data.state as SessionState;
        } catch (err) {
            setError(err instanceof Error ? err.message : 'An unknown error occurred during load');
            return null;
        } finally {
            setLoading(false);
        }
    };

    return { saveState, loadState, loading, error };
};
