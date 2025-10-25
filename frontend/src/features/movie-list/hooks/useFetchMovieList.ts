import { useState, useEffect, useCallback } from 'react';
import type {Movie} from "@/types/types";

interface useFetchReturn {
    data: Movie[] | null;
    loading: boolean;
    error: string | null;
    searchMovies: (titleQuery: string) => Promise<void>;
}

export const useFetchMovieList = () : useFetchReturn => {
    // State variables
    const [data, setData] = useState<Movie[] | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Data Fetching
    const fetchMovie = async (titleQuery: string = '') => {
        setLoading(true);
        setError(null);
        try{
            const BASE_URL = 'http://localhost:8080/';
            // Add title parameter if provided
            const url = titleQuery.trim()
                ? `${BASE_URL}?title=${encodeURIComponent(titleQuery.trim())}`
                : BASE_URL;
            
            const fetching = await fetch(url)
            const fetchedData = await fetching.json() // converts to JSON
            console.log(fetchedData) // logs JSON data
            setData(fetchedData);
        }
        catch (error){ // error handling for crashes
            console.log(error)
            const message =
                error instanceof Error ? error.message : "An error has occurred";
            setError(message);
        }

        finally {
            setLoading(false)
        }
    }

    // Search function for parent component
    const searchMovies = useCallback(async (titleQuery: string) => {
        await fetchMovie(titleQuery);
    }, []);

    // Run & Return States
    useEffect(() => {
        fetchMovie();
    }, []); // no argument, as this page is static

    return { data, loading, error, searchMovies };
}