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
    const BASE_URL = 'http://localhost:8080/fabflix_war/login';

    const fetchLogin = async(email: string, password: string) => {
        const postRequest = { // defines how fetch will send
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        };

        try {
            setLoading(true);
            setError(null);

            console.log("Sent fields: ", email, password);

            const response = await fetch(BASE_URL, postRequest);
            console.log("PR: ", response);

            if (!response.ok) throw new Error(`Server error: ${response.status}`);

            console.log("POST received by backend");

            const jsonResponse = await response.json();
            console.log("JSON Response:", jsonResponse);

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
