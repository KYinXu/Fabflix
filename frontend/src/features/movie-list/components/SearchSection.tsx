import React, { useState } from 'react';
import SearchBar from './SearchBar';
import YearDropdown from './YearDropdown';

interface SearchSectionProps {
    onSearch: (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => void;
}

const SearchSection: React.FC<SearchSectionProps> = ({ onSearch }) => {
    const [movieSearch, setMovieSearch] = useState('');
    const [starSearch, setStarSearch] = useState('');
    const [directorSearch, setDirectorSearch] = useState('');
    const [yearSearch, setYearSearch] = useState('');

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
                        className="px-8 py-3 bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 hover:from-blue-600 hover:via-purple-600 hover:to-pink-600 text-white font-semibold rounded-lg shadow-lg hover:shadow-xl transition-all duration-300 transform hover:scale-105 whitespace-nowrap"
                    >
                        Search
                    </button>
                </div>
            </form>
        </div>
    );
};

export default SearchSection;

