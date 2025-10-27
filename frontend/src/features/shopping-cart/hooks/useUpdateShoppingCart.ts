import {useState} from "react";

export const useUpdateShoppingCart = () => {
    const [data, setData] = useState<any> (null);
    const [loading, setLoading] = useState<boolean> (false);
    const [error, setError] = useState<string | null> (null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/cart`;

    const removeItem = async (movieId: string) => {
        try {
            setLoading(true);
            await fetch(BASE_URL, {
                method: "DELETE",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieId }),
            });
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const updateItem = async (movieId: string, quantity: number) => {
        try {
            setLoading(true);
            const response = await fetch(BASE_URL, {
                method: "PUT",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ movieId, quantity }),
            });
            if (!response.ok) throw new Error(`Server error: ${response.status}`);
            const json = await response.json();
            setData(json);
        } catch (err: any) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, updateItem, removeItem };

}