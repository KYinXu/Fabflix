import React from 'react';
import { useParams } from 'react-router-dom';
import { useFetchStar } from './hooks/useFetchStar';
import BackButton from '../../components/BackButton';
import RolesList from './components/RolesList';

const Star: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { data: star, loading, error } = useFetchStar(id || '');

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl" style={{ color: 'var(--theme-text-primary)' }}>Loading star information...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl" style={{ color: 'var(--theme-error)' }}>Error: {error}</div>
      </div>
    );
  }

  if (!star) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-xl" style={{ color: 'var(--theme-text-primary)' }}>Star not found</div>
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="max-w-4xl mx-auto space-y-8">
        <BackButton text="Back to Movie List" />
        
        {/* Star Header */}
        <div className="pb-6" style={{ borderBottom: '4px solid var(--theme-secondary)' }}>
          <h1 className="text-5xl font-bold mb-2" style={{ color: 'var(--theme-text-primary)' }}>{star.name}</h1>
          {star.birth_year && (
            <p className="text-lg" style={{ color: 'var(--theme-text-secondary)' }}>
              Born: <span className="font-semibold" style={{ color: 'var(--theme-text-primary)' }}>{star.birth_year}</span>
            </p>
          )}
        </div>

        {/* Movies Section */}
        <RolesList movies={star.movies || []} />
      </div>
    </div>
  );
};

export default Star;