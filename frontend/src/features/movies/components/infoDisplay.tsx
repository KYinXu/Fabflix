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
      <div className="pb-6" style={{ borderBottom: '4px solid var(--theme-primary)' }}>
        <h1 className="text-5xl font-bold mb-4" style={{ color: 'var(--theme-text-primary)' }}>{movie.title}</h1>
        
        {/* Genres Section */}
        {movie.genres && movie.genres.length > 0 && (
          <div className="mb-4">
            <GenresList genres={movie.genres} />
          </div>
        )}
        
        <div className="flex items-center space-x-4 text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
          <span className="font-semibold">{movie.year}</span>
          <span>•</span>
          <span>Directed by <span className="font-semibold" style={{ color: 'var(--theme-text-primary)' }}>{movie.director}</span></span>
        </div>
      </div>

      {/* Rating Section */}
      {movie.ratings && <RatingsBar rating={movie.ratings} />}

      {/* Cast Section */}
      {movie.stars && <CastMembersList stars={movie.stars} />}

      {/* Movie ID for reference */}
      <div className="text-sm pt-4" style={{ color: 'var(--theme-text-muted)' }}>
        Movie ID: {movie.id}
      </div>
    </div>
  );
};

export default InfoDisplay;

