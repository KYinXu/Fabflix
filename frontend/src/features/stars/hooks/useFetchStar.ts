import { useState, useEffect } from 'react';
import { Star } from '../types/star';

interface UseFetchStarReturn {
  data: Star | null;
  loading: boolean;
  error: string | null;
  refetch: () => void;
}

export const useFetchStar = (starId: string): UseFetchStarReturn => {
  const [data, setData] = useState<Star | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchStar = async () => {
      console.log(starId);
    try {
      setLoading(true);
      setError(null);
      const BASE_URL = 'http://localhost:8080';
      const res = await fetch(`${BASE_URL}/star/${starId}`);
      
      if (!res.ok) {
        throw new Error(`Failed to fetch star: ${res.status} ${res.statusText}`);
      }
      
      const starData = await res.json();
      setData(starData);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setData(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (starId) {
      fetchStar();
    }
  }, [starId]);

  const refetch = () => {
    if (starId) {
      fetchStar();
    }
  };

  return { data, loading, error, refetch };
};