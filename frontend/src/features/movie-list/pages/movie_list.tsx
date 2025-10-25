import React, { useState } from 'react';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import { useFetchGenres } from "../hooks/useFetchGenres";
import MovieListGrid from '../components/MovieListGrid';
import SearchSection from '../components/SearchSection';
import BrowseSection from '../components/BrowseSection';

const MovieList: React.FC = () => {
    const {data, loading, error, searchMovies, browseMovies} = useFetchMovieList(); // create state by calling hook
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
                genres={genres}
            />
            
            {data && <MovieListGrid movies={data} />}
        </div>
    );
};

export default MovieList;
