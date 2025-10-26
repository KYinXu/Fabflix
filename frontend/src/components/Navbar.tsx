import React from 'react';
import { Link } from 'react-router-dom';

const Navbar: React.FC = () => {
    const handleLogout = async () => {
        try {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/logout`, {
                method: 'POST',
                credentials: 'include',
            });
            
            if (response.ok) {
                window.location.href = '/login';
            }
        } catch (error) {
            console.error('Logout failed:', error);
            // Still redirect to login even if logout request fails
            window.location.href = '/login';
        }
    };

    return (
        <nav className="bg-white shadow-lg border-b border-gray-200">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo/Brand */}
                    <div className="flex-shrink-0">
                        <Link 
                            to="/" 
                            className="text-2xl font-bold bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent hover:opacity-80 transition-opacity"
                        >
                            Fabflix
                        </Link>
                    </div>

                    {/* Logout Button */}
                    <div className="flex items-center">
                        <button
                            onClick={handleLogout}
                            className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
                        >
                            Logout
                        </button>
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;
