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
      className="block p-5 border border-gray-200 rounded-lg hover:border-blue-500 hover:shadow-md transition-all"
    >
      <div className="flex justify-between items-start mb-2">
        <div className="flex-1">
          <h3 className="text-xl font-semibold text-gray-900 mb-1">
            {movie.title}
          </h3>
          <p className="text-gray-600">
            Directed by <span className="font-medium">{movie.director}</span>
          </p>
        </div>
        <span className="text-lg text-gray-700 font-semibold ml-4">
          {movie.year}
        </span>
      </div>
      
      {movie.ratings && (
        <div className="flex items-center gap-2 mt-3 pt-3 border-t border-gray-100">
          <div className="flex items-center gap-1">
            <span className="text-yellow-500 text-lg">★</span>
            <span className="text-gray-700 font-medium">
              {movie.ratings.ratings.toFixed(1)} / 10
            </span>
          </div>
        </div>
      )}
    </Link>
  );
};

export default RolesDisplay;

