import {useState} from "react";

interface useFetchReturn {
    data: loginResponse | null;
    loading: boolean;
    error: string | null;
    fetchLogin: (email: string, password: string, recaptchaToken: string | null) => Promise<void>;
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

    const fetchLogin = async(email: string, password: string, recaptchaToken: string | null) => {
        const postRequest: RequestInit = { // defines how fetch will send
            method: 'POST',
            credentials: "include",
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, "g-recaptcha-response": recaptchaToken }),
        };

        try {
            setLoading(true);
            setError(null);

            const response = await fetch(BASE_URL, postRequest);

            const jsonResponse = await response.json();

            // Check if login failed
            if (jsonResponse.status === "failure") {
                setError("Invalid credentials. Please check your email and password.");
                setData(jsonResponse);
            } else {
                setData(jsonResponse);
            }
        } catch (err: any) {
            console.error("Login error:", err);
            setError("An unexpected error occurred. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, fetchLogin };
};
