import React, { useState } from 'react';
import SearchBar from './SearchBar';

interface SearchSectionProps {
    onSearch: (movieQuery: string, starQuery: string) => void;
}

const SearchSection: React.FC<SearchSectionProps> = ({ onSearch }) => {
    const [movieSearch, setMovieSearch] = useState('');
    const [starSearch, setStarSearch] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        onSearch(movieSearch, starSearch);
    };

    return (
        <div className="container mx-auto px-4 mb-8">
            <form onSubmit={handleSubmit} className="max-w-6xl mx-auto">
                <div className="flex flex-col md:flex-row gap-4 items-stretch md:items-center">
                    {/* Movie Search Bar */}
                    <div className="flex-1">
                        <SearchBar
                            value={movieSearch}
                            onChange={setMovieSearch}
                            placeholder="Search for movies... 🎬"
                            focusColor="blue"
                        />
                    </div>

                    {/* Star Search Bar */}
                    <div className="flex-1">
                        <SearchBar
                            value={starSearch}
                            onChange={setStarSearch}
                            placeholder="Search for stars... ⭐"
                            focusColor="purple"
                        />
                    </div>

                    {/* Search Button */}
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

