import React from 'react';
import RolesDisplay from './RolesDisplay';

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

interface RolesListProps {
  movies: Movie[];
}

const RolesList: React.FC<RolesListProps> = ({ movies }) => {
  if (!movies || movies.length === 0) {
    return <p style={{ color: 'var(--theme-text-muted)' }}>No movies found for this star.</p>;
  }

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold" style={{ color: 'var(--theme-text-primary)' }}>Movies</h2>
      <div className="space-y-4">
        {movies.map((movie) => (
          <RolesDisplay key={movie.id} movie={movie} />
        ))}
      </div>
    </div>
  );
};

export default RolesList;

