import React from 'react';

interface YearDropdownProps {
    value: string;
    onChange: (value: string) => void;
}

const YearDropdown: React.FC<YearDropdownProps> = ({ value, onChange }) => {
    const years: number[] = [];
    
    // Generate years from 2001 to 2017
    for (let year = 2017; year >= 2001; year--) {
        years.push(year);
    }

    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            className="w-full px-4 py-3 border-2 rounded-lg focus:outline-none focus:ring-2 transition-all"
            style={{
                borderColor: 'var(--theme-border-primary)',
                backgroundColor: 'var(--theme-bg-secondary)',
                color: 'var(--theme-text-primary)',
                '--tw-ring-color': 'var(--theme-secondary)'
            } as React.CSSProperties}
        >
            <option value="" style={{ backgroundColor: 'var(--theme-bg-secondary)', color: 'var(--theme-text-primary)' }}>Year... 📅</option>
            {years.map((year) => (
                <option key={year} value={year} style={{ backgroundColor: 'var(--theme-bg-secondary)', color: 'var(--theme-text-primary)' }}>
                    {year}
                </option>
            ))}
        </select>
    );
};

export default YearDropdown;

