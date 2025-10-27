import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useLocation } from 'react-router-dom';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import { useFetchGenres } from "../hooks/useFetchGenres";
import { useSessionState } from "@/hooks/useSessionState";
import { CurrentState, createSearchState, createLetterBrowseState, createGenreBrowseState, createSortState, createPageSizeState, createBrowseTypeState } from '@/types/session';
import MovieListGrid from '../components/MovieListGrid';
import SearchSection from '../components/SearchSection';
import BrowseSection from '../components/BrowseSection';
import SortControl from '../components/SortControl';
import PaginationControls from '../components/PaginationControls';

const MovieList: React.FC = () => {
    const {data, loading, error, currentPage, hasNextPage, pageSize, sortCriteria, sortOrder, searchMovies, browseMovies, browseByGenre, goToNextPage, goToPreviousPage, setPageSize, setSortCriteria, setSortOrder} = useFetchMovieList(); // create state by calling hook
    const {data: genres} = useFetchGenres(); // fetch genres
    const {saveState, loadState} = useSessionState(); // session state management
    const [searchParams] = useSearchParams();
    const location = useLocation();
    const [browseType, setBrowseType] = useState<'title' | 'genre'>('title');
    const [selectedLetter, setSelectedLetter] = useState<string>('All');
    const [selectedGenreId, setSelectedGenreId] = useState<number | null>(null);
    const hasLoadedRef = useRef(false);
    const previousPathRef = useRef<string>(location.pathname);
    
    // Helper function to get current state
    const getCurrentState = (): CurrentState => ({
        browseType,
        selectedLetter,
        selectedGenreId,
        searchValues,
        sortCriteria,
        sortOrder,
        pageSize,
        currentPage
    });
    const [searchValues, setSearchValues] = useState({
        movieQuery: '',
        starQuery: '',
        directorQuery: '',
        yearQuery: ''
    });
    
    // Load session state on mount or when navigating to home from another route
    useEffect(() => {
        const loadSessionState = async () => {
            // Check if we navigated FROM another route TO home
            const navigatedToHome = previousPathRef.current !== location.pathname && location.pathname === '/';
            
            // Either first load or navigating to home from another page
            if (!hasLoadedRef.current || navigatedToHome) {
                hasLoadedRef.current = true;
                
                const sessionState = await loadState();
                
                // Check if there's any meaningful state to restore
                const hasRestorableState = sessionState && (
                    sessionState.searchState.movieQuery || 
                    sessionState.searchState.starQuery || 
                    sessionState.searchState.directorQuery || 
                    sessionState.searchState.yearQuery ||
                    (sessionState.browseType === 'genre' && sessionState.selectedGenreId) ||
                    (sessionState.browseType === 'title' && sessionState.selectedLetter && sessionState.selectedLetter !== 'All')
                );
                
                if (hasRestorableState) {
                    // Restore browse type and selections
                    setBrowseType(sessionState.browseType);
                    setSelectedLetter(sessionState.selectedLetter);
                    setSelectedGenreId(sessionState.selectedGenreId);
                    setSearchValues(sessionState.searchState);
                    
                    // Apply the restored state based on search state
                    if (sessionState.searchState.movieQuery || sessionState.searchState.starQuery || 
                        sessionState.searchState.directorQuery || sessionState.searchState.yearQuery) {
                        // Restore search results
                        searchMovies(
                            sessionState.searchState.movieQuery,
                            sessionState.searchState.starQuery,
                            sessionState.searchState.directorQuery,
                            sessionState.searchState.yearQuery
                        );
                    } else if (sessionState.browseType === 'genre' && sessionState.selectedGenreId) {
                        // Restore genre browse
                        browseByGenre(sessionState.selectedGenreId);
                    } else if (sessionState.browseType === 'title' && sessionState.selectedLetter && sessionState.selectedLetter !== 'All') {
                        // Restore title browse (only if not 'All')
                        browseMovies(sessionState.selectedLetter);
                    }
                } else {
                    // No meaningful session state - handle URL parameters or load default
                    const genreIdParam = searchParams.get('genreId');
                    if (genreIdParam) {
                        const genreId = parseInt(genreIdParam);
                        if (!isNaN(genreId)) {
                            setBrowseType('genre');
                            setSelectedGenreId(genreId);
                            setSelectedLetter('');
                            browseByGenre(genreId);
                        }
                    } else {
                        // No session state, no URL params - execute default query
                        browseMovies('All');
                    }
                }
            }
            
            // Update previous path for next comparison
            previousPathRef.current = location.pathname;
        };
        
        loadSessionState();
    }, [location.pathname, searchParams]); // Re-run when location changes
    
    const handleSearch = async (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        // Search movies by title, star, director, and year (empty strings show all movies)
        searchMovies(movieQuery, starQuery, directorQuery, yearQuery);
        setSelectedLetter('All');
        setSelectedGenreId(null);
        
        // Update search values and save to session
        const newSearchValues = { movieQuery, starQuery, directorQuery, yearQuery };
        setSearchValues(newSearchValues);
        
        const stateToSave = createSearchState(getCurrentState(), newSearchValues);
        await saveState(stateToSave);
    };


    const handleLetterChange = async (letter: string) => {
        // Always trigger browse when letter is clicked
        browseMovies(letter);
        setBrowseType('title');
        setSelectedLetter(letter);
        setSelectedGenreId(null);
        
        await saveState(createLetterBrowseState(getCurrentState(), letter));
    };

    const handleGenreChange = async (genreId: number) => {
        // Always trigger browse when genre is clicked
        browseByGenre(genreId);
        setBrowseType('genre');
        setSelectedGenreId(genreId);
        setSelectedLetter('');
        
        await saveState(createGenreBrowseState(getCurrentState(), genreId));
    };

    const handleSortChange = async (newSortCriteria: string, newSortOrder: string) => {
        setSortCriteria(newSortCriteria);
        setSortOrder(newSortOrder);
        
        await saveState(createSortState(getCurrentState(), newSortCriteria, newSortOrder));
    };

    const handlePageSizeChange = async (newPageSize: number) => {
        setPageSize(newPageSize);
        
        await saveState(createPageSizeState(getCurrentState(), newPageSize));
    };

    const handleBrowseTypeChange = async (type: 'title' | 'genre') => {
        setBrowseType(type);
        if (type === 'title') {
            setSelectedLetter('All');
            setSelectedGenreId(null);
        } else {
            setSelectedLetter('');
            setSelectedGenreId(null);
        }
        
        await saveState(createBrowseTypeState(getCurrentState(), type));
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
                
                <SearchSection onSearch={handleSearch} initialValues={searchValues} />
                
                <BrowseSection 
                    onLetterChange={handleLetterChange}
                    onGenreChange={handleGenreChange}
                    onBrowseTypeChange={handleBrowseTypeChange}
                    genres={genres}
                    initialBrowseType={browseType}
                    initialSelectedLetter={selectedLetter}
                    initialSelectedGenreId={selectedGenreId}
                />
                
                {/* Sort Control and Pagination */}
                <div className="w-full px-4 mb-6">
                    <div className="flex justify-between items-center">
                        <div className="flex items-center">
                            <SortControl 
                                sortCriteria={sortCriteria}
                                sortOrder={sortOrder}
                                onSortCriteriaChange={(criteria) => handleSortChange(criteria, sortOrder)}
                                onSortOrderChange={(order) => handleSortChange(sortCriteria, order)}
                            />
                        </div>
                        <div className="flex items-center min-w-0">
                            <PaginationControls
                                currentPage={currentPage}
                                hasNextPage={hasNextPage}
                                pageSize={pageSize}
                                goToPreviousPage={goToPreviousPage}
                                goToNextPage={goToNextPage}
                                setPageSize={handlePageSizeChange}
                                isLoading={loading}
                            />
                        </div>
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
            
            <SearchSection onSearch={handleSearch} initialValues={searchValues} />
            
            <BrowseSection 
                onLetterChange={handleLetterChange}
                onGenreChange={handleGenreChange}
                genres={genres}
                initialBrowseType={browseType}
                initialSelectedLetter={selectedLetter}
                initialSelectedGenreId={selectedGenreId}
            />
            
            {/* Sort Control and Pagination */}
            <div className="w-full px-4 mb-6">
                <div className="flex justify-between items-center">
                    <div className="flex items-center">
                        <SortControl 
                            sortCriteria={sortCriteria}
                            sortOrder={sortOrder}
                            onSortCriteriaChange={(criteria) => handleSortChange(criteria, sortOrder)}
                            onSortOrderChange={(order) => handleSortChange(sortCriteria, order)}
                        />
                    </div>
                    <div className="flex items-center min-w-0">
                        <PaginationControls
                            currentPage={currentPage}
                            hasNextPage={hasNextPage}
                            pageSize={pageSize}
                            goToPreviousPage={goToPreviousPage}
                            goToNextPage={goToNextPage}
                            setPageSize={handlePageSizeChange}
                            isLoading={loading}
                        />
                    </div>
                </div>
            </div>
            
            {/* Display current filter */}
            {(selectedLetter || selectedGenreId !== null || (searchValues.movieQuery || searchValues.starQuery || searchValues.directorQuery || searchValues.yearQuery)) && 
             !(browseType === 'title' && selectedLetter === 'All') && (
                <div className="container mx-auto px-4 mb-6">
                    <h2 className="text-4xl font-bold text-center" style={{ color: 'var(--theme-text-primary)' }}>
                        {(browseType === 'title' && selectedLetter && selectedLetter !== 'All') || (browseType === 'genre' && selectedGenreId !== null) ? (
                            <>
                                {browseType === 'title' && selectedLetter && (
                                    <>Browsing movies starting with "<span className="font-extrabold" style={{ color: 'var(--theme-secondary-light)' }}>{selectedLetter}</span>"</>
                                )}
                                {browseType === 'genre' && selectedGenreId !== null && genres && (
                                    <>Browsing <span className="font-extrabold" style={{ color: 'var(--theme-secondary-light)' }}>{genres.find(g => g.id === selectedGenreId)?.name || 'Genre'}</span> movies</>
                                )}
                            </>
                        ) : (searchValues.movieQuery || searchValues.starQuery || searchValues.directorQuery || searchValues.yearQuery) ? (
                            <>Search results</>
                        ) : (
                            <>Browsing movies...</>
                        )}
                    </h2>
                </div>
            )}

            
            {data && <MovieListGrid movies={data} onGenreClick={handleGenreChange} />}
            
            {/* Bottom Pagination Controls */}
            <div className="container mx-auto px-4 mt-8">
                <div className="max-w-7xl mx-auto flex justify-center">
                    <PaginationControls
                        currentPage={currentPage}
                        hasNextPage={hasNextPage}
                        pageSize={pageSize}
                        goToPreviousPage={goToPreviousPage}
                        goToNextPage={goToNextPage}
                        setPageSize={handlePageSizeChange}
                        isLoading={loading}
                    />
                </div>
            </div>
        </div>
    );
};

export default MovieList;
