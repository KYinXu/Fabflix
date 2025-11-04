import React, {useEffect, useState} from "react";
import { useFetchLogin } from "../hooks/useFetchLogin";
import {useNavigate} from "react-router-dom";
import ReCAPTCHA from 'react-google-recaptcha';

const Login: React.FC = () => {
    const { data, loading, error, fetchLogin } = useFetchLogin();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [recaptcha, setRecaptcha] = useState<string | null>(null);
    const navigate = useNavigate();

    useEffect(() => {
        if (data && data.status === "success"){
            navigate("/");
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
        <div className="flex justify-center items-center h-screen" style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
            <div className="p-8 rounded-xl shadow-md w-96 border" style={{ backgroundColor: 'var(--theme-bg-card)', borderColor: 'var(--theme-border-secondary)' }}>
                <h1 className="text-2xl font-bold text-center mb-6" style={{ 
                    background: 'linear-gradient(to right, var(--theme-primary-light), var(--theme-secondary-light), var(--theme-accent-light))',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                    backgroundClip: 'text'
                }}>
                    Fabflix Login
                </h1>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <div>
                        <label className="block" style={{ color: 'var(--theme-text-dark)' }}>Email</label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            className="w-full p-2 rounded-lg focus:outline-none focus:ring-2"
                            style={{ 
                                borderColor: 'var(--theme-border-secondary)',
                                backgroundColor: 'var(--theme-bg-card)',
                                color: 'var(--theme-text-dark)',
                                '--tw-ring-color': 'var(--theme-primary)'
                            } as React.CSSProperties}
                        />
                    </div>

                    <div>
                        <label className="block" style={{ color: 'var(--theme-text-dark)' }}>Password</label>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full p-2 rounded-lg focus:outline-none focus:ring-2"
                            style={{ 
                                borderColor: 'var(--theme-border-secondary)',
                                backgroundColor: 'var(--theme-bg-card)',
                                color: 'var(--theme-text-dark)',
                                '--tw-ring-color': 'var(--theme-primary)'
                            } as React.CSSProperties}
                        />
                    </div>
                    <ReCAPTCHA
                        sitekey={import.meta.env.VITE_RECAPTCHA_SITE_KEY}
                        onChange={(token) => setRecaptcha(token)}
                    />
                    <button
                        type="submit"
                        disabled={loading}
                        className="w-full text-white py-2 rounded-lg transition duration-200"
                        style={{ backgroundColor: 'var(--theme-primary)' }}
                        onMouseEnter={(e) => !loading && ((e.target as HTMLElement).style.backgroundColor = 'var(--theme-primary-hover)')}
                        onMouseLeave={(e) => (e.target as HTMLElement).style.backgroundColor = 'var(--theme-primary)'}
                    >
                        {loading ? "Logging in..." : "Login"}
                    </button>
                </form>
                {error && <p className="text-center mt-4" style={{ color: 'var(--theme-error)' }}>{error}</p>}
                {data && data.status === "success" && (
                    <p className="text-center mt-4" style={{ color: 'var(--theme-success)' }}>Login Successful!</p>
                )}
            </div>
        </div>
    );
};

export default Login;
