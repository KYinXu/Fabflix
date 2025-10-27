import React from 'react';
import { useParams } from 'react-router-dom';
import { useFetchMovie } from './hooks/useFetchMovie';
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
            <div className="text-center py-8" style={{ color: 'var(--theme-text-primary)' }}>Loading movie information...</div>
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
              <p className="mb-4" style={{ color: 'var(--theme-error)' }}>Error: {error}</p>
              <button 
                onClick={refetch}
                className="px-4 py-2 text-white rounded hover:opacity-80 transition-colors duration-200"
                style={{ backgroundColor: 'var(--theme-primary)' }}
                onMouseEnter={(e) => {
                  const target = e.target as HTMLElement;
                  target.style.backgroundColor = 'var(--theme-primary-hover)';
                }}
                onMouseLeave={(e) => {
                  const target = e.target as HTMLElement;
                  target.style.backgroundColor = 'var(--theme-primary)';
                }}
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
            <div className="text-center py-8" style={{ color: 'var(--theme-text-primary)' }}>Movie not found</div>
          )}
        </div>
      </div>
    </div>
  );
};
export default Movie;
