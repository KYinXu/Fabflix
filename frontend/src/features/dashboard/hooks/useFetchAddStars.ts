import { useState } from "react";

interface useFetchReturn {
    data: addStarResponse | null;
    loading: boolean;
    error: string | null;
    fetchAddStar: (name: string, birthYear: string | null) => Promise<void>;
}

interface addStarResponse {
    status: string;
}

export const useFetchAddStar = (): useFetchReturn => {
    const [data, setData] = useState<addStarResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/add-star`;

    const fetchAddStar = async (name: string, birthYear: string | null) => {
        const postRequest: RequestInit = {
            method: 'POST',
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, birth_year: birthYear }),
        };

        try {
            setLoading(true);
            setError(null);
            const response = await fetch(BASE_URL, postRequest);
            const jsonResponse = await response.json();

            if (jsonResponse.status === "failure") {
                setError("Failed to add star. Please check input values.");
            }
            setData(jsonResponse);
        } catch (err: any) {
            console.error("Add Star Error:", err);
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, fetchAddStar };
};
