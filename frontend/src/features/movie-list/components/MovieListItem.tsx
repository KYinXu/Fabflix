import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { Movie } from '@/types/types';
import AddToCartButton from "../../../components/AddToCartButton";

interface MovieListItemProps {
    movie: Movie;
}

const MovieListItem: React.FC<MovieListItemProps> = ({ movie }) => {
    const navigate = useNavigate();
    
    const handleCardClick = (e: React.MouseEvent) => {
        // Only navigate if the click wasn't on a link or interactive element
        if ((e.target as HTMLElement).closest('a')) {
            return;
        }
        navigate(`/movie/${movie.id}`);
    };
    
    return (
        <div
            onClick={handleCardClick}
            className="p-6 border-2 border-gray-200 rounded-lg shadow-md hover:shadow-2xl hover:border-blue-500 hover:scale-105 transition-all duration-300 cursor-pointer block bg-white"
        >
            <div className="flex justify-between items-start mb-3">
                <h2 className="text-2xl font-bold text-gray-800 line-clamp-2 flex-1">
                    {movie.title}
                </h2>
                {movie.ratings && (
                    <div className="flex items-center ml-3 bg-yellow-100 px-2 py-1 rounded">
                        <span className="text-yellow-600 mr-1">⭐</span>
                        <span className="font-bold text-gray-800">{movie.ratings.ratings?.toFixed(1)}</span>
                    </div>
                )}
            </div>
            
            <div className="space-y-3 text-gray-600">
                <p className="flex items-center">
                    <span className="font-semibold text-gray-700 mr-2">Year:</span>
                    <span>{movie.year}</span>
                </p>
                <p className="flex items-center">
                    <span className="font-semibold text-gray-700 mr-2">Director:</span>
                    <span className="truncate">{movie.director}</span>
                </p>
                
                {movie.genres && movie.genres.length > 0 && (
                    <div>
                        <span className="font-semibold text-gray-700 block mb-1">Genres:</span>
                        <div className="flex flex-wrap gap-2">
                            {movie.genres.map((genre) => (
                                <span
                                    key={genre.id}
                                    className="px-2 py-1 bg-blue-100 text-blue-700 text-sm rounded-full"
                                >
                                    {genre.name}
                                </span>
                            ))}
                        </div>
                    </div>
                )}
                
                {movie.stars && movie.stars.length > 0 && (
                    <div>
                        <span className="font-semibold text-gray-700 block mb-2">Stars:</span>
                        <div className="flex flex-wrap gap-2">
                            {movie.stars.map((star) => (
                                <Link
                                    key={star.id}
                                    to={`/star/${star.id}`}
                                    className="px-3 py-1.5 bg-purple-100 text-purple-700 text-sm rounded-md hover:bg-purple-200 hover:shadow-md transition-all duration-200 font-medium"
                                >
                                    {star.name}
                                </Link>
                            ))}
                        </div>
                    </div>
                )}
            </div>
            <div className="flex justify-self-start container mx-auto px-4 mb-4">
                <AddToCartButton movie={movie} />
            </div>
        </div>
    );
};

export default MovieListItem;

