import { useState, useEffect, useRef } from 'react';

export interface AutocompleteMovie {
    id: string;
    title: string;
    year: number;
}

export const useAutocompleteMovieSearch = (query: string) => {
    const [suggestions, setSuggestions] = useState<AutocompleteMovie[]>([]);
    const [loading, setLoading] = useState(false);
    const timeoutRef = useRef<NodeJS.Timeout>();

    useEffect(() => {
        if (timeoutRef.current) clearTimeout(timeoutRef.current);
        
        const trimmedQuery = query.trim();
        
        if (!trimmedQuery || trimmedQuery.length <= 3) {
            setSuggestions([]);
            setLoading(false);
            return;
        }

        timeoutRef.current = setTimeout(async () => {
            setLoading(true);
            try {
                const url = `${import.meta.env.VITE_BACKEND_URL}/autocomplete-movie-search?title=${encodeURIComponent(trimmedQuery)}`;
                const res = await fetch(url, { credentials: 'include' });
                const data = await res.json();
                setSuggestions(data || []);
            } catch (err) {
                setSuggestions([]);
            } finally {
                setLoading(false);
            }
        }, 300);

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [query]);

    return { suggestions, loading };
};

