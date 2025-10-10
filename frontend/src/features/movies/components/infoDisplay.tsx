import React from 'react';
import type { Movie } from '../types/movie';
import {Link} from "react-router-dom";

interface InfoDisplayProps {
  movie: Movie;
}

const InfoDisplay: React.FC<InfoDisplayProps> = ({ movie }) => {
  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      {/* Header Section */}
      <div className="bg-gradient-to-r from-blue-600 to-purple-600 text-white p-6">
        <h1 className="text-4xl font-bold mb-2">{movie.title}</h1>
        <div className="flex items-center space-x-4 text-lg">
          <span>{movie.year}</span>
          <span>•</span>
          <span>Directed by {movie.director}</span>
        </div>
      </div>

      <div className="p-6">
        {/* Rating Section */}
        {movie.rating && (
          <div className="mb-6 p-4 bg-gray-50 rounded-lg">
            <h3 className="text-xl font-semibold mb-2">Rating</h3>
            <div className="flex items-center space-x-4">
              <div className="text-3xl font-bold text-yellow-500">
                ⭐ {movie.rating.ratings.toFixed(1)}
              </div>
              <div className="text-gray-600">
                Based on {movie.rating.vote_count.toLocaleString()} votes
              </div>
            </div>
          </div>
        )}

        {/* Stars Section */}
        {movie.stars && movie.stars.length > 0 && (
          <div className="mb-6">
            <h3 className="text-xl font-semibold mb-3">Cast</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {movie.stars.map((star) => (
                <Link to={`/star/${star.id}`} key={star.id}>
                <div key={star.id} className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
                  <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center text-white font-semibold">
                    {star.name.charAt(0)}
                  </div>
                  <div>
                    <div className="font-medium">{star.name}</div>
                    {star.birth_year && (
                      <div className="text-sm text-gray-600">
                        Born: {star.birth_year}
                      </div>
                    )}
                  </div>
                </div>
                </Link>
              ))}
            </div>
          </div>
        )}

        {/* Genres Section */}
        {movie.genres && movie.genres.length > 0 && (
          <div className="mb-6">
            <h3 className="text-xl font-semibold mb-3">Genres</h3>
            <div className="flex flex-wrap gap-2">
              {movie.genres.map((genre) => (
                <span
                  key={genre.id}
                  className="px-3 py-1 bg-blue-100 text-blue-800 rounded-full text-sm font-medium"
                >
                  {genre.name}
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Movie ID for reference */}
        <div className="text-sm text-gray-500 border-t pt-4">
          Movie ID: {movie.id}
        </div>
      </div>
    </div>
  );
};

export default InfoDisplay;

