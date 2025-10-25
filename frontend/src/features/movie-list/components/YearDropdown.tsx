import React from 'react';

interface YearDropdownProps {
    value: string;
    onChange: (value: string) => void;
}

const YearDropdown: React.FC<YearDropdownProps> = ({ value, onChange }) => {
    const currentYear = new Date().getFullYear();
    const years: number[] = [];
    
    // Generate years from current year back to 100 years ago
    for (let year = currentYear; year >= currentYear - 100; year--) {
        years.push(year);
    }

    return (
        <select
            value={value}
            onChange={(e) => onChange(e.target.value)}
            className="w-full px-4 py-3 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-purple-500 focus:ring-2 focus:ring-purple-200 transition-all bg-white"
        >
            <option value="">Year... 📅</option>
            {years.map((year) => (
                <option key={year} value={year}>
                    {year}
                </option>
            ))}
        </select>
    );
};

export default YearDropdown;

