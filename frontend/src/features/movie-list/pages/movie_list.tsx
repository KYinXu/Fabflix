import React from 'react';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import MovieListGrid from '../components/MovieListGrid';
import SearchSection from '../components/SearchSection';

const MovieList: React.FC = () => {
    const {data, loading, error, searchMovies} = useFetchMovieList(); // create state by calling hook
    
    const handleSearch = (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        // Search movies by title, star, director, and year (empty strings show all movies)
        searchMovies(movieQuery, starQuery, directorQuery, yearQuery);
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
            
            {data && <MovieListGrid movies={data} />}
        </div>
    );
};

export default MovieList;
