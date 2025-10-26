import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import { useFetchGenres } from "../hooks/useFetchGenres";
import MovieListGrid from '../components/MovieListGrid';
import SearchSection from '../components/SearchSection';
import BrowseSection from '../components/BrowseSection';
import SortControl from '../components/SortControl';

const MovieList: React.FC = () => {
    const {data, loading, error, currentPage, hasNextPage, pageSize, sortCriteria, sortOrder, searchMovies, browseMovies, browseByGenre, goToNextPage, goToPreviousPage, setPageSize, setSortCriteria, setSortOrder} = useFetchMovieList(); // create state by calling hook
    const {data: genres} = useFetchGenres(); // fetch genres
    const [searchParams] = useSearchParams();
    const [browseType, setBrowseType] = useState<'title' | 'genre'>('title');
    const [selectedLetter, setSelectedLetter] = useState<string>('All');
    const [selectedGenreId, setSelectedGenreId] = useState<number | null>(null);
    const [hasInitialized, setHasInitialized] = useState(false);
    
    // Handle URL parameters on mount
    useEffect(() => {
        const genreIdParam = searchParams.get('genreId');
        if (genreIdParam && !hasInitialized) {
            const genreId = parseInt(genreIdParam);
            if (!isNaN(genreId)) {
                setBrowseType('genre');
                setSelectedGenreId(genreId);
                setSelectedLetter('');
                browseByGenre(genreId);
                setHasInitialized(true);
            }
        }
    }, [searchParams, hasInitialized, browseByGenre]);
    
    const handleSearch = (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        // Search movies by title, star, director, and year (empty strings show all movies)
        searchMovies(movieQuery, starQuery, directorQuery, yearQuery);
        setSelectedLetter('All');
        setSelectedGenreId(null);
    };

    const handleBrowseTypeChange = (type: 'title' | 'genre') => {
        setBrowseType(type);
        if (type === 'title') {
            browseMovies('All');
            setSelectedLetter('All');
            setSelectedGenreId(null);
        } else {
            setSelectedLetter('');
        }
    };

    const handleLetterChange = (letter: string) => {
        // Only trigger browse if we're on the title tab
        if (browseType === 'title') {
            browseMovies(letter);
            setSelectedLetter(letter);
            setSelectedGenreId(null);
        }
    };

    const handleGenreChange = (genreId: number) => {
        // Only trigger browse if we're on the genre tab
        if (browseType === 'genre') {
            browseByGenre(genreId);
            setSelectedGenreId(genreId);
            setSelectedLetter('');
        }
    };

    if (error){
        return (
            <div className="movie-list-error">
                <h1 className="text-3xl font-bold text-red-700 text-center">ERROR: {error}</h1>
            </div>
        )
    }
    if (loading){
        return (
            <div className="movie-list">
                <h1 className="text-6xl font-extrabold text-center mb-8 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent py-4">
                    Fabflix
                </h1>
                
                <SearchSection onSearch={handleSearch} />
                
                <BrowseSection 
                    onBrowseTypeChange={handleBrowseTypeChange}
                    onLetterChange={handleLetterChange}
                    onGenreChange={handleGenreChange}
                    genres={genres}
                />
                
                {/* Sort Control */}
                <div className="container mx-auto px-4 mb-6">
                    <div className="max-w-7xl mx-auto">
                        <SortControl 
                            sortCriteria={sortCriteria}
                            sortOrder={sortOrder}
                            onSortCriteriaChange={setSortCriteria}
                            onSortOrderChange={setSortOrder}
                        />
                    </div>
                </div>
                
                <div className="container mx-auto px-4">
                    <p className="text-gray-600 text-center">
                        Loading movies...
                    </p>
                </div>
            </div>
        );
    }

    return (
        <div className="movie-list">
            <h1 className="text-6xl font-extrabold text-center mb-8 bg-gradient-to-r from-blue-600 via-purple-600 to-pink-600 bg-clip-text text-transparent py-4">
                Fabflix
            </h1>
            
            <SearchSection onSearch={handleSearch} />
            
            <BrowseSection 
                onBrowseTypeChange={handleBrowseTypeChange}
                onLetterChange={handleLetterChange}
                onGenreChange={handleGenreChange}
                genres={genres}
            />
            
            {/* Sort Control */}
            <div className="container mx-auto px-4 mb-6">
                <div className="max-w-7xl mx-auto">
                    <SortControl 
                        sortCriteria={sortCriteria}
                        sortOrder={sortOrder}
                        onSortCriteriaChange={setSortCriteria}
                        onSortOrderChange={setSortOrder}
                    />
                </div>
            </div>
            
            {/* Display current filter */}
            {(selectedLetter || selectedGenreId !== null) && (
                <div className="container mx-auto px-4 mb-6">
                    <h2 className="text-2xl font-semibold text-gray-700 text-center">
                        {browseType === 'title' && selectedLetter && (
                            <>Browsing movies starting with "<span className="text-blue-600 font-bold">{selectedLetter === 'All' ? 'All Letters' : selectedLetter}</span>"</>
                        )}
                        {browseType === 'genre' && selectedGenreId !== null && genres && (
                            <>Browsing <span className="text-blue-600 font-bold">{genres.find(g => g.id === selectedGenreId)?.name || 'Genre'}</span> movies</>
                        )}
                    </h2>
                </div>
            )}
            
            {data && <MovieListGrid movies={data} />}
            
            {/* Pagination Controls */}
            {data && data.length > 0 && (
                <div className="flex flex-col items-center gap-6 mt-8">
                    <div className="flex items-center gap-2">
                        <label htmlFor="pageSize" className="text-gray-600 font-medium">
                            Movies per page:
                        </label>
                        <select
                            id="pageSize"
                            value={pageSize}
                            onChange={(e) => setPageSize(Number(e.target.value))}
                            className="px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                        >
                            <option value="10">10</option>
                            <option value="25">25</option>
                            <option value="50">50</option>
                            <option value="100">100</option>
                        </select>
                    </div>
                    <div className="flex justify-center items-center gap-4">
                        <button
                            onClick={goToPreviousPage}
                            disabled={currentPage === 0}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                currentPage === 0
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white hover:shadow-lg'
                            }`}
                        >
                            Previous Page
                        </button>
                        <span className="text-gray-600 font-medium">
                            Page {currentPage + 1}
                        </span>
                        <button
                            onClick={goToNextPage}
                            disabled={!hasNextPage}
                            className={`px-6 py-3 rounded-lg font-semibold transition-all duration-200 ${
                                !hasNextPage
                                    ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                                    : 'bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500 text-white hover:shadow-lg'
                            }`}
                        >
                            Next Page
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default MovieList;
