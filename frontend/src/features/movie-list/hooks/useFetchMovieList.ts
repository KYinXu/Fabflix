import { useState, useEffect, useCallback } from 'react';
import type {Movie} from "@/types/types";

interface useFetchReturn {
    data: Movie[] | null;
    loading: boolean;
    error: string | null;
    currentPage: number;
    hasNextPage: boolean;
    pageSize: number;
    sortCriteria: string;
    sortOrder: string;
    searchMovies: (titleQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => Promise<void>;
    browseMovies: (letter: string) => Promise<void>;
    browseByGenre: (genreId: number) => Promise<void>;
    goToNextPage: () => Promise<void>;
    goToPreviousPage: () => Promise<void>;
    setPageSize: (size: number) => Promise<void>;
    setSortCriteria: (criteria: string) => Promise<void>;
    setSortOrder: (order: string) => Promise<void>;
}

export const useFetchMovieList = () : useFetchReturn => {
    // State variables
    const [data, setData] = useState<Movie[] | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [hasNextPage, setHasNextPage] = useState<boolean>(false);
    const [pageSize, setPageSizeState] = useState<number>(25);
    const [sortCriteria, setSortCriteriaState] = useState<string>('r.ratings');
    const [sortOrder, setSortOrderState] = useState<string>('DESC');
    
    // Track query parameters for pagination
    const [lastQuery, setLastQuery] = useState<{
        titleQuery: string;
        starQuery: string;
        directorQuery: string;
        yearQuery: string;
        letter: string;
        genreId: number | null;
    }>({
        titleQuery: '',
        starQuery: '',
        directorQuery: '',
        yearQuery: '',
        letter: '',
        genreId: null
    });

    // Data Fetching
    const fetchMovie = async (titleQuery: string = '', starQuery: string = '', directorQuery: string = '', yearQuery: string = '', letter: string = '', genreId: number | null = null, page: number = 0, size: number = 25, criteria: string = sortCriteria, order: string = sortOrder) => {
        setLoading(true);
        setError(null);
        try{
            const BASE_URL = 'http://localhost:8080/fabflix_war_exploded/movies';
            // Build query params
            const params = new URLSearchParams();
            if (titleQuery.trim()) {
                params.append('title', titleQuery.trim());
            }
            if (starQuery.trim()) {
                params.append('star', starQuery.trim());
            }
            if (directorQuery.trim()) {
                params.append('director', directorQuery.trim());
            }
            if (yearQuery.trim()) {
                params.append('year', yearQuery.trim());
            }
            if (letter.trim()) {
                params.append('letter', letter.trim());
            }
            if (genreId !== null) {
                params.append('genreId', genreId.toString());
            }
            if (page > 0) {
                params.append('page', page.toString());
            }
            if (size !== 25) {
                params.append('pageSize', size.toString());
            }
            if (criteria === 'r.ratings') {
                params.append('sortCriteria', 'r.ratings');
                params.append('tieBreaker', 'title');
            } else {
                params.append('sortCriteria', criteria);
                if (criteria === 'm.title') {
                    params.append('tieBreaker', 'ratings');
                }
            }
            if (order !== 'DESC') {
                params.append('sortOrder', order);
            }
            const url = params.toString() ? `${BASE_URL}?${params.toString()}` : BASE_URL;

            const fetching = await fetch(url, {credentials: 'include'})
            const fetchedData = await fetching.json() // converts to JSON
            console.log(fetchedData) // logs JSON data
            setData(fetchedData);
            
            // Determine if there's a next page (if we got exactly size results, there might be more)
            setHasNextPage(fetchedData.length === size);
        }
        catch (error){ // error handling for crashes
            console.log(error)
            const message =
                error instanceof Error ? error.message : "An error has occurred";
            setError(message);
        }

        finally {
            setLoading(false)
        }
    }

    // Search function for parent component
    const searchMovies = useCallback(async (titleQuery: string, starQuery: string, directorQuery: string, yearQuery: string) => {
        setCurrentPage(0);
        setLastQuery({ titleQuery, starQuery, directorQuery, yearQuery, letter: '', genreId: null });
        await fetchMovie(titleQuery, starQuery, directorQuery, yearQuery, '', null, 0, pageSize);
    }, [pageSize]);

    // Browse function for browsing by letter
    const browseMovies = useCallback(async (letter: string) => {
        setCurrentPage(0);
        setLastQuery({ titleQuery: '', starQuery: '', directorQuery: '', yearQuery: '', letter, genreId: null });
        await fetchMovie('', '', '', '', letter, null, 0, pageSize);
    }, [pageSize]);

    // Browse function for browsing by genre
    const browseByGenre = useCallback(async (genreId: number) => {
        setCurrentPage(0);
        setLastQuery({ titleQuery: '', starQuery: '', directorQuery: '', yearQuery: '', letter: '', genreId });
        await fetchMovie('', '', '', '', '', genreId, 0, pageSize);
    }, [pageSize]);

    // Pagination functions
    const goToNextPage = useCallback(async () => {
        const nextPage = currentPage + 1;
        setCurrentPage(nextPage);
        await fetchMovie(
            lastQuery.titleQuery,
            lastQuery.starQuery,
            lastQuery.directorQuery,
            lastQuery.yearQuery,
            lastQuery.letter,
            lastQuery.genreId,
            nextPage,
            pageSize
        );
    }, [currentPage, lastQuery, pageSize]);

    const goToPreviousPage = useCallback(async () => {
        if (currentPage > 0) {
            const prevPage = currentPage - 1;
            setCurrentPage(prevPage);
            await fetchMovie(
                lastQuery.titleQuery,
                lastQuery.starQuery,
                lastQuery.directorQuery,
                lastQuery.yearQuery,
                lastQuery.letter,
                lastQuery.genreId,
                prevPage,
                pageSize
            );
        }
    }, [currentPage, lastQuery, pageSize]);

    const setPageSize = useCallback(async (size: number) => {
        setPageSizeState(size);
        setCurrentPage(0); // Reset to first page when changing page size
        await fetchMovie(
            lastQuery.titleQuery,
            lastQuery.starQuery,
            lastQuery.directorQuery,
            lastQuery.yearQuery,
            lastQuery.letter,
            lastQuery.genreId,
            0,
            size
        );
    }, [lastQuery, sortCriteria, sortOrder]);

    const setSortCriteria = useCallback(async (criteria: string) => {
        setSortCriteriaState(criteria);
        setCurrentPage(0); // Reset to first page when changing sort
        await fetchMovie(
            lastQuery.titleQuery,
            lastQuery.starQuery,
            lastQuery.directorQuery,
            lastQuery.yearQuery,
            lastQuery.letter,
            lastQuery.genreId,
            0,
            pageSize,
            criteria,
            sortOrder
        );
    }, [lastQuery, pageSize, sortOrder]);

    const setSortOrder = useCallback(async (order: string) => {
        setSortOrderState(order);
        setCurrentPage(0); // Reset to first page when changing sort
        await fetchMovie(
            lastQuery.titleQuery,
            lastQuery.starQuery,
            lastQuery.directorQuery,
            lastQuery.yearQuery,
            lastQuery.letter,
            lastQuery.genreId,
            0,
            pageSize,
            sortCriteria,
            order
        );
    }, [lastQuery, pageSize, sortCriteria]);

    // Run & Return States
    useEffect(() => {
        fetchMovie();
    }, []); // no argument, as this page is static

    return { data, loading, error, currentPage, hasNextPage, pageSize, sortCriteria, sortOrder, searchMovies, browseMovies, browseByGenre, goToNextPage, goToPreviousPage, setPageSize, setSortCriteria, setSortOrder };
}