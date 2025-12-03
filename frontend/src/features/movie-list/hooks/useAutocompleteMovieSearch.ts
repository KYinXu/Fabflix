import { useState, useEffect, useRef } from 'react';

export interface AutocompleteMovie {
    id: string;
    title: string;
    year: number;
}

const CACHE_PREFIX = 'autocomplete_cache_';
const getCacheKey = (query: string) => `${CACHE_PREFIX}${query.toLowerCase().trim()}`;

const getCachedResults = (query: string): AutocompleteMovie[] | null => {
    try {
        const cacheKey = getCacheKey(query);
        const cached = sessionStorage.getItem(cacheKey);
        if (cached) {
            return JSON.parse(cached);
        }
    } catch (err) {
        console.error('Error reading from sessionStorage:', err);
    }
    return null;
};

const setCachedResults = (query: string, results: AutocompleteMovie[]) => {
    try {
        const cacheKey = getCacheKey(query);
        sessionStorage.setItem(cacheKey, JSON.stringify(results));
    } catch (err) {
        console.error('Error writing to sessionStorage:', err);
    }
};

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

        // Check cache first
        const cachedResults = getCachedResults(trimmedQuery);
        if (cachedResults) {
            setSuggestions(cachedResults);
            setLoading(false);
            return;
        }

        timeoutRef.current = setTimeout(async () => {
            setLoading(true);
            try {
                const url = `${import.meta.env.VITE_BACKEND_URL}/autocomplete-movie-search?title=${encodeURIComponent(trimmedQuery)}`;
                const res = await fetch(url, { credentials: 'include' });
                const data = await res.json();
                const results = data || [];
                setSuggestions(results);
                // Cache the results
                setCachedResults(trimmedQuery, results);
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

