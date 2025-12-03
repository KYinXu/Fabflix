import React, { createContext, useContext, useRef, ReactNode, useCallback } from 'react';

interface MovieSearchContextType {
    searchFromNavbar: (query: string) => void;
    setSearchFromNavbar: (fn: (query: string) => void) => void;
}

const MovieSearchContext = createContext<MovieSearchContextType | undefined>(undefined);

export const MovieSearchProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
    const searchFromNavbarRef = useRef<((query: string) => void) | null>(null);

    const setSearchFromNavbar = useCallback((fn: (query: string) => void) => {
        searchFromNavbarRef.current = fn;
    }, []);

    const handleSearch = useCallback((query: string) => {
        if (searchFromNavbarRef.current) {
            searchFromNavbarRef.current(query);
        }
    }, []);

    return (
        <MovieSearchContext.Provider value={{ searchFromNavbar: handleSearch, setSearchFromNavbar }}>
            {children}
        </MovieSearchContext.Provider>
    );
};

export const useMovieSearch = () => {
    const context = useContext(MovieSearchContext);
    if (context === undefined) {
        throw new Error('useMovieSearch must be used within a MovieSearchProvider');
    }
    return context;
};

