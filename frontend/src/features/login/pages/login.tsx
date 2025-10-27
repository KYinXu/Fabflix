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
                    Fabflix Login
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
                            onFocus={(e) => {
                                e.currentTarget.style.borderColor = 'var(--theme-primary)';
                                e.currentTarget.style.boxShadow = '0 0 0 3px rgba(139, 69, 19, 0.1)';
                            }}
                            onBlur={(e) => {
                                e.currentTarget.style.borderColor = 'var(--theme-border-secondary)';
                                e.currentTarget.style.boxShadow = 'none';
                            }}
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
                            onFocus={(e) => {
                                e.currentTarget.style.borderColor = 'var(--theme-primary)';
                                e.currentTarget.style.boxShadow = '0 0 0 3px rgba(139, 69, 19, 0.1)';
                            }}
                            onBlur={(e) => {
                                e.currentTarget.style.borderColor = 'var(--theme-border-secondary)';
                                e.currentTarget.style.boxShadow = 'none';
                            }}
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
                                e.currentTarget.style.transform = 'translateY(-1px)';
                            }
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.backgroundColor = 'var(--theme-primary)';
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                            e.currentTarget.style.transform = 'translateY(0)';
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
                        <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                        </svg>
                        <span className="text-sm font-medium">{error}</span>
                    </div>
                )}
                
                {data && data.status === "success" && (
                    <div className="mt-6 p-4 rounded-lg border-2 flex items-center gap-3" style={{
                        backgroundColor: 'rgba(34, 197, 94, 0.1)',
                        borderColor: 'var(--theme-success)',
                        color: 'var(--theme-success)'
                    }}>
                        <svg className="w-5 h-5 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                        </svg>
                        <span className="text-sm font-medium">Login Successful!</span>
                    </div>
                )}
            </div>
        </div>
    );
};

export default Login;
