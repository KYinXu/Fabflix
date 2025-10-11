import React from 'react';
import { useParams, Link } from 'react-router-dom';
import { useFetchStar } from './hooks/useFetchStar';
import BackButton from '../../components/BackButton';

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
      <div className="max-w-4xl mx-auto space-y-8">
        <BackButton text="Back to Movie List" />
        
        {/* Star Header */}
        <div className="border-b-4 border-purple-600 pb-6">
          <h1 className="text-5xl font-bold text-gray-900 mb-2">{star.name}</h1>
          {star.birth_year && (
            <p className="text-lg text-gray-600">
              Born: <span className="font-semibold text-gray-900">{star.birth_year}</span>
            </p>
          )}
        </div>

        {/* Movies Section */}
        <div className="space-y-4">
          <h2 className="text-2xl font-bold text-gray-900">Movies</h2>
          
          {star.movies && star.movies.length > 0 ? (
            <div className="space-y-4">
              {star.movies.map((movie) => (
                <Link
                  key={movie.id}
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
              ))}
            </div>
          ) : (
            <p className="text-gray-500">No movies found for this star.</p>
          )}
        </div>
      </div>
    </div>
  );
};

export default Star;