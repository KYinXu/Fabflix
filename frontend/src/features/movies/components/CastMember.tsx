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
      className="flex items-center space-x-3 p-4 rounded-lg hover:shadow-md transition-all"
      style={{
        backgroundColor: 'var(--theme-bg-secondary)',
        borderColor: 'var(--theme-border-primary)',
        border: '1px solid'
      }}
      onMouseEnter={(e) => {
        const target = e.target as HTMLElement;
        target.style.borderColor = 'var(--theme-primary)';
      }}
      onMouseLeave={(e) => {
        const target = e.target as HTMLElement;
        target.style.borderColor = 'var(--theme-border-primary)';
      }}
    >
      <div className="w-12 h-12 rounded-full flex items-center justify-center text-white font-bold text-lg"
           style={{
             background: 'linear-gradient(to bottom right, var(--theme-primary), var(--theme-secondary))'
           }}>
        {star.name.charAt(0)}
      </div>
      <div>
        <div className="font-semibold" style={{ color: 'var(--theme-text-primary)' }}>{star.name}</div>
        {star.birth_year && (
          <div className="text-sm" style={{ color: 'var(--theme-text-muted)' }}>
            Born: {star.birth_year}
          </div>
        )}
      </div>
    </Link>
  );
};

export default CastMember;

