import React, { useState, useEffect } from 'react';
import SearchBar from './SearchBar';
import YearDropdown from './YearDropdown';
import { SearchState } from '@/types/session';

interface SearchSectionProps {
    onSearch: (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => void;
    initialValues?: SearchState;
}

const SearchSection: React.FC<SearchSectionProps> = ({ onSearch, initialValues }) => {
    const [movieSearch, setMovieSearch] = useState(initialValues?.movieQuery || '');
    const [starSearch, setStarSearch] = useState(initialValues?.starQuery || '');
    const [directorSearch, setDirectorSearch] = useState(initialValues?.directorQuery || '');
    const [yearSearch, setYearSearch] = useState(initialValues?.yearQuery || '');

    // Update state when initialValues change
    useEffect(() => {
        if (initialValues) {
            setMovieSearch(initialValues.movieQuery || '');
            setStarSearch(initialValues.starQuery || '');
            setDirectorSearch(initialValues.directorQuery || '');
            setYearSearch(initialValues.yearQuery || '');
        }
    }, [initialValues]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSearch(movieSearch, starSearch, directorSearch, yearSearch);
    };

    return (
        <div className="container mx-auto px-4 mb-8">
            <form onSubmit={handleSubmit} className="max-w-7xl mx-auto">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
                    {/* Movie Search Bar */}
                    <SearchBar
                        value={movieSearch}
                        onChange={setMovieSearch}
                        placeholder="Movie title... 🎬"
                        focusColor="blue"
                    />

                    {/* Star Search Bar */}
                    <SearchBar
                        value={starSearch}
                        onChange={setStarSearch}
                        placeholder="Star name... ⭐"
                        focusColor="purple"
                    />

                    {/* Director Search Bar */}
                    <SearchBar
                        value={directorSearch}
                        onChange={setDirectorSearch}
                        placeholder="Director name... 🎥"
                        focusColor="blue"
                    />

                    {/* Year Dropdown */}
                    <YearDropdown
                        value={yearSearch}
                        onChange={setYearSearch}
                    />
                </div>

                {/* Search Button - Centered below */}
                <div className="flex justify-center">
                    <button
                        type="submit"
                        className="px-8 py-3 text-white font-semibold rounded-lg shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105 whitespace-nowrap"
                        style={{
                            background: 'linear-gradient(to right, var(--theme-primary), var(--theme-secondary), var(--theme-accent))'
                        }}
                        onMouseEnter={(e) => {
                            const target = e.target as HTMLElement;
                            target.style.background = 'linear-gradient(to right, var(--theme-primary-hover), var(--theme-secondary-hover), var(--theme-accent-hover))';
                        }}
                        onMouseLeave={(e) => {
                            const target = e.target as HTMLElement;
                            target.style.background = 'linear-gradient(to right, var(--theme-primary), var(--theme-secondary), var(--theme-accent))';
                        }}
                    >
                        Search
                    </button>
                </div>
            </form>
        </div>
    );
};

export default SearchSection;

