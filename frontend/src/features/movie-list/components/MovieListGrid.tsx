import React from 'react';
import type { Movie } from '@/types/types';
import MovieListItem from './MovieListItem';

interface MovieListGridProps {
    movies: Movie[];
    onGenreClick?: (genreId: number) => void;
}

const MovieListGrid: React.FC<MovieListGridProps> = ({ movies, onGenreClick }) => {
    return (
        <div className="w-full px-2 grid grid-cols-1 md:grid-cols-3 lg:grid-cols-5 gap-3">
            {movies.map((movie) => (
                <MovieListItem key={movie.id} movie={movie} onGenreClick={onGenreClick} />
            ))}
        </div>
    );
};

export default MovieListGrid;

