import {useState} from "react";

interface useFetchReturn {
    data: loginResponse | null;
    loading: boolean;
    error: string | null;
    fetchLogin: (email: string, password: string) => Promise<void>;
}

interface loginResponse {
    status: string;
}

export const useFetchLogin = () : useFetchReturn => {
    // State Variables
    const [data, setData] = useState<loginResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/login`;

    const fetchLogin = async(email: string, password: string) => {
        const postRequest: RequestInit = { // defines how fetch will send
            method: 'POST',
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        };

        try {
            setLoading(true);
            setError(null);


            const response = await fetch(BASE_URL, postRequest);

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

    return { data, loading, error, fetchLogin };
};
