import React from 'react';

interface OrderSummaryProps {
    total: number;
    onContinueShopping: () => void;
    onProceedToCheckout: () => void;
}

const OrderSummary: React.FC<OrderSummaryProps> = ({ 
    total, 
    onContinueShopping, 
    onProceedToCheckout 
}) => {
    return (
        <div className="sticky bottom-0 bg-gradient-to-t from-var(--theme-bg-primary) via-var(--theme-bg-primary) to-transparent pt-6">
            <div
                className="p-6 rounded-lg mb-6"
                style={{
                    backgroundColor: 'var(--theme-bg-secondary)',
                    border: '2px solid var(--theme-secondary)',
                    boxShadow: '0 4px 16px rgba(0, 0, 0, 0.15)'
                }}
            >
                <h2 className="text-2xl font-bold mb-4" style={{ color: 'var(--theme-text-primary)' }}>
                    Order Summary
                </h2>
                <div className="space-y-3 mb-6">
                    <div className="flex justify-between text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
                        <span>Subtotal:</span>
                        <span style={{ color: 'var(--theme-text-primary)' }}>${total.toFixed(2)}</span>
                    </div>
                    <div className="flex justify-between text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
                        <span>Tax (estimated):</span>
                        <span style={{ color: 'var(--theme-text-primary)' }}>$0.00</span>
                    </div>
                    <div className="border-t pt-3 mt-3" style={{ borderColor: 'var(--theme-border-primary)' }}>
                        <div className="flex justify-between text-2xl font-bold" style={{ color: 'var(--theme-secondary)' }}>
                            <span>Total:</span>
                            <span>${total.toFixed(2)}</span>
                        </div>
                    </div>
                </div>
                <div className="flex gap-4">
                    <button
                        onClick={onContinueShopping}
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
                        Continue Shopping
                    </button>
                    <button
                        onClick={onProceedToCheckout}
                        className="flex-1 px-6 py-3 rounded-lg font-semibold transition-all duration-200"
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
                        Proceed to Checkout
                    </button>
                </div>
            </div>
        </div>
    );
};

export default OrderSummary;
