import React from 'react';

interface Rating {
  ratings: number;
  vote_count: number;
}

interface RatingsBarProps {
  rating: Rating;
}

const RatingsBar: React.FC<RatingsBarProps> = ({ rating }) => {
  return (
    <div className="space-y-3">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Rating</h2>
        <div className="text-3xl font-bold text-gray-900">
          {rating.ratings.toFixed(1)} <span className="text-xl text-gray-500">/ 10</span>
        </div>
      </div>
      
      {/* Progress Bar */}
      <div className="w-full bg-gray-200 rounded-full h-8">
        <div 
          className="bg-gradient-to-r from-yellow-400 to-yellow-500 h-8 rounded-full transition-all duration-500 flex items-center justify-end pr-3"
          style={{ width: `${(rating.ratings / 10) * 100}%` }}
        >
          <span className="text-white text-sm font-bold">
            {rating.ratings.toFixed(1)}
          </span>
        </div>
      </div>
      
      <div className="text-gray-600">
        Based on {rating.vote_count.toLocaleString()} votes
      </div>
    </div>
  );
};

export default RatingsBar;

