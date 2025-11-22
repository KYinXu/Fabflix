
import React, { useState, useRef } from 'react';

function AddToCartButton({ movie } : { movie: any }) {
    const [isAdded, setIsAdded] = useState(false);
    const [isFailed, setIsFailed] = useState(false);
    const isProcessingRef = useRef(false);
    const buttonRef = useRef<HTMLButtonElement | null>(null);

    const handleClick = async (event: React.MouseEvent<HTMLButtonElement>) => {
        event?.stopPropagation();
        
        // Prevent multiple clicks while processing
        if (isProcessingRef.current || isAdded || isFailed) {
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
            const response = await fetch(BASE_URL, postRequest);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
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
            setIsFailed(true);
            setTimeout(() => {
                setIsFailed(false);
                isProcessingRef.current = false;
                // Reset any lingering hover styles
                if (buttonRef.current) {
                    buttonRef.current.style.backgroundColor = 'var(--theme-bg-secondary)';
                    buttonRef.current.style.color = 'var(--theme-text-primary)';
                }
            }, 700);
        }
    };

    const isInState = isAdded || isFailed;
    const backgroundColor = isAdded ? '#10b981' : isFailed ? '#ef4444' : 'var(--theme-bg-secondary)';
    const borderColor = isAdded ? '#10b981' : isFailed ? '#ef4444' : 'var(--theme-secondary)';
    const textColor = isInState ? 'white' : 'var(--theme-text-primary)';

    return (
        <button
            ref={buttonRef}
            onClick={handleClick}
            disabled={isInState}
            className="px-4 py-2 rounded font-medium transition-all duration-300 border-2"
            style={{
                backgroundColor: backgroundColor,
                borderColor: borderColor,
                color: textColor,
                transform: isInState ? 'scale(1.05)' : 'scale(1)',
                cursor: isInState ? 'default' : 'pointer',
                opacity: isInState ? 1 : 1,
                userSelect: 'none' as const,
                WebkitUserSelect: 'none' as const,
                MozUserSelect: 'none' as const,
                msUserSelect: 'none' as const
            }}
            onMouseEnter={(e) => {
                if (!isInState) {
                    const target = e.currentTarget;
                    target.style.backgroundColor = 'var(--theme-secondary)';
                    target.style.color = 'white';
                }
            }}
            onMouseLeave={(e) => {
                if (!isInState) {
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
                ) : isFailed ? (
                    <span className="flex items-center justify-center gap-1">
                        <span>✗</span>
                        Failed
                    </span>
                ) : (
                    'Add to Cart'
                )}
            </span>
        </button>
    );
}

export default AddToCartButton;