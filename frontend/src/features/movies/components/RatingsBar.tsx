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
        <h2 className="text-2xl font-bold" style={{ color: 'var(--theme-text-primary)' }}>Rating</h2>
        <div className="text-3xl font-bold" style={{ color: 'var(--theme-text-primary)' }}>
          {rating.ratings.toFixed(1)} <span className="text-xl" style={{ color: 'var(--theme-text-muted)' }}>/ 10</span>
        </div>
      </div>
      
      {/* Progress Bar */}
      <div className="w-full rounded-full h-8" style={{ backgroundColor: 'var(--theme-bg-tertiary)' }}>
        <div 
          className="h-8 rounded-full transition-all duration-500 flex items-center justify-end pr-3"
          style={{ 
            width: `${(rating.ratings / 10) * 100}%`,
            background: 'linear-gradient(to right, var(--theme-warning), #f59e0b)'
          }}
        >
          <span className="text-white text-sm font-bold">
            {rating.ratings.toFixed(1)}
          </span>
        </div>
      </div>
      
      <div style={{ color: 'var(--theme-text-secondary)' }}>
        Based on {rating.vote_count.toLocaleString()} votes
      </div>
    </div>
  );
};

export default RatingsBar;

