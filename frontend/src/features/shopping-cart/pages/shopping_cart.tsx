import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useFetchShoppingCart} from "../hooks/useFetchShoppingCart";
import {useUpdateShoppingCart} from "../hooks/useUpdateShoppingCart";

const ShoppingCart: React.FC = () => {
    const { data, loading, error, fetchShoppingCart } = useFetchShoppingCart();
    const [cart, setCart] = useState<any[]>([]);
    const [total, setTotal] = useState<number>(0);

    const { data: updateData, loading: updateLoading,
        error: updateError, updateItem, removeItem } = useUpdateShoppingCart();

    const navigate = useNavigate();

    const handleProceed = () => {
        navigate("/payment", {state: {cart, total} });
    };

    useEffect(() => {
        fetchShoppingCart();
    }, []);

    useEffect(() => {
        if (data && data["Current Cart"]) {
            const cartItems = Object.values(data["Current Cart"])
            setCart(cartItems);
            setTotal(data["Total Price"]);
        }
    }, [data]);

    const handleQuantityChange = async (movieId: string, quantity: number) => {
        await updateItem(movieId, quantity);
        await fetchShoppingCart(); // refresh after change
    };

    const handleRemoveItem = async (movieId: string) => {
        await removeItem(movieId);
        await fetchShoppingCart();
    };

    if (loading) return <p style={{ color: 'var(--theme-text-primary)' }}>Loading...</p>;
    if (error) return <p style={{ color: 'var(--theme-error)' }}>Error: {error}</p>;


    return (
        <div className="flex flex-col items-center min-h-screen p-8" style={{ backgroundColor: 'var(--theme-bg-primary)' }}>
            <h1 className="text-2xl font-semibold mb-10" style={{ color: 'var(--theme-text-primary)' }}>Cart</h1>

            <div className="w-full max-w-md space-y-4">
                {cart.map((item, index) => (
                    <div
                        key={index}
                        className="flex justify-between items-center px-6 py-4 rounded-xl"
                        style={{ backgroundColor: 'var(--theme-bg-secondary)' }}
                    >
                        <div className="text-lg" style={{ color: 'var(--theme-text-primary)' }}>{item.title}</div>
                        <div className="flex items-center gap-6">
                            <div style={{ color: 'var(--theme-text-secondary)' }}>${item.price.toFixed(2)}</div>
                            <div className="flex items-center space-x-2">
                                <button
                                    onClick={() => handleQuantityChange(item.movieId, item.quantity - 1)}
                                    className="text-lg font-bold hover:text-black"
                                    style={{ color: 'var(--theme-text-primary)' }}
                                >
                                    −
                                </button>
                                <span style={{ color: 'var(--theme-text-primary)' }}>x{item.quantity}</span>
                                <button
                                    onClick={() => handleQuantityChange(item.movieId, item.quantity + 1)}
                                    className="text-lg font-bold hover:text-black"
                                    style={{ color: 'var(--theme-text-primary)' }}
                                >
                                    +
                                </button>

                                <button
                                    onClick={() => handleRemoveItem(item.movieId)}
                                    className="text-red-500 hover:text-red-700 ml-4"
                                    style={{ color: 'var(--theme-error)' }}
                                >
                                    🗑
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            <div className="w-full max-w-md flex justify-between items-center mt-8 text-lg" style={{ color: 'var(--theme-text-primary)' }}>
                <p>Total:</p>
                <p className="font-semibold" style={{ color: 'var(--theme-text-primary)' }}>${total.toFixed(2)}</p>
            </div>

            <button
                onClick={handleProceed}
                className="mt-6 px-6 py-3 rounded-xl transition text-lg"
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
                Proceed to Payment &gt;
            </button>

            {updateLoading && <p className="mt-4" style={{ color: 'var(--theme-text-secondary)' }}>Updating cart...</p>}
            {updateError && <p className="mt-4" style={{ color: 'var(--theme-error)' }}>Error: {updateError}</p>}
        </div>
    );
};

export default ShoppingCart;