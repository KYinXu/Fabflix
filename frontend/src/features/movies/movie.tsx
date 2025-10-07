import React from 'react';
import { useParams } from 'react-router-dom';
import { useFetchMovie } from './hooks/useFetch';
import InfoDisplay from './components/infoDisplay';
import BackButton from '../../components/BackButton';

const Movie: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { data: movie, loading, error, refetch } = useFetchMovie(id || '');

  if (loading) {
    return (
      <div className="movie-detail">
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-4xl mx-auto">
            <BackButton text="Back to Movie List" />
            <div className="text-center py-8">Loading movie information...</div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="movie-detail">
        <div className="container mx-auto px-4 py-8">
          <div className="max-w-4xl mx-auto">
            <BackButton text="Back to Movie List" />
            <div className="text-center py-8">
              <p className="text-red-600 mb-4">Error: {error}</p>
              <button 
                onClick={refetch}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                Retry
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="movie-detail">
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <BackButton text="Back to Movie List" />
          {movie ? (
            <InfoDisplay movie={movie} />
          ) : (
            <div className="text-center py-8">Movie not found</div>
          )}
        </div>
      </div>
    </div>
  );
};

/**
 * Movie page information:
 * title, year, director, stars, genres, rating
 */
export default Movie;
