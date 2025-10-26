import { useState, useEffect } from 'react';
import type { Genre } from "@/types/types";

interface useFetchGenresReturn {
    data: Genre[] | null;
    loading: boolean;
    error: string | null;
}

export const useFetchGenres = (): useFetchGenresReturn => {
    const [data, setData] = useState<Genre[] | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const fetchGenres = async () => {
            setLoading(true);
            setError(null);
            
            try {
                const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/?action=listGenres`;
                const response = await fetch(BASE_URL);
                const fetchedData = await response.json();
                console.log('Genres fetched:', fetchedData);
                setData(fetchedData);
            } catch (err) {
                console.error(err);
                const message =
                    err instanceof Error ? err.message : "An error has occurred";
                setError(message);
            } finally {
                setLoading(false);
            }
        };

        fetchGenres();
    }, []);

    return { data, loading, error };
};
