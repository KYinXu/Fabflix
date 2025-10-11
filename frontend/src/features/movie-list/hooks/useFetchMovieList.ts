import { useState, useEffect } from 'react';
import type {Movie} from "@/types/types";

interface useFetchReturn {
    data: Movie[] | null;
    loading: boolean;
    error: string | null;
}

export const useFetchMovieList = () : useFetchReturn => {
    // State variables
    const [data, setData] = useState<Movie[] | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    // Data Fetching
    const fetchMovie = async () => {
        try{
            const BASE_URL = 'http://localhost:8080/2025_fall_cs_122b_untitled_group_name_war/';
            const fetching = await fetch(BASE_URL)
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

    // Run & Return States
    useEffect(() => {
        fetchMovie();
    }, []); // no argument, as this page is static

    return { data, loading, error };
}