import React from 'react';

interface Genre {
  id: number;
  name: string;
}

interface GenresListProps {
  genres: Genre[];
}

const GenresList: React.FC<GenresListProps> = ({ genres }) => {
  if (!genres || genres.length === 0) {
    return null;
  }

  return (
    <div className="flex flex-wrap gap-2">
      {genres.map((genre) => (
        <span
          key={genre.id}
          className="px-4 py-1.5 bg-blue-100 text-blue-700 rounded-full text-sm font-medium"
        >
          {genre.name}
        </span>
      ))}
    </div>
  );
};

export default GenresList;

