import { useState } from "react";

interface Column {
    name: string;
    type: string;
}

interface Table {
    name: string;
    columns: Column[];
}

interface MetadataResponse {
    tables: Table[];
}

export const useFetchMetadata = () => {
    const [data, setData] = useState<MetadataResponse | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/metadata`;

    const fetchMetadata = async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch(BASE_URL, {
                method: "GET",
                credentials: "include",
            });

            if (!response.ok) throw new Error("Failed to fetch metadata");

            const json = await response.json();
            setData(json);
        } catch (err: any) {
            console.error("Metadata fetch error:", err);
            setError("Unable to fetch metadata. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return { data, loading, error, fetchMetadata };
};
