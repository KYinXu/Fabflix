import React, {useEffect, useState} from "react";
import { useFetchDashboard } from "../hooks/useFetchDashboardLogin";
import {useNavigate} from "react-router-dom";
import ReCAPTCHA from 'react-google-recaptcha';

const DashboardLogin: React.FC = () => {
    const { data, loading, error, fetchLogin } = useFetchDashboard();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [recaptcha, setRecaptcha] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (data && data.status === "success"){
            navigate("/dashboard");
        }
    }, [data, navigate]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!recaptcha) {
            alert("Please complete the reCAPTCHA first!");
            return;
        }
        await fetchLogin(email, password, recaptcha);
    };

    return (
        <div className="flex justify-center items-center min-h-screen" style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
            <div className="p-8 rounded-xl shadow-lg w-96 border-2" style={{
                backgroundColor: 'var(--theme-bg-card)',
                borderColor: 'var(--theme-border-secondary)',
                boxShadow: '0 10px 25px rgba(0, 0, 0, 0.3)'
            }}>
                <h1 className="text-3xl font-bold text-center mb-8" style={{
                    background: 'linear-gradient(to right, var(--theme-primary-light), var(--theme-secondary-light), var(--theme-accent-light))',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text'
                }}>
                    Fabflix Employee Login
                </h1>

                <form onSubmit={handleSubmit} className="space-y-6">
                    <div>
                        <label className="block text-sm font-medium mb-2" style={{ color: 'var(--theme-text-primary)' }}>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                            style={{
                                borderColor: 'var(--theme-border-secondary)',
                                backgroundColor: 'var(--theme-bg-primary)',
                                color: 'var(--theme-text-primary)',
                                '--tw-ring-color': 'var(--theme-primary)'
                            } as React.CSSProperties}
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium mb-2" style={{ color: 'var(--theme-text-primary)' }}>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full p-3 rounded-lg border-2 focus:outline-none focus:ring-2 transition-all duration-200"
                            style={{
                                borderColor: 'var(--theme-border-secondary)',
                                backgroundColor: 'var(--theme-bg-primary)',
                                color: 'var(--theme-text-primary)',
                                '--tw-ring-color': 'var(--theme-primary)'
                            } as React.CSSProperties}
                        />
                    </div>

                    <div className="flex justify-center">
                        <ReCAPTCHA
                            sitekey={import.meta.env.VITE_RECAPTCHA_SITE_KEY}
                            onChange={(token) => setRecaptcha(token)}
                        />
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full py-3 rounded-lg font-semibold transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                        style={{
                            backgroundColor: 'var(--theme-primary)',
                            color: 'white',
                            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                        }}
                        onMouseEnter={(e) => {
                            if (!loading) {
                                e.currentTarget.style.backgroundColor = 'var(--theme-primary-hover)';
                                e.currentTarget.style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.25)';
                            }
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'var(--theme-primary)';
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                        }}
                    >
                        {loading ? "Logging in..." : "Login"}
                    </button>
                </form>

                {error && (
                    <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3" style={{
                        backgroundColor: 'rgba(220, 38, 38, 0.1)',
                        borderColor: 'var(--theme-error)',
                        color: 'var(--theme-error)'
                    }}>
                        <span className="text-sm font-medium">{error}</span>
                    </div>
                )}

                {data && data.status === "success" && (
                    <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3" style={{
                        backgroundColor: 'rgba(34, 197, 94, 0.1)',
                        borderColor: 'var(--theme-success)',
                        color: 'var(--theme-success)'
                    }}>
                        <span className="text-sm font-medium">Login Successful!</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default DashboardLogin;
