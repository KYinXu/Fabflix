import React, {useEffect, useState} from "react";
import {useFetchPayment} from "../hooks/useFetchPayment";
import {updateDatabaseOnPurchase} from "../components/updateDatabaseOnPurchase"

const Payment: React.FC = () => {
    const { data, loading, error, fetchPayment } = useFetchPayment();
    const [id, setId] = useState("");
    const [first_name, setFirstName] = useState("");
    const [last_name, setLastName] = useState("");
    const [expiration, setExpiration] = useState("");

    useEffect(() => {
        if (data && data.status === "success"){
            updateDatabaseOnPurchase(id);
        }
    }, [data]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!id || !first_name || !last_name || !expiration) {
            alert("Please fill in all fields.");
            return;
        }
        await fetchPayment(id, first_name, last_name, expiration);
    };

    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
            <div className="bg-white p-8 rounded-xl shadow-md w-96">
                <h1 className="text-2xl font-bold text-center mb-6 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 text-transparent bg-clip-text">
                    Payment
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-gray-700">Credit Card ID</label>
                        <input
                            type="id"
                            value={id}
                            onChange={(e) => setId(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">First Name</label>
                        <input
                            type="first_name"
                            value={first_name}
                            onChange={(e) => setFirstName(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <label className="block text-gray-700">Last Name</label>
                        <input
                            type="last_name"
                            value={last_name}
                            onChange={(e) => setLastName(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <div>
                        <input
                            type="date"
                            value={expiration}
                            onChange={(e) => setExpiration(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                    >
                        {loading ? "Confirming Payment..." : "Place Order"}
                    </button>
                </form>

                {error && <p className="text-red-500 text-center mt-4">{error}</p>}
                {data && data.status === "success" && (
                    <p className="text-green-600 text-center mt-4">Payment Successful!</p>
                )}
                {data && data.status !== "success" && (
                    <p className="text-red-500 text-center mt-4">Payment Failed!</p>
                )}
            </div>
        </div>
    );
};

export default Payment;
