import React from 'react';
import { useParams, Link } from 'react-router-dom';
import { useFetchStar } from './hooks/useFetchStar';

const Star: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { data: star, loading, error } = useFetchStar(id || '');

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl">Loading star information...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl text-red-600">Error: {error}</div>
      </div>
    );
  }

  if (!star) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl">Star not found</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto">
        {/* Star Header */}
        <div className="bg-white shadow-lg rounded-lg p-6 mb-6">
          <h1 className="text-4xl font-bold mb-4">{star.name}</h1>
          {star.birth_year && (
            <p className="text-gray-600 text-lg">
              Born: {star.birth_year}
            </p>
          )}
        </div>

        {/* Movies Section */}
        <div className="bg-white shadow-lg rounded-lg p-6">
          <h2 className="text-2xl font-bold mb-4">Movies</h2>
          
          {star.movies && star.movies.length > 0 ? (
            <div className="space-y-3">
              {star.movies.map((movie) => (
                <Link
                  key={movie.id}
                  to={`/movie/${movie.id}`}
                  className="block p-4 border border-gray-200 rounded-lg hover:bg-gray-50 hover:border-blue-500 transition-colors"
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="text-xl font-semibold text-blue-600 hover:text-blue-800">
                        {movie.title}
                      </h3>
                      <p className="text-gray-600 mt-1">
                        Directed by {movie.director}
                      </p>
                    </div>
                    <span className="text-gray-500 font-medium">
                      {movie.year}
                    </span>
                  </div>
                  
                  {movie.rating && (
                    <div className="mt-2">
                      <span className="text-yellow-500">
                        {`★ {movie.rating.ratings.toFixed(1)}`}
                      </span>
                    </div>
                  )}
                </Link>
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No movies found for this star.</p>
          )}
        </div>

        {/* Back Button */}
        <div className="mt-6">
          <Link
            to="/"
            className="inline-block px-6 py-2 bg-gray-200 hover:bg-gray-300 rounded-lg transition-colors"
          >
            ← Back to Home
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Star;