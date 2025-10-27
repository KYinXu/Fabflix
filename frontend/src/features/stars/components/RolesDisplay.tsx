import React from 'react';
import { Link } from 'react-router-dom';

interface Movie {
  id: string;
  title: string;
  year: number;
  director: string;
  ratings?: {
    ratings: number;
    vote_count: number;
  };
}

interface RolesDisplayProps {
  movie: Movie;
}

const RolesDisplay: React.FC<RolesDisplayProps> = ({ movie }) => {
  return (
    <Link
      to={`/movie/${movie.id}`}
      className="block p-5 rounded-lg hover:shadow-md transition-all"
      style={{
        backgroundColor: 'var(--theme-bg-secondary)',
        borderColor: 'var(--theme-border-primary)',
        border: '1px solid'
      }}
      onMouseEnter={(e) => {
        const target = e.target as HTMLElement;
        target.style.borderColor = 'var(--theme-primary)';
      }}
      onMouseLeave={(e) => {
        const target = e.target as HTMLElement;
        target.style.borderColor = 'var(--theme-border-primary)';
      }}
    >
      <div className="flex justify-between items-start mb-2">
        <div className="flex-1">
          <h3 className="text-xl font-semibold mb-1" style={{ color: 'var(--theme-text-primary)' }}>
            {movie.title}
          </h3>
          <p style={{ color: 'var(--theme-text-secondary)' }}>
            Directed by <span className="font-medium" style={{ color: 'var(--theme-text-primary)' }}>{movie.director}</span>
          </p>
        </div>
        <span className="text-lg font-semibold ml-4" style={{ color: 'var(--theme-text-primary)' }}>
          {movie.year}
        </span>
      </div>
      
      {movie.ratings && (
        <div className="flex items-center gap-2 mt-3 pt-3" style={{ borderTop: '1px solid var(--theme-border-primary)' }}>
          <div className="flex items-center gap-1">
            <span className="text-yellow-500 text-lg">★</span>
            <span className="font-medium" style={{ color: 'var(--theme-text-primary)' }}>
              {movie.ratings.ratings.toFixed(1)} / 10
            </span>
          </div>
        </div>
      )}
    </Link>
  );
};

export default RolesDisplay;

