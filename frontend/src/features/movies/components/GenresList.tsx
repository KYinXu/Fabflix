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
          className="px-4 py-1.5 bg-blue-100 text-blue-700 rounded-full text-sm font-medium hover:bg-blue-200 hover:text-blue-800 transition-colors cursor-pointer"
        >
          {genre.name}
        </button>
      ))}
    </div>
  );
};

export default GenresList;

