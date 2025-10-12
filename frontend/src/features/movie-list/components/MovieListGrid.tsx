import React from 'react';
import type { Movie } from '@/types/types';
import MovieListItem from './MovieListItem';

interface MovieListGridProps {
    movies: Movie[];
}

const MovieListGrid: React.FC<MovieListGridProps> = ({ movies }) => {
    return (
        <div className="container mx-auto px-4 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {movies.map((movie) => (
                <MovieListItem key={movie.id} movie={movie} />
            ))}
        </div>
    );
};

export default MovieListGrid;

