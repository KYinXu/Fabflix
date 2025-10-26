import React, {useEffect, useState} from "react";
import { useFetchLogin } from "../hooks/useFetchLogin";
import {useNavigate} from "react-router-dom";

const Login: React.FC = () => {
    const { data, loading, error, fetchLogin } = useFetchLogin();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        if (data && data.status === "success"){
            navigate("/");
        }
    }, [data, navigate]);

const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        await fetchLogin(email, password);
    };

    return (
        <div className="flex justify-center items-center h-screen bg-gray-100">
            <div className="bg-white p-8 rounded-xl shadow-md w-96">
                <h1 className="text-2xl font-bold text-center mb-6 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 text-transparent bg-clip-text">
                    Fabflix Login
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block text-gray-700">Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    <div>
                        <label className="block text-gray-700">Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full border border-gray-300 p-2 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition duration-200"
                    >
                        {loading ? "Logging in..." : "Login"}
                    </button>
                </form>

                {error && <p className="text-red-500 text-center mt-4">{error}</p>}
                {data && data.status === "success" && (
                    <p className="text-green-600 text-center mt-4">Login Successful!</p>
                )}
            </div>
        </div>
    );
};

export default Login;
