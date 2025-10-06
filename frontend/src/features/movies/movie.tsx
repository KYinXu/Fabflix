import React from 'react';
import { useParams } from 'react-router-dom';
import type { Movie } from '../../types/movie';
import InfoDisplay from './components/infoDisplay';
import BackButton from '../../components/BackButton';

const Movie: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  
  // Mock data for example movie
  const mockMovie: Movie = {
    id: id || '1',
    title: 'The Shawshank Redemption',
    year: 1994,
    director: 'Frank Darabont',
    stars: [
      { id: '1', name: 'Tim Robbins', birth_year: 1958 },
      { id: '2', name: 'Morgan Freeman', birth_year: 1937 }
    ],
    genres: [
      { id: 1, name: 'Drama' }
    ],
    rating: {
      movie_id: id || '1',
      ratings: 9.3,
      vote_count: 2500000
    }
  };

  return (
    <div className="movie-detail">
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-4xl mx-auto">
          <BackButton text="Back to Movie List" />
          <InfoDisplay movie={mockMovie} />
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
