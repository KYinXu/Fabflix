import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { Movie } from '@/types/types';
import AddToCartButton from "../../../components/AddToCartButton";

interface MovieListItemProps {
    movie: Movie;
    onGenreClick?: (genreId: number) => void;
}

const MovieListItem: React.FC<MovieListItemProps> = ({ movie, onGenreClick }) => {
    const navigate = useNavigate();
    
    const handleCardClick = (e: React.MouseEvent) => {
        // Only navigate if the click wasn't on a link or interactive element
        if ((e.target as HTMLElement).closest('a')) {
            return;
        }
        navigate(`/movie/${movie.id}`);
    };
    
    const handleGenreClick = (e: React.MouseEvent, genreId: number) => {
        e.stopPropagation(); // Prevent card click
        if (onGenreClick) {
            onGenreClick(genreId);
        }
    };
    
    return (
        <div
            onClick={handleCardClick}
            className="p-6 rounded transition-all duration-300 cursor-pointer block group h-full flex flex-col"
            style={{ 
                backgroundColor: 'var(--theme-bg-secondary)'
            }}
        >
            <div className="flex justify-between items-start mb-3">
                <h2 className="text-2xl font-bold line-clamp-2 flex-1" style={{ color: 'var(--theme-text-primary)' }}>
                    {movie.title}
                </h2>
                {movie.ratings && (
                    <div className="flex items-center ml-3 bg-yellow-100 px-2 py-1 rounded">
                        <span className="text-yellow-600 mr-1">⭐</span>
                        <span className="font-bold text-gray-800">{movie.ratings.ratings?.toFixed(1)}</span>
                    </div>
                )}
            </div>
            
            <div className="space-y-3 mb-4 flex-1" style={{ color: 'var(--theme-text-secondary)' }}>
                <p className="flex items-center">
                    <span className="font-semibold mr-2" style={{ color: 'var(--theme-text-primary)' }}>Year:</span>
                    <span>{movie.year}</span>
                </p>
                <p className="flex items-center">
                    <span className="font-semibold mr-2" style={{ color: 'var(--theme-text-primary)' }}>Director:</span>
                    <span className="truncate">{movie.director}</span>
                </p>
                
                {movie.genres && movie.genres.length > 0 && (
                    <div>
                        <span className="font-semibold block mb-1" style={{ color: 'var(--theme-text-primary)' }}>Genres:</span>
                        <div className="flex flex-wrap gap-2">
                            {movie.genres.map((genre) => (
                                <span
                                    key={genre.id}
                                    className="px-2 py-1 text-sm rounded-full cursor-pointer transition-all duration-200"
                                    style={{ 
                                        backgroundColor: 'rgba(109, 40, 217, 0.1)', 
                                        color: '#6d28d9',
                                        border: '1px solid rgba(109, 40, 217, 0.3)'
                                    }}
                                    onClick={(e) => handleGenreClick(e, genre.id)}
                                    onMouseEnter={(e) => {
                                        e.currentTarget.style.backgroundColor = '#6d28d9';
                                        e.currentTarget.style.color = 'white';
                                        e.currentTarget.style.borderColor = '#6d28d9';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.currentTarget.style.backgroundColor = 'rgba(109, 40, 217, 0.1)';
                                        e.currentTarget.style.color = '#6d28d9';
                                        e.currentTarget.style.borderColor = 'rgba(109, 40, 217, 0.3)';
                                    }}
                                >
                                    {genre.name}
                                </span>
                            ))}
                        </div>
                    </div>
                )}
                
                {movie.stars && movie.stars.length > 0 && (
                    <div>
                        <span className="font-semibold block mb-2" style={{ color: 'var(--theme-text-primary)' }}>Stars:</span>
                        <div className="flex flex-wrap gap-2">
                            {movie.stars.map((star) => (
                                <Link
                                    key={star.id}
                                    to={`/star/${star.id}`}
                                    className="px-2 py-1 text-sm rounded-full transition-all duration-200"
                                    style={{ 
                                        backgroundColor: 'rgba(59, 130, 246, 0.1)', 
                                        color: '#3b82f6',
                                        border: '1px solid rgba(59, 130, 246, 0.3)'
                                    }}
                                    onMouseEnter={(e) => {
                                        e.currentTarget.style.backgroundColor = '#3b82f6';
                                        e.currentTarget.style.color = 'white';
                                        e.currentTarget.style.borderColor = '#3b82f6';
                                    }}
                                    onMouseLeave={(e) => {
                                        e.currentTarget.style.backgroundColor = 'rgba(59, 130, 246, 0.1)';
                                        e.currentTarget.style.color = '#3b82f6';
                                        e.currentTarget.style.borderColor = 'rgba(59, 130, 246, 0.3)';
                                    }}
                                >
                                    {star.name}
                                </Link>
                            ))}
                        </div>
                    </div>
                )}
            </div>
            
            <div className="flex justify-end">
                <AddToCartButton movie={movie} />
            </div>
        </div>
    );
};

export default MovieListItem;

