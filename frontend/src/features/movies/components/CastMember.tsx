import React from 'react';
import { Link } from 'react-router-dom';

interface Star {
  id: string;
  name: string;
  birth_year?: number;
}

interface CastMemberProps {
  star: Star;
}

const CastMember: React.FC<CastMemberProps> = ({ star }) => {
  return (
    <Link 
      to={`/star/${star.id}`} 
      className="flex items-center space-x-3 p-4 border border-gray-200 rounded-lg hover:border-blue-500 hover:shadow-md transition-all"
    >
      <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-600 rounded-full flex items-center justify-center text-white font-bold text-lg">
        {star.name.charAt(0)}
      </div>
      <div>
        <div className="font-semibold text-gray-900">{star.name}</div>
        {star.birth_year && (
          <div className="text-sm text-gray-500">
            Born: {star.birth_year}
          </div>
        )}
      </div>
    </Link>
  );
};

export default CastMember;

