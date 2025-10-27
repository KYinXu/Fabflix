import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useFetchMovieList } from "../hooks/useFetchMovieList";
import { useFetchGenres } from "../hooks/useFetchGenres";
import { useSessionState } from "@/hooks/useSessionState";
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
    const [browseType, setBrowseType] = useState<'title' | 'genre'>('title');
    const [selectedLetter, setSelectedLetter] = useState<string>('All');
    const [selectedGenreId, setSelectedGenreId] = useState<number | null>(null);
    const [hasInitialized, setHasInitialized] = useState(false);
    const [searchValues, setSearchValues] = useState({
        movieQuery: '',
        starQuery: '',
        directorQuery: '',
        yearQuery: ''
    });
    
    // Load session state on mount
    useEffect(() => {
        const loadSessionState = async () => {
            if (!hasInitialized) {
                const sessionState = await loadState();
                if (sessionState) {
                    // Restore browse type and selections
                    setBrowseType(sessionState.browseType);
                    setSelectedLetter(sessionState.selectedLetter);
                    setSelectedGenreId(sessionState.selectedGenreId);
                    setSearchValues(sessionState.searchState);
                    
                    // Restore sort settings
                    if (sessionState.sortCriteria) {
                        setSortCriteria(sessionState.sortCriteria);
                    }
                    if (sessionState.sortOrder) {
                        setSortOrder(sessionState.sortOrder);
                    }
                    if (sessionState.pageSize) {
                        setPageSize(sessionState.pageSize);
                    }
                    
                    // Apply the restored state
                    if (sessionState.browseType === 'genre' && sessionState.selectedGenreId) {
                        browseByGenre(sessionState.selectedGenreId);
                    } else if (sessionState.browseType === 'title') {
                        browseMovies(sessionState.selectedLetter);
                    } else if (sessionState.searchState.movieQuery || sessionState.searchState.starQuery || 
                               sessionState.searchState.directorQuery || sessionState.searchState.yearQuery) {
                        searchMovies(
                            sessionState.searchState.movieQuery,
                            sessionState.searchState.starQuery,
                            sessionState.searchState.directorQuery,
                            sessionState.searchState.yearQuery
                        );
                    }
                } else {
                    // Handle URL parameters if no session state
                    const genreIdParam = searchParams.get('genreId');
                    if (genreIdParam) {
                        const genreId = parseInt(genreIdParam);
                        if (!isNaN(genreId)) {
                            setBrowseType('genre');
                            setSelectedGenreId(genreId);
                            setSelectedLetter('');
                            browseByGenre(genreId);
                        }
                    }
                }
                setHasInitialized(true);
            }
        };
        
        loadSessionState();
    }, [hasInitialized, loadState, browseByGenre, browseMovies, searchMovies, setSortCriteria, setSortOrder, setPageSize, searchParams]);
    
    const handleSearch = async (movieQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        // Search movies by title, star, director, and year (empty strings show all movies)
        searchMovies(movieQuery, starQuery, directorQuery, yearQuery);
        setSelectedLetter('All');
        setSelectedGenreId(null);
        
        // Update search values and save to session
        const newSearchValues = { movieQuery, starQuery, directorQuery, yearQuery };
        setSearchValues(newSearchValues);
        
        await saveState({
            browseType: 'title',
            selectedLetter: 'All',
            selectedGenreId: null,
            searchState: newSearchValues,
            sortCriteria,
            sortOrder,
            pageSize,
            currentPage
        });
    };

    const handleBrowseTypeChange = async (type: 'title' | 'genre') => {
        setBrowseType(type);
        if (type === 'title') {
            browseMovies('All');
            setSelectedLetter('All');
            setSelectedGenreId(null);
            
            await saveState({
                browseType: type,
                selectedLetter: 'All',
                selectedGenreId: null,
                searchState: searchValues,
                sortCriteria,
                sortOrder,
                pageSize,
                currentPage
            });
        } else {
            setSelectedLetter('');
            
            await saveState({
                browseType: type,
                selectedLetter: '',
                selectedGenreId: null,
                searchState: searchValues,
                sortCriteria,
                sortOrder,
                pageSize,
                currentPage
            });
        }
    };

    const handleLetterChange = async (letter: string) => {
        // Only trigger browse if we're on the title tab
        if (browseType === 'title') {
            browseMovies(letter);
            setSelectedLetter(letter);
            setSelectedGenreId(null);
            
            await saveState({
                browseType,
                selectedLetter: letter,
                selectedGenreId: null,
                searchState: searchValues,
                sortCriteria,
                sortOrder,
                pageSize,
                currentPage
            });
        }
    };

    const handleGenreChange = async (genreId: number) => {
        // Only trigger browse if we're on the genre tab
        if (browseType === 'genre') {
            browseByGenre(genreId);
            setSelectedGenreId(genreId);
            setSelectedLetter('');
            
            await saveState({
                browseType,
                selectedLetter: '',
                selectedGenreId: genreId,
                searchState: searchValues,
                sortCriteria,
                sortOrder,
                pageSize,
                currentPage
            });
        }
    };

    const handleSortChange = async (newSortCriteria: string, newSortOrder: string) => {
        setSortCriteria(newSortCriteria);
        setSortOrder(newSortOrder);
        
        await saveState({
            browseType,
            selectedLetter,
            selectedGenreId,
            searchState: searchValues,
            sortCriteria: newSortCriteria,
            sortOrder: newSortOrder,
            pageSize,
            currentPage
        });
    };

    const handlePageSizeChange = async (newPageSize: number) => {
        setPageSize(newPageSize);
        
        await saveState({
            browseType,
            selectedLetter,
            selectedGenreId,
            searchState: searchValues,
            sortCriteria,
            sortOrder,
            pageSize: newPageSize,
            currentPage
        });
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
                    onBrowseTypeChange={handleBrowseTypeChange}
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
                onBrowseTypeChange={handleBrowseTypeChange}
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
            {(selectedLetter || selectedGenreId !== null) && (
                <div className="container mx-auto px-4 mb-6">
                    <h2 className="text-4xl font-bold text-center" style={{ color: 'var(--theme-text-primary)' }}>
                        {browseType === 'title' && selectedLetter && (
                            <>Browsing movies starting with "<span className="font-extrabold" style={{ color: 'var(--theme-secondary-light)' }}>{selectedLetter === 'All' ? 'All Letters' : selectedLetter}</span>"</>
                        )}
                        {browseType === 'genre' && selectedGenreId !== null && genres && (
                            <>Browsing <span className="font-extrabold" style={{ color: 'var(--theme-secondary-light)' }}>{genres.find(g => g.id === selectedGenreId)?.name || 'Genre'}</span> movies</>
                        )}
                    </h2>
                </div>
            )}

            
            {data && <MovieListGrid movies={data} />}
            
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
