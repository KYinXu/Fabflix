import React, {useEffect, useState} from "react";
import {useNavigate} from "react-router-dom";
import {useFetchShoppingCart} from "../hooks/useFetchShoppingCart";

const ShoppingCart: React.FC = () => {
    const { data, loading, error, fetchShoppingCart } = useFetchShoppingCart();
    const [cart, setCart] = useState<any[]>([]);
    const [total, setTotal] = useState<number>(0);
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

    if (loading) return <p>Loading...</p>;
    if (error) return <p>Error: {error}</p>;


    return (
        <div>
            <h1>Shopping Cart</h1>
            <ul>
                {cart.map((item: any) => (
                    <li key={item.movieId}>
                        {item.title} — ${item.price.toFixed(2)} × {item.quantity}
                    </li>
                ))}
            </ul>
            <p>Total: ${total.toFixed(2)}</p>
            <button onClick={handleProceed}>Proceed to Checkout</button>
        </div>
    );
};

export default ShoppingCart;