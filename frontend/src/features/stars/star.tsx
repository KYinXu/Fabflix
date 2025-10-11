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
        <RolesList movies={star.movies || []} />
      </div>
    </div>
  );
};

export default Star;