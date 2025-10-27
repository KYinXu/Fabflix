import React, {useEffect, useState} from "react";
import {useFetchPayment} from "../hooks/useFetchPayment";
import {updateDatabaseOnPurchase} from "../components/updateDatabaseOnPurchase"
import {useLocation, useNavigate} from "react-router-dom";

const Payment: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { data, loading, error, fetchPayment } = useFetchPayment();
    const [id, setId] = useState("");
    const [first_name, setFirstName] = useState("");
    const [last_name, setLastName] = useState("");
    const [expiration, setExpiration] = useState("");

    const cart = location.state?.cart || [];
    const total = location.state?.total || 0;

    useEffect(() => {
        if (data && data.status === "success"){
            updateDatabaseOnPurchase(id);
        }
    }, [data, id]);

    const handleSubmit = async (event: React.FormEvent) => {
        event.preventDefault();
        if (!id || !first_name || !last_name || !expiration) {
            return;
        }
        await fetchPayment(id, first_name, last_name, expiration);
    };

    const handleBack = () => {
        navigate("/cart");
    };

    if (data && data.status === "success") {
        return (
            <div className="min-h-screen flex items-center justify-center" style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
                <div className="text-center max-w-md mx-auto px-4">
                    <div className="mb-6">
                        <div className="text-7xl mb-4">✅</div>
                        <h1 className="text-4xl font-bold mb-2" style={{ color: 'var(--theme-text-primary)' }}>
                            Payment Successful!
                        </h1>
                        <p className="text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
                            Thank you for your order
                        </p>
                    </div>
                    <div className="p-6 rounded-lg mb-6" style={{ backgroundColor: 'var(--theme-bg-secondary)' }}>
                        <p className="text-lg mb-2" style={{ color: 'var(--theme-text-secondary)' }}>
                            Total Paid: <span className="font-bold" style={{ color: 'var(--theme-secondary)' }}>${total.toFixed(2)}</span>
                        </p>
                        <p className="text-sm" style={{ color: 'var(--theme-text-secondary)' }}>
                            {cart.length} {cart.length === 1 ? 'item' : 'items'}
                        </p>
                    </div>
                    <button
                        onClick={() => navigate("/")}
                        className="px-8 py-3 rounded-lg font-semibold transition-all duration-200"
                        style={{
                            backgroundColor: 'var(--theme-secondary)',
                            color: 'white',
                            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.25)';
                            e.currentTarget.style.transform = 'translateY(-2px)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                            e.currentTarget.style.transform = 'translateY(0)';
                        }}
                    >
                        Continue Shopping
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center py-8" style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
            <div className="max-w-lg w-full mx-auto px-4">
                <div className="mb-8 text-center">
                    <h1 className="text-4xl font-bold mb-2" style={{ color: 'var(--theme-text-primary)' }}>
                        Checkout
                    </h1>
                    <p className="text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
                        Complete your purchase
                    </p>
                </div>

                <div className="mb-6 p-6 rounded-lg" style={{ backgroundColor: 'var(--theme-bg-secondary)' }}>
                    <h2 className="text-xl font-semibold mb-4" style={{ color: 'var(--theme-text-primary)' }}>
                        Order Summary
                    </h2>
                    {cart.map((item: any) => (
                        <div key={item.movieId} className="flex justify-between mb-2">
                            <span className="text-sm" style={{ color: 'var(--theme-text-secondary)' }}>
                                {item.title} x{item.quantity}
                            </span>
                            <span className="text-sm font-semibold" style={{ color: 'var(--theme-text-primary)' }}>
                                ${(item.price * item.quantity).toFixed(2)}
                            </span>
                        </div>
                    ))}
                    <div className="border-t pt-3 mt-3" style={{ borderColor: 'var(--theme-border-primary)' }}>
                        <div className="flex justify-between text-xl font-bold" style={{ color: 'var(--theme-secondary)' }}>
                            <span>Total:</span>
                            <span>${total.toFixed(2)}</span>
                        </div>
                    </div>
                </div>

                <div className="p-6 rounded-lg" style={{ backgroundColor: 'var(--theme-bg-secondary)' }}>
                    <h2 className="text-xl font-semibold mb-4" style={{ color: 'var(--theme-text-primary)' }}>
                        Payment Information
                    </h2>
                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block mb-2 font-medium" style={{ color: 'var(--theme-text-primary)' }}>
                                Credit Card ID
                            </label>
                            <input
                                type="text"
                                value={id}
                                onChange={(e) => setId(e.target.value)}
                                className="w-full p-3 rounded-lg transition-all duration-200"
                                style={{
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    border: '1px solid var(--theme-border-primary)'
                                }}
                                onFocus={(e) => {
                                    e.target.style.borderColor = 'var(--theme-secondary)';
                                    e.target.style.boxShadow = '0 0 0 3px rgba(139, 92, 246, 0.1)';
                                }}
                                onBlur={(e) => {
                                    e.target.style.borderColor = 'var(--theme-border-primary)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            />
                        </div>
                        <div>
                            <label className="block mb-2 font-medium" style={{ color: 'var(--theme-text-primary)' }}>
                                First Name
                            </label>
                            <input
                                type="text"
                                value={first_name}
                                onChange={(e) => setFirstName(e.target.value)}
                                className="w-full p-3 rounded-lg transition-all duration-200"
                                style={{
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    border: '1px solid var(--theme-border-primary)'
                                }}
                                onFocus={(e) => {
                                    e.target.style.borderColor = 'var(--theme-secondary)';
                                    e.target.style.boxShadow = '0 0 0 3px rgba(139, 92, 246, 0.1)';
                                }}
                                onBlur={(e) => {
                                    e.target.style.borderColor = 'var(--theme-border-primary)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            />
                        </div>
                        <div>
                            <label className="block mb-2 font-medium" style={{ color: 'var(--theme-text-primary)' }}>
                                Last Name
                            </label>
                            <input
                                type="text"
                                value={last_name}
                                onChange={(e) => setLastName(e.target.value)}
                                className="w-full p-3 rounded-lg transition-all duration-200"
                                style={{
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    border: '1px solid var(--theme-border-primary)'
                                }}
                                onFocus={(e) => {
                                    e.target.style.borderColor = 'var(--theme-secondary)';
                                    e.target.style.boxShadow = '0 0 0 3px rgba(139, 92, 246, 0.1)';
                                }}
                                onBlur={(e) => {
                                    e.target.style.borderColor = 'var(--theme-border-primary)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            />
                        </div>
                        <div>
                            <label className="block mb-2 font-medium" style={{ color: 'var(--theme-text-primary)' }}>
                                Expiration Date
                            </label>
                            <input
                                type="date"
                                value={expiration}
                                onChange={(e) => setExpiration(e.target.value)}
                                className="w-full p-3 rounded-lg transition-all duration-200"
                                style={{
                                    backgroundColor: 'var(--theme-bg-primary)',
                                    color: 'var(--theme-text-primary)',
                                    border: '1px solid var(--theme-border-primary)'
                                }}
                                onFocus={(e) => {
                                    e.target.style.borderColor = 'var(--theme-secondary)';
                                    e.target.style.boxShadow = '0 0 0 3px rgba(139, 92, 246, 0.1)';
                                }}
                                onBlur={(e) => {
                                    e.target.style.borderColor = 'var(--theme-border-primary)';
                                    e.target.style.boxShadow = 'none';
                                }}
                            />
                        </div>
                        <div className="flex gap-4 pt-2">
                            <button
                                type="button"
                                onClick={handleBack}
                                className="flex-1 px-6 py-3 rounded-lg font-semibold transition-all duration-200 border-2"
                                style={{
                                    backgroundColor: 'transparent',
                                    color: 'var(--theme-text-primary)',
                                    borderColor: 'var(--theme-border-primary)'
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.backgroundColor = 'var(--theme-bg-tertiary)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.backgroundColor = 'transparent';
                                }}
                            >
                                Back to Cart
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="flex-1 px-6 py-3 rounded-lg font-semibold transition-all duration-200"
                                style={{
                                    backgroundColor: 'var(--theme-secondary)',
                                    color: 'white',
                                    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
                                    opacity: loading ? 0.6 : 1
                                }}
                                onMouseEnter={(e) => {
                                    if (!loading) {
                                        e.currentTarget.style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.25)';
                                        e.currentTarget.style.transform = 'translateY(-2px)';
                                    }
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                                    e.currentTarget.style.transform = 'translateY(0)';
                                }}
                            >
                                {loading ? "Processing..." : "Place Order"}
                            </button>
                        </div>
                    </form>

                    {error && (
                        <div className="mt-4 p-4 rounded-lg border-2" style={{ 
                            backgroundColor: 'rgba(239, 68, 68, 0.1)', 
                            borderColor: 'var(--theme-error)',
                            color: 'var(--theme-error)'
                        }}>
                            <div className="flex items-center gap-2">
                                <span className="text-xl">⚠️</span>
                                <p className="font-semibold text-sm">{error}</p>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Payment;
