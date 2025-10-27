import React from 'react';

interface PaginationControlsProps {
    currentPage: number;
    hasNextPage: boolean;
    pageSize: number;
    goToPreviousPage: () => void;
    goToNextPage: () => void;
    setPageSize: (size: number) => void;
    isLoading?: boolean;
}

const PaginationControls: React.FC<PaginationControlsProps> = ({
    currentPage,
    hasNextPage,
    pageSize,
    goToPreviousPage,
    goToNextPage,
    setPageSize,
}) => {
    return (
        <div className="flex items-center gap-6 p-2 rounded" style={{ backgroundColor: 'var(--theme-bg-secondary)', border: '1px solid var(--theme-border-primary)' }}>
            <div className="flex items-center gap-3">
                <label htmlFor="pageSize" className="text-base font-medium" style={{ color: 'var(--theme-text-secondary)' }}>
                    Per page:
                </label>
                <select
                    id="pageSize"
                    value={pageSize}
                    onChange={(e) => setPageSize(Number(e.target.value))}
                    className="px-3 py-2 text-base border rounded focus:outline-none focus:ring-2"
                    style={{ 
                        borderColor: 'var(--theme-border-primary)',
                        backgroundColor: 'var(--theme-bg-secondary)',
                        color: 'var(--theme-text-primary)',
                        '--tw-ring-color': 'var(--theme-primary)'
                    } as React.CSSProperties}
                >
                    <option value="10">10</option>
                    <option value="25">25</option>
                    <option value="50">50</option>
                    <option value="100">100</option>
                </select>
            </div>
            <div className="flex items-center gap-3">
                <button
                    onClick={goToPreviousPage}
                    disabled={currentPage === 0}
                    className={`px-4 py-2 text-base rounded font-medium transition-colors duration-200 ${
                        currentPage === 0
                            ? 'opacity-50 cursor-not-allowed'
                            : 'hover:opacity-80'
                    }`}
                    style={{ 
                        backgroundColor: currentPage === 0 ? 'var(--theme-bg-tertiary)' : 'var(--theme-primary)',
                        color: 'var(--theme-text-primary)'
                    }}
                >
                    ← Prev
                </button>
                <span className="text-base font-medium px-3" style={{ color: 'var(--theme-text-secondary)' }}>
                    Page {currentPage + 1}
                </span>
                <button
                    onClick={goToNextPage}
                    disabled={!hasNextPage}
                    className={`px-4 py-2 text-base rounded font-medium transition-colors duration-200 ${
                        !hasNextPage
                            ? 'opacity-50 cursor-not-allowed'
                            : 'hover:opacity-80'
                    }`}
                    style={{ 
                        backgroundColor: !hasNextPage ? 'var(--theme-bg-tertiary)' : 'var(--theme-primary)',
                        color: 'var(--theme-text-primary)'
                    }}
                >
                    Next →
                </button>
            </div>
        </div>
    );
};

export default PaginationControls;
