import React from 'react';

interface EmptyCartStateProps {
    onBrowseMovies: () => void;
}

const EmptyCartState: React.FC<EmptyCartStateProps> = ({ onBrowseMovies }) => {
    return (
        <div className="text-center py-16" style={{ backgroundColor: 'var(--theme-bg-secondary)', borderRadius: '12px' }}>
            <div className="text-6xl mb-4">🛒</div>
            <h2 className="text-2xl font-semibold mb-2" style={{ color: 'var(--theme-text-primary)' }}>
                Your cart is empty
            </h2>
            <p className="mb-6" style={{ color: 'var(--theme-text-secondary)' }}>
                Looks like you haven't added any movies yet.
            </p>
            <button
                onClick={onBrowseMovies}
                className="px-6 py-3 rounded-lg font-semibold transition-all duration-200"
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
                Browse Movies
            </button>
        </div>
    );
};

export default EmptyCartState;
