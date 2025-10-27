import React from 'react';

interface CartItemProps {
    item: {
        movieId: string;
        title: string;
        price: number;
        quantity: number;
    };
    onIncrease: (movieId: string) => void;
    onDecrease: (movieId: string) => void;
    onRemove: (movieId: string) => void;
}

const CartItem: React.FC<CartItemProps> = ({ item, onIncrease, onDecrease, onRemove }) => {
    return (
        <div
            className="p-6 rounded-lg transition-all duration-200"
            style={{
                backgroundColor: 'var(--theme-bg-secondary)',
                border: '1px solid var(--theme-border-primary)',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
            }}
        >
            <div className="flex justify-between items-center">
                <div className="flex-1">
                    <h3 className="text-xl font-semibold mb-1" style={{ color: 'var(--theme-text-primary)' }}>
                        {item.title}
                    </h3>
                    <p className="text-sm" style={{ color: 'var(--theme-text-secondary)' }}>
                        ${item.price.toFixed(2)} each
                    </p>
                </div>
                <div className="flex items-center gap-4">
                    {/* Quantity Controls */}
                    <div className="flex items-center gap-2">
                        <button
                            onClick={() => onDecrease(item.movieId)}
                            className="w-8 h-8 rounded-full font-bold transition-all duration-200 flex items-center justify-center"
                            style={{
                                backgroundColor: 'var(--theme-bg-tertiary)',
                                color: 'var(--theme-text-primary)',
                                border: '1px solid var(--theme-border-primary)'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = 'var(--theme-error)';
                                e.currentTarget.style.color = 'white';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = 'var(--theme-bg-tertiary)';
                                e.currentTarget.style.color = 'var(--theme-text-primary)';
                            }}
                        >
                            −
                        </button>
                        <span className="w-12 text-center font-semibold" style={{ color: 'var(--theme-text-primary)' }}>
                            {item.quantity}
                        </span>
                        <button
                            onClick={() => onIncrease(item.movieId)}
                            className="w-8 h-8 rounded-full font-bold transition-all duration-200 flex items-center justify-center"
                            style={{
                                backgroundColor: 'var(--theme-bg-tertiary)',
                                color: 'var(--theme-text-primary)',
                                border: '1px solid var(--theme-border-primary)'
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.backgroundColor = 'var(--theme-secondary)';
                                e.currentTarget.style.color = 'white';
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.backgroundColor = 'var(--theme-bg-tertiary)';
                                e.currentTarget.style.color = 'var(--theme-text-primary)';
                            }}
                        >
                            +
                        </button>
                    </div>
                    
                    {/* Price Display */}
                    <div className="text-right min-w-[100px]">
                        <div className="text-lg font-semibold" style={{ color: 'var(--theme-text-primary)' }}>
                            ${(item.price * item.quantity).toFixed(2)}
                        </div>
                    </div>

                    {/* Remove Button */}
                    <button
                        onClick={() => onRemove(item.movieId)}
                        className="w-10 h-10 rounded-full transition-all duration-200 flex items-center justify-center ml-2"
                        style={{
                            backgroundColor: 'var(--theme-error)',
                            color: 'white'
                        }}
                        onMouseEnter={(e) => {
                            e.currentTarget.style.transform = 'scale(1.1)';
                        }}
                        onMouseLeave={(e) => {
                            e.currentTarget.style.transform = 'scale(1)';
                        }}
                        title="Remove item"
                    >
                        ✕
                    </button>
                </div>
            </div>
        </div>
    );
};

export default CartItem;
