
import React, { useState, useRef } from 'react';

function AddToCartButton({ movie } : { movie: any }) {
    const [isAdded, setIsAdded] = useState(false);
    const isProcessingRef = useRef(false);
    const buttonRef = useRef<HTMLButtonElement | null>(null);

    const handleClick = async (event: React.MouseEvent<HTMLButtonElement>) => {
        event?.stopPropagation();
        
        // Prevent multiple clicks while processing
        if (isProcessingRef.current || isAdded) {
            return;
        }
        
        isProcessingRef.current = true;
        const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/cart`;
        try{
            const postRequest: RequestInit = {
                method: 'POST',
                credentials: "include",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify([
                    {
                        movieId: movie.id,
                        title: movie.title,
                        quantity: 1,
                    },
                ])
            };
            await fetch(BASE_URL, postRequest);
            setIsAdded(true);
            setTimeout(() => {
                setIsAdded(false);
                isProcessingRef.current = false;
                // Reset any lingering hover styles
                if (buttonRef.current) {
                    buttonRef.current.style.backgroundColor = 'var(--theme-bg-secondary)';
                    buttonRef.current.style.color = 'var(--theme-text-primary)';
                }
            }, 700);

        } catch (err: any) {
            console.error('Failed to add item to cart:', err);
            isProcessingRef.current = false;
        }
    };

    return (
        <button
            ref={buttonRef}
            onClick={handleClick}
            disabled={isAdded}
            className="px-4 py-2 rounded font-medium transition-all duration-300 border-2"
            style={{
                backgroundColor: isAdded ? '#10b981' : 'var(--theme-bg-secondary)',
                borderColor: isAdded ? '#10b981' : 'var(--theme-secondary)',
                color: isAdded ? 'white' : 'var(--theme-text-primary)',
                transform: isAdded ? 'scale(1.05)' : 'scale(1)',
                cursor: isAdded ? 'default' : 'pointer',
                opacity: isAdded ? 1 : 1,
                userSelect: 'none' as const,
                WebkitUserSelect: 'none' as const,
                MozUserSelect: 'none' as const,
                msUserSelect: 'none' as const
            }}
            onMouseEnter={(e) => {
                if (!isAdded) {
                    const target = e.currentTarget;
                    target.style.backgroundColor = 'var(--theme-secondary)';
                    target.style.color = 'white';
                }
            }}
            onMouseLeave={(e) => {
                if (!isAdded) {
                    const target = e.currentTarget;
                    target.style.backgroundColor = 'var(--theme-bg-secondary)';
                    target.style.color = 'var(--theme-text-primary)';
                }
            }}
        >
            <span className="inline-block min-w-[100px] text-center" style={{ userSelect: 'none' as const }}>
                {isAdded ? (
                    <span className="flex items-center justify-center gap-1">
                        <span>✓</span>
                        Added!
                    </span>
                ) : (
                    'Add to Cart'
                )}
            </span>
        </button>
    );
}

export default AddToCartButton;