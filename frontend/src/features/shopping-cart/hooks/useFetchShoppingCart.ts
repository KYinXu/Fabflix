import {useState} from "react";

interface Cart {
    movieId: string;
    title: string;
    price: number;
    quantity: number;
}

interface useFetchReturn {
    data: any | null;
    loading: boolean;
    error: string | null;
    fetchShoppingCart: (cart?: Cart[] | Cart) => Promise<void>;
}

export const useFetchShoppingCart = (): useFetchReturn => {
    const [data, setData] = useState<any> (null);
    const [loading, setLoading] = useState<boolean> (false);
    const [error, setError] = useState<string | null> (null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/cart`;


    const fetchShoppingCart = async () => {
        const getRequest = {
            method: 'GET',
            credentials: 'include',
            };

        try {
            setLoading(true);
            setError(null);

            // @ts-ignore
            const response = await fetch(BASE_URL, getRequest);

            if (!response.ok) throw new Error(`Server error: ${response.status}`);


            const jsonResponse = await response.json();
            setData(jsonResponse);
        } catch (err: any) {
            console.error("Login error:", err);
            setError(err.message || "Unexpected error");
        } finally {
            setLoading(false);
        }
    };
    return { data, loading, error, fetchShoppingCart };
};