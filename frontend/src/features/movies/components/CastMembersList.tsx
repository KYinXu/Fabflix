import React from 'react';
import CastMember from './CastMember';

interface Star {
  id: string;
  name: string;
  birth_year?: number;
}

interface CastMembersListProps {
  stars: Star[];
}

const CastMembersList: React.FC<CastMembersListProps> = ({ stars }) => {
  if (!stars || stars.length === 0) {
    return null;
  }

  return (
    <div className="space-y-4">
      <h2 className="text-2xl font-bold text-gray-900">Cast</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {stars.map((star) => (
          <CastMember key={star.id} star={star} />
        ))}
      </div>
    </div>
  );
};

export default CastMembersList;

