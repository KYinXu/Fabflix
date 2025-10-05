import React from 'react';
import { useNavigate } from 'react-router-dom';

interface BackButtonProps {
  text: string;
  destination?: string;
  className?: string;
}

const BackButton: React.FC<BackButtonProps> = ({ 
  text, 
  destination = '/', 
  className = '' 
}) => {
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(destination);
  };

  return (
    <button 
      onClick={handleClick}
      className={`mb-6 inline-flex items-center px-4 py-2 hover:text-gray-500 text-gray-400 font-medium rounded-lg transition-colors duration-200 ${className}`}
    >
      <svg 
        className="w-4 h-4 mr-2" 
        fill="none" 
        stroke="currentColor" 
        viewBox="0 0 24 24"
      >
        <path 
          strokeLinecap="round" 
          strokeLinejoin="round" 
          strokeWidth={2} 
          d="M10 19l-7-7m0 0l7-7m-7 7h18" 
        />
      </svg>
      {text}
    </button>
  );
};

export default BackButton;
