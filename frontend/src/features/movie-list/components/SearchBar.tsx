import React from 'react';

interface SearchBarProps {
    value: string;
    onChange: (value: string) => void;
    placeholder: string;
    focusColor: 'blue' | 'purple';
}

const SearchBar: React.FC<SearchBarProps> = ({ value, onChange, placeholder, focusColor }) => {
    const focusClasses = focusColor === 'blue'
        ? 'focus:border-blue-500 focus:ring-blue-200'
        : 'focus:border-purple-500 focus:ring-purple-200';

    return (
        <input
            type="text"
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder}
            className={`w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:ring-2 transition-all ${focusClasses}`}
        />
    );
};

export default SearchBar;

