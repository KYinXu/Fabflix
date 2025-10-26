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
        <div className="flex items-center gap-4 bg-white rounded-lg shadow-md p-4">
            <div className="flex items-center gap-2">
                <label htmlFor="sortCriteria" className="text-gray-700 font-medium whitespace-nowrap">
                    Sort by:
                </label>
                <select
                    id="sortCriteria"
                    value={sortCriteria}
                    onChange={(e) => onSortCriteriaChange(e.target.value)}
                    className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
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
                className="px-4 py-2 bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white rounded-lg font-semibold hover:shadow-lg transition-all duration-200 hover:scale-105 flex items-center gap-2"
                title="Toggle sort order"
            >
                {sortOrder === 'ASC' ? (
                    <>
                        <span>↑</span>
                        <span>Ascending</span>
                    </>
                ) : (
                    <>
                        <span>↓</span>
                        <span>Descending</span>
                    </>
                )}
            </button>
        </div>
    );
};

export default SortControl;
