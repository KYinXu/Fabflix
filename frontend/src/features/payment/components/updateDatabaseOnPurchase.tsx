
export const updateDatabaseOnPurchase = async (id: string) => {

    const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/payment`;

    const postRequest: RequestInit = { // defines how fetch will send
        method: 'POST',
        credentials: "include",
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            id,
            first_name: "",
            last_name: "",
            expiration: "2030-01-01"
        })
    };

    try {
        const response = await fetch(BASE_URL, postRequest);

        if (!response.ok) throw new Error(`Server error: ${response.status}`);

        const jsonResponse = await response.json();
        console.log("Database update response:", jsonResponse);
    } catch (err) {
        console.error("Error updating database:", err);
    }
};