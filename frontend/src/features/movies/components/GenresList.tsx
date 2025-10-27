import React from 'react';
import { useNavigate } from 'react-router-dom';

interface Genre {
  id: number;
  name: string;
}

interface GenresListProps {
  genres: Genre[];
}

const GenresList: React.FC<GenresListProps> = ({ genres }) => {
  const navigate = useNavigate();

  if (!genres || genres.length === 0) {
    return null;
  }

  const handleGenreClick = (genreId: number) => {
    // Navigate to movie list with genre filter
    navigate(`/?genreId=${genreId}`);
  };

  return (
    <div className="flex flex-wrap gap-2">
      {genres.map((genre) => (
        <button
          key={genre.id}
          onClick={() => handleGenreClick(genre.id)}
          className="px-4 py-1.5 rounded-full text-sm font-medium transition-colors cursor-pointer"
          style={{
            backgroundColor: 'var(--theme-primary)',
            color: 'var(--theme-text-primary)'
          }}
          onMouseEnter={(e) => {
            const target = e.target as HTMLElement;
            target.style.backgroundColor = 'var(--theme-primary-hover)';
          }}
          onMouseLeave={(e) => {
            const target = e.target as HTMLElement;
            target.style.backgroundColor = 'var(--theme-primary)';
          }}
        >
          {genre.name}
        </button>
      ))}
    </div>
  );
};

export default GenresList;

