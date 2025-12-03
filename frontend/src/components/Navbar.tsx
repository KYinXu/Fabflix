import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import CheckoutButton from './CheckoutButton';
import AutocompleteSearchBar from '@/features/movie-list/components/AutocompleteSearchBar';
import { useMovieSearch } from '@/contexts/MovieSearchContext';

const Navbar: React.FC = () => {
    const [searchValue, setSearchValue] = useState('');
    const { searchFromNavbar } = useMovieSearch();
    const location = useLocation();
    
    const handleNavbarSearch = (query: string) => {
        // If we're on the movie list page, use context to trigger search without URL change
        if (location.pathname === '/') {
            searchFromNavbar(query);
        } else {
            // If we're on another page, navigate to movie list with search
            window.location.href = `/?title=${encodeURIComponent(query)}&searchMode=token`;
        }
    };

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
        <nav className="shadow-lg border-b" style={{ backgroundColor: 'var(--theme-bg-secondary)', borderColor: 'var(--theme-border-primary)' }}>
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between items-center h-16">
                    {/* Logo/Brand */}
                    <div className="flex-shrink-0">
                        <Link 
                            to="/" 
                            className="text-2xl font-bold hover:opacity-80 transition-opacity"
                            style={{ 
                                background: 'linear-gradient(to right, var(--theme-primary-light), var(--theme-secondary-light), var(--theme-accent-light))',
                                WebkitBackgroundClip: 'text',
                                WebkitTextFillColor: 'transparent',
                                backgroundClip: 'text'
                            }}
                        >
                            Fabflix
                        </Link>
                    </div>

                    {/* Search Bar */}
                    <div className="flex-1 max-w-md mx-4">
                        <AutocompleteSearchBar
                            value={searchValue}
                            onChange={setSearchValue}
                            placeholder="Search movies..."
                            focusColor="blue"
                            onSearch={handleNavbarSearch}
                        />
                    </div>

                    {/* Cart and Logout Buttons */}
                    <div className="flex items-center space-x-4">
                        <CheckoutButton />
                        <button
                            onClick={handleLogout}
                            className="text-white px-6 py-3 rounded-lg text-sm font-semibold transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2"
                            style={{ 
                                backgroundColor: 'var(--theme-error)',
                                '--tw-ring-color': 'var(--theme-error)',
                                '--tw-ring-offset-color': 'var(--theme-bg-secondary)'
                            } as React.CSSProperties}
                            onMouseEnter={(e) => {
                                const target = e.target as HTMLElement;
                                target.style.backgroundColor = '#dc2626';
                            }}
                            onMouseLeave={(e) => {
                                const target = e.target as HTMLElement;
                                target.style.backgroundColor = 'var(--theme-error)';
                            }}
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
