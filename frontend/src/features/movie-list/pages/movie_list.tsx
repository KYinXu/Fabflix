import React from 'react';
import {useFetchMovieList} from "../hooks/useFetchMovieList";

const MovieList: React.FC = () => {
    const {data, loading, error} = useFetchMovieList(); // create state by calling hook

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
                <h1 className="text-3xl font-bold text-center mb-8">Fabflix</h1>
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
            <h1 className="text-3xl font-bold text-center mb-8">Fabflix</h1>
            <div className="container mx-auto px-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {data && data.map((movie) => (
                    <div key={movie.title} className="p-4 border rounded shadow">
                        <h2 className="text-xl font-semibold">{movie.title}</h2>
                        <p><strong>Year:</strong> {movie.year}</p>
                        <p><strong>Director:</strong> {movie.director}</p>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default MovieList;
