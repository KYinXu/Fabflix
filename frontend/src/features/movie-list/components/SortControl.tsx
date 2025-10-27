import React from 'react';

interface SortControlProps {
    sortCriteria: string;
    sortOrder: string;
    onSortCriteriaChange: (criteria: string) => void;
    onSortOrderChange: (order: string) => void;
}

const SortControl: React.FC<SortControlProps> = ({ sortCriteria, sortOrder, onSortCriteriaChange, onSortOrderChange }) => {
    const handleOrderToggle = () => {
        const newOrder = sortOrder === 'ASC' ? 'DESC' : 'ASC';
        onSortOrderChange(newOrder);
    };

    const sortOptions = [
        { value: 'r.ratings', label: 'Ratings' },
        { value: 'm.title', label: 'Titles' }
    ];

    return (
        <div className="flex items-center gap-3 rounded p-3" style={{ backgroundColor: 'var(--theme-bg-secondary)', borderColor: 'var(--theme-border-primary)' }}>
            <div className="flex items-center gap-2">
                <label htmlFor="sortCriteria" className="text-base font-medium whitespace-nowrap" style={{ color: 'var(--theme-text-secondary)' }}>
                    Sort:
                </label>
                <select
                    id="sortCriteria"
                    value={sortCriteria}
                    onChange={(e) => onSortCriteriaChange(e.target.value)}
                    className="px-3 py-2 text-base border rounded focus:outline-none focus:ring-1 cursor-pointer"
                    style={{ 
                        borderColor: 'var(--theme-border-primary)',
                        backgroundColor: 'var(--theme-bg-tertiary)',
                        color: 'var(--theme-text-primary)',
                        '--tw-ring-color': 'var(--theme-primary)'
                    } as React.CSSProperties}
                >
                    {sortOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                    ))}
                </select>
            </div>
            
            <button
                onClick={handleOrderToggle}
                className="px-3 py-2 text-lg rounded font-medium transition-colors duration-200 flex items-center justify-center"
                style={{ 
                    backgroundColor: 'transparent',
                    color: 'var(--theme-text-primary)'
                }}
                onMouseEnter={(e) => {
                    const target = e.target as HTMLElement;
                    target.style.backgroundColor = 'var(--theme-bg-tertiary)';
                }}
                onMouseLeave={(e) => {
                    const target = e.target as HTMLElement;
                    target.style.backgroundColor = 'transparent';
                }}
                title="Toggle sort order"
            >
                {sortOrder === 'ASC' ? '↑' : '↓'}
            </button>
        </div>
    );
};

export default SortControl;
