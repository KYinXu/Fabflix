import React from 'react';
import type { Movie } from '@/types/types';
import CastMembersList from './CastMembersList';
import RatingsBar from './RatingsBar';
import GenresList from './GenresList';

interface InfoDisplayProps {
  movie: Movie;
}

const InfoDisplay: React.FC<InfoDisplayProps> = ({ movie }) => {
  return (
    <div className="space-y-8">
      {/* Header Section */}
      <div className="border-b-4 border-blue-600 pb-6">
        <h1 className="text-5xl font-bold text-gray-900 mb-4">{movie.title}</h1>
        
        {/* Genres Section */}
        {movie.genres && movie.genres.length > 0 && (
          <div className="mb-4">
            <GenresList genres={movie.genres} />
          </div>
        )}
        
        <div className="flex items-center space-x-4 text-lg text-gray-600">
          <span className="font-semibold">{movie.year}</span>
          <span>•</span>
          <span>Directed by <span className="font-semibold text-gray-900">{movie.director}</span></span>
        </div>
      </div>

      {/* Rating Section */}
      {movie.ratings && <RatingsBar rating={movie.ratings} />}

      {/* Cast Section */}
      {movie.stars && <CastMembersList stars={movie.stars} />}

      {/* Movie ID for reference */}
      <div className="text-sm text-gray-400 pt-4">
        Movie ID: {movie.id}
      </div>
    </div>
  );
};

export default InfoDisplay;

