import { useState, useEffect, useCallback } from 'react';
import type {Movie} from "@/types/types";

interface useFetchReturn {
    data: Movie[] | null;
    loading: boolean;
    error: string | null;
    searchMovies: (titleQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => Promise<void>;
}

export const useFetchMovieList = () : useFetchReturn => {
    // State variables
    const [data, setData] = useState<Movie[] | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Data Fetching
    const fetchMovie = async (titleQuery: string = '', starQuery: string = '', directorQuery: string = '', yearQuery: string = '') => {
        setLoading(true);
        setError(null);
        try{
            const BASE_URL = 'http://localhost:8080/';
            // Build query params
            const params = new URLSearchParams();
            if (titleQuery.trim()) {
                params.append('title', titleQuery.trim());
            }
            if (starQuery.trim()) {
                params.append('star', starQuery.trim());
            }
            if (directorQuery.trim()) {
                params.append('director', directorQuery.trim());
            }
            if (yearQuery.trim()) {
                params.append('year', yearQuery.trim());
            }
            const url = params.toString() ? `${BASE_URL}?${params.toString()}` : BASE_URL;
            
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
    const searchMovies = useCallback(async (titleQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        await fetchMovie(titleQuery, starQuery, directorQuery, yearQuery);
    }, []);

    // Run & Return States
    useEffect(() => {
        fetchMovie();
    }, []); // no argument, as this page is static

    return { data, loading, error, searchMovies };
}