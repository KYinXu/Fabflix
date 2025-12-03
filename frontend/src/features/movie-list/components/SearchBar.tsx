import React from 'react';

interface SearchBarProps {
    value: string;
    onChange: (value: string) => void;
    placeholder: string;
    focusColor: 'blue' | 'purple';
    onKeyDown?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
}

const SearchBar: React.FC<SearchBarProps> = ({ value, onChange, placeholder, focusColor, onKeyDown }) => {
    const focusClasses = focusColor === 'blue'
        ? 'focus:border-blue-500 focus:ring-blue-200'
        : 'focus:border-purple-500 focus:ring-purple-200';

    return (
        <input
            type="text"
            value={value}
            onChange={(e) => onChange(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder={placeholder}
            className={`w-full px-4 py-3 border-2 rounded-lg focus:outline-none focus:ring-2 transition-all ${focusClasses}`}
            style={{
                borderColor: 'var(--theme-border-primary)',
                backgroundColor: 'var(--theme-bg-secondary)',
                color: 'var(--theme-text-primary)',
                '--tw-ring-color': focusColor === 'blue' ? 'var(--theme-primary)' : 'var(--theme-secondary)'
            } as React.CSSProperties}
        />
    );
};

export default SearchBar;

