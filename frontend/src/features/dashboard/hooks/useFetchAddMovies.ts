import { useState } from "react";

interface useFetchReturn {
    data: addMovieResponse | null;
    loading: boolean;
    error: string | null;
    fetchAddMovie: (title: string, year: string, director: string, star: string, genre: string) => Promise<void>;
}

interface addMovieResponse {
    status: string;
}

export const useFetchAddMovie = (): useFetchReturn => {
    const [data, setData] = useState<addMovieResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/add-movie`;

    const fetchAddMovie = async (title: string, year: string, director: string, star: string, genre: string) => {
        const postRequest: RequestInit = {
            method: 'POST',
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, year, director, star, genre }),
        };

        try {
            setLoading(true);
            setError(null);
            const response = await fetch(BASE_URL, postRequest);
            const jsonResponse = await response.json();

            if (jsonResponse.status === "failure") {
                setError("Failed to add movie. Please check input values.");
            }
            setData(jsonResponse);
        } catch (err: any) {
            console.error("Add Movie Error:", err);
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, fetchAddMovie };
};
