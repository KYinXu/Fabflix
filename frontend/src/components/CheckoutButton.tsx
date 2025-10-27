import {useNavigate} from "react-router-dom";
import { useState, useEffect } from "react";

function CheckoutButton() {
    const navigate = useNavigate();
    const [itemCount, setItemCount] = useState<number>(0);

    useEffect(() => {
        const fetchCartCount = async () => {
            try {
                const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/cart`, {
                    method: 'GET',
                    credentials: 'include'
                });
                if (response.ok) {
                    const data = await response.json();
                    if (data["Current Cart"]) {
                        const cartItems = Object.values(data["Current Cart"]);
                        const totalItems = cartItems.reduce((sum: number, item: any) => sum + item.quantity, 0);
                        setItemCount(totalItems);
                    }
                }
            } catch (error) {
                console.error('Failed to fetch cart count:', error);
            }
        };

        fetchCartCount();
        
        // Poll for cart updates every 2 seconds
        const interval = setInterval(fetchCartCount, 2000);
        
        return () => clearInterval(interval);
    }, []);

    const handleClick = () => {
        navigate("/cart");
    };

    return (
        <div className="flex items-center gap-3">
            {itemCount > 0 && (
                <span className="text-sm font-medium" style={{ color: 'var(--theme-text-primary)' }}>
                    <span style={{ color: 'var(--theme-secondary)', fontWeight: 'bold' }}>{itemCount}</span> {itemCount === 1 ? 'item' : 'items'} in cart
                </span>
            )}
            <button
                onClick={handleClick}
                className="px-6 py-3 rounded-lg text-sm font-semibold transition-all duration-200
                           border-2 relative"
                style={{
                    backgroundColor: 'var(--theme-bg-primary)',
                    color: 'var(--theme-text-primary)',
                    borderColor: 'var(--theme-secondary)',
                    boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)'
                }}
                onMouseEnter={(e) => {
                    e.currentTarget.style.backgroundColor = 'var(--theme-secondary)';
                    e.currentTarget.style.color = 'white';
                    e.currentTarget.style.boxShadow = '0 6px 20px rgba(0, 0, 0, 0.25)';
                }}
                onMouseLeave={(e) => {
                    e.currentTarget.style.backgroundColor = 'var(--theme-bg-primary)';
                    e.currentTarget.style.color = 'var(--theme-text-primary)';
                    e.currentTarget.style.boxShadow = '0 4px 12px rgba(0, 0, 0, 0.15)';
                }}
            >
                🛒 Checkout
            </button>
        </div>
    );
}

export default CheckoutButton;