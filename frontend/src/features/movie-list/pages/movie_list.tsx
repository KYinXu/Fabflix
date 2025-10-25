import React, { useState } from 'react';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import { useFetchGenres } from "../hooks/useFetchGenres";
import MovieListGrid from '../components/MovieListGrid';
import SearchSection from '../components/SearchSection';
import BrowseSection from '../components/BrowseSection';

const MovieList: React.FC = () => {
    const {data, loading, error, currentPage, hasNextPage, pageSize, searchMovies, browseMovies, browseByGenre, goToNextPage, goToPreviousPage, setPageSize} = useFetchMovieList(); // create state by calling hook
    const {data: genres} = useFetchGenres(); // fetch genres
    const [browseType, setBrowseType] = useState<'title' | 'genre'>('title');
    
    const handleSearch = (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        // Search movies by title, star, director, and year (empty strings show all movies)
        searchMovies(movieQuery, starQuery, directorQuery, yearQuery);
    };

    const handleBrowseTypeChange = (type: 'title' | 'genre') => {
        setBrowseType(type);
        if (type === 'title') {
            browseMovies('All');
        }
    };

    const handleLetterChange = (letter: string) => {
        // Only trigger browse if we're on the title tab
        if (browseType === 'title') {
            browseMovies(letter);
        }
    };

    const handleGenreChange = (genreId: number) => {
        // Only trigger browse if we're on the genre tab
        if (browseType === 'genre') {
            browseByGenre(genreId);
        }
    };

    if (error){
        return (
            <div className="movie-list-error">
                <h1 className="text-3xl font-bold text-red-700 text-center">ERROR: {error}</h1>
            </div>
        )
    }
    if (loading){
        return (
            <div className="movie-list">
                <h1 className="text-6xl font-extrabold text-center mb-8 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent py-4">
                    Fabflix
                </h1>
                
                <SearchSection onSearch={handleSearch} />
                
                <BrowseSection 
                    onBrowseTypeChange={handleBrowseTypeChange}
                    onLetterChange={handleLetterChange}
                    onGenreChange={handleGenreChange}
                    genres={genres}
                />
                
                <div className="container mx-auto px-4">
                    <p className="text-gray-600 text-center">
                        Filler text
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="movie-list">
            <h1 className="text-6xl font-extrabold text-center mb-8 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent py-4">
                Fabflix
            </h1>
            
            <SearchSection onSearch={handleSearch} />
            
            <BrowseSection 
                onBrowseTypeChange={handleBrowseTypeChange}
                onLetterChange={handleLetterChange}
                onGenreChange={handleGenreChange}
                genres={genres}
            />
            
            {data && <MovieListGrid movies={data} />}
            
            {/* Pagination Controls */}
            {data && data.length > 0 && (
                <div className="flex flex-col items-center gap-6 mt-8">
                    <div className="flex items-center gap-2">
                        <label htmlFor="pageSize" className="text-gray-600 font-medium">
                            Movies per page:
                        </label>
                        <select
                            id="pageSize"
                            value={pageSize}
                            onChange={(e) => setPageSize(Number(e.target.value))}
                            className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </select>
                    </div>
                    <div className="flex justify-center items-center gap-4">
                        <button
                            onClick={goToPreviousPage}
                            disabled={currentPage === 0}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                currentPage === 0
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white hover:shadow-lg'
                            }`}
                        >
                            Previous Page
                        </button>
                        <span className="text-gray-600 font-medium">
                            Page {currentPage + 1}
                        </span>
                        <button
                            onClick={goToNextPage}
                            disabled={!hasNextPage}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                !hasNextPage
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white hover:shadow-lg'
                            }`}
                        >
                            Next Page
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MovieList;
