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
    const [selectedIndex, setSelectedIndex] = useState(-1);
    const containerRef = useRef<HTMLDivElement>(null);
    const suggestionRefs = useRef<(HTMLAnchorElement | null)[]>([]);
    
    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
            // Navigate to selected movie
            window.location.href = `/movie/${suggestions[selectedIndex].id}`;
        } else if (value.trim() && onSearch) {
            onSearch(value.trim());
        }
        setIsOpen(false);
        setSelectedIndex(-1);
    };

    const handleKeyDown = (e: React.KeyboardEvent) => {
        if (!isOpen || suggestions.length === 0) {
            if (e.key === 'ArrowDown' && isOpen && suggestions.length > 0) {
                e.preventDefault();
                setSelectedIndex(0);
            }
            return;
        }

        switch (e.key) {
            case 'ArrowDown':
                e.preventDefault();
                setSelectedIndex(prev => 
                    prev < suggestions.length - 1 ? prev + 1 : prev
                );
                break;
            case 'ArrowUp':
                e.preventDefault();
                setSelectedIndex(prev => prev > 0 ? prev - 1 : -1);
                break;
            case 'Enter':
                e.preventDefault();
                if (selectedIndex >= 0 && selectedIndex < suggestions.length) {
                    window.location.href = `/movie/${suggestions[selectedIndex].id}`;
                    setIsOpen(false);
                    setSelectedIndex(-1);
                } else {
                    handleSubmit(e);
                }
                break;
            case 'Escape':
                e.preventDefault();
                setIsOpen(false);
                setSelectedIndex(-1);
                break;
        }
    };

    // Scroll selected item into view
    useEffect(() => {
        if (selectedIndex >= 0 && suggestionRefs.current[selectedIndex]) {
            suggestionRefs.current[selectedIndex]?.scrollIntoView({
                block: 'nearest',
                behavior: 'smooth'
            });
        }
    }, [selectedIndex]);

    useEffect(() => {
        setIsOpen(value.length > 0 && (loading || suggestions.length > 0));
        setSelectedIndex(-1); // Reset selection when suggestions change
        suggestionRefs.current = [];
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
                <SearchBar 
                    value={value} 
                    onChange={onChange} 
                    placeholder={placeholder} 
                    focusColor={focusColor}
                    onKeyDown={handleKeyDown}
                />
            </form>
            {isOpen && (
                <div className="absolute z-50 w-full mt-1 border-2 rounded-lg shadow-lg"
                     style={{ borderColor: 'var(--theme-border-primary)', backgroundColor: 'var(--theme-bg-secondary)' }}>
                    {loading && <div className="px-4 py-2 text-center">Loading...</div>}
                    {!loading && suggestions.length === 0 && <div className="px-4 py-2 text-center">No results</div>}
                    {!loading && suggestions.map((movie, index) => (
                        <Link 
                            key={movie.id} 
                            to={`/movie/${movie.id}`} 
                            ref={el => { suggestionRefs.current[index] = el; }}
                            className={`block px-4 py-2 ${
                                selectedIndex === index 
                                    ? 'bg-gray-200 dark:bg-gray-600' 
                                    : 'hover:bg-gray-100 dark:hover:bg-gray-700'
                            }`}
                            onClick={() => {
                                setIsOpen(false);
                                setSelectedIndex(-1);
                            }}
                            onMouseEnter={() => setSelectedIndex(index)}
                        >
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

