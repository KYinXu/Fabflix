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

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;


    return (
        <div className="flex flex-col items-center min-h-screen bg-white p-8">
            <h1 className="text-2xl font-semibold mb-10">Cart</h1>

            <div className="w-full max-w-md space-y-4">
                {cart.map((item, index) => (
                    <div
                        key={index}
                        className="flex justify-between items-center bg-gray-200 px-6 py-4 rounded-xl"
                    >
                        <div className="text-lg">{item.title}</div>
                        <div className="flex items-center gap-6">
                            <div className="text-gray-800">${item.price.toFixed(2)}</div>
                            <div className="flex items-center text-gray-600 space-x-2">
                                <button
                                    onClick={() => handleQuantityChange(item.movieId, item.quantity - 1)}
                                    className="text-lg font-bold hover:text-black"
                                >
                                    −
                                </button>
                                <span>x{item.quantity}</span>
                                <button
                                    onClick={() => handleQuantityChange(item.movieId, item.quantity + 1)}
                                    className="text-lg font-bold hover:text-black"
                                >
                                    +
                                </button>

                                <button
                                    onClick={() => handleRemoveItem(item.movieId)}
                                    className="text-red-500 hover:text-red-700 ml-4"
                                >
                                    🗑
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            <div className="w-full max-w-md flex justify-between items-center mt-8 text-lg">
                <p>Total:</p>
                <p className="font-semibold">${total.toFixed(2)}</p>
            </div>

            <button
                onClick={handleProceed}
                className="mt-6 px-6 py-3 bg-gray-200 rounded-xl hover:bg-gray-300 transition text-lg"
            >
                Proceed to Payment &gt;
            </button>

            {updateLoading && <p className="mt-4 text-gray-500">Updating cart...</p>}
            {updateError && <p className="mt-4 text-red-500">Error: {updateError}</p>}
        </div>
    );
};

export default ShoppingCart;