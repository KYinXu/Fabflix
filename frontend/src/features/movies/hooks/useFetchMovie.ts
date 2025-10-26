import { useState, useEffect } from 'react';
import type { Movie } from "@/types/types";

interface UseFetchReturn {
  data: Movie | null;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

export const useFetchMovie = (movieId: string): UseFetchReturn => {
  const [data, setData] = useState<Movie | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchMovie = async () => {
    try {
      setLoading(true);
      setError(null);
      const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}`;
      const res = await fetch(`${BASE_URL}/movie/${movieId}`, {
        credentials: 'include'
      });
      
      if (!res.ok) {
        //throw new Error(`Failed to fetch movie: ${res.status} ${res.statusText}`);
      }
      
      const movieData = await res.json();
      setData(movieData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (movieId) {
      fetchMovie();
    }
  }, [movieId]);

  const refetch = () => {
    if (movieId) {
      fetchMovie();
    }
  };

  return { data, loading, error, refetch };
};