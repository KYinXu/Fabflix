import React, { useState, useRef, useEffect } from 'react';
import { Link } from 'react-router-dom';
import SearchBar from './SearchBar';
import { useAutocompleteMovieSearch } from '../hooks/useAutocompleteMovieSearch';

interface AutocompleteSearchBarProps {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
    focusColor?: 'blue' | 'purple';
    onSearch?: (query: string) => void;
}

const AutocompleteSearchBar: React.FC<AutocompleteSearchBarProps> = ({ 
    value, 
    onChange, 
    placeholder = "Search movies...",
    focusColor = 'blue',
    onSearch
}) => {
    const { suggestions, loading } = useAutocompleteMovieSearch(value);
    const [isOpen, setIsOpen] = useState(false);
    const containerRef = useRef<HTMLDivElement>(null);
    
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (value.trim() && onSearch) {
            onSearch(value.trim());
        }
        setIsOpen(false);
    };

    useEffect(() => {
        setIsOpen(value.length > 0 && (loading || suggestions.length > 0));
    }, [value, suggestions, loading]);

    useEffect(() => {
        const handleClickOutside = (e: MouseEvent) => {
            if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    return (
        <div ref={containerRef} className="relative w-full">
            <form onSubmit={handleSubmit}>
                <SearchBar value={value} onChange={onChange} placeholder={placeholder} focusColor={focusColor} />
            </form>
            {isOpen && (
                <div className="absolute z-50 w-full mt-1 border-2 rounded-lg shadow-lg max-h-96 overflow-y-auto"
                     style={{ borderColor: 'var(--theme-border-primary)', backgroundColor: 'var(--theme-bg-secondary)' }}>
                    {loading && <div className="px-4 py-2 text-center">Loading...</div>}
                    {!loading && suggestions.length === 0 && <div className="px-4 py-2 text-center">No results</div>}
                    {!loading && suggestions.map(movie => (
                        <Link key={movie.id} to={`/movie/${movie.id}`} 
                              className="block px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700"
                              onClick={() => setIsOpen(false)}>
                            <div className="font-medium" style={{ color: 'var(--theme-text-primary)' }}>{movie.title}</div>
                            {movie.year && <div className="text-sm text-gray-500">{movie.year}</div>}
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
};

export default AutocompleteSearchBar;

