import {useState} from "react";

interface useFetchReturn {
    data: paymentResponse | null;
    loading: boolean;
    error: string | null;
    fetchPayment: (id: string, first_name: string, last_name: string, expiration: string) => Promise<void>;
}

interface paymentResponse {
    status: string;
}

export const useFetchPayment = () : useFetchReturn => {
    // State Variables
    const [data, setData] = useState<paymentResponse | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/payment`;

    const fetchPayment = async(id: string, first_name: string,
                               last_name: string, expiration: string) => {
        const postRequest: RequestInit = { // defines how fetch will send
            method: 'POST',
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, first_name, last_name, expiration }),
        };

        try {
            setLoading(true);
            setError(null);

            console.log("Sent fields: ", id, first_name, last_name, expiration);

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

    return { data, loading, error, fetchPayment };
};