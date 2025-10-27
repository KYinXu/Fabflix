import { useState, useCallback } from 'react';
import type {Movie} from "@/types/types";
import { QueryParams, FetchParams } from '@/types/session';

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
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [hasNextPage, setHasNextPage] = useState<boolean>(false);
    const [pageSize, setPageSizeState] = useState<number>(25);
    const [sortCriteria, setSortCriteriaState] = useState<string>('r.ratings');
    const [sortOrder, setSortOrderState] = useState<string>('DESC');
    
    // Track query parameters for pagination
    const [lastQuery, setLastQuery] = useState<QueryParams>({
        titleQuery: '',
        starQuery: '',
        directorQuery: '',
        yearQuery: '',
        letter: '',
        genreId: null
    });

    // Helper function to create QueryParams from individual parameters
    const createQueryParams = (
        titleQuery: string,
        starQuery: string,
        directorQuery: string,
        yearQuery: string,
        letter: string,
        genreId: number | null
    ): QueryParams => ({
        titleQuery,
        starQuery,
        directorQuery,
        yearQuery,
        letter,
        genreId
    });

    // Helper function to create FetchParams from individual parameters
    const createFetchParams = (
        queryParams: QueryParams,
        page: number,
        pageSize: number,
        sortCriteria: string,
        sortOrder: string
    ): FetchParams => ({
        ...queryParams,
        page,
        pageSize,
        sortCriteria,
        sortOrder
    });

    // Helper function to build query parameters
    const buildQueryParams = (fetchParams: FetchParams): URLSearchParams => {
        const params = new URLSearchParams();
        
        // Search parameters
        if (fetchParams.titleQuery.trim()) params.append('title', fetchParams.titleQuery.trim());
        if (fetchParams.starQuery.trim()) params.append('star', fetchParams.starQuery.trim());
        if (fetchParams.directorQuery.trim()) params.append('director', fetchParams.directorQuery.trim());
        if (fetchParams.yearQuery.trim()) params.append('year', fetchParams.yearQuery.trim());
        
        // Browse parameters
        if (fetchParams.letter.trim()) params.append('letter', fetchParams.letter.trim());
        if (fetchParams.genreId !== null) params.append('genreId', fetchParams.genreId.toString());
        
        // Pagination parameters
        if (fetchParams.page > 0) params.append('page', fetchParams.page.toString());
        if (fetchParams.pageSize !== 25) params.append('pageSize', fetchParams.pageSize.toString());
        
        // Sort parameters
        if (fetchParams.sortCriteria === 'r.ratings') {
            params.append('sortCriteria', 'r.ratings');
            params.append('tieBreaker', 'title');
        } else {
            params.append('sortCriteria', fetchParams.sortCriteria);
            if (fetchParams.sortCriteria === 'm.title') {
                params.append('tieBreaker', 'ratings');
            }
        }
        if (fetchParams.sortOrder !== 'DESC') params.append('sortOrder', fetchParams.sortOrder);
        
        return params;
    };

    // Data Fetching
    const fetchMovie = async (fetchParams: FetchParams) => {
        setLoading(true);
        setError(null);
        try{
            const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/movies`;
            const params = buildQueryParams(fetchParams);
            const url = params.toString() ? `${BASE_URL}?${params.toString()}` : BASE_URL;

            const fetching = await fetch(url, {credentials: 'include'})
            const fetchedData = await fetching.json() // converts to JSON
            setData(fetchedData);
            
            // Determine if there's a next page (if we got exactly size results, there might be more)
            setHasNextPage(fetchedData.length === fetchParams.pageSize);
        }
        catch (error){ // error handling for crashes
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
        const queryParams = createQueryParams(titleQuery, starQuery, directorQuery, yearQuery, '', null);
        const fetchParams = createFetchParams(queryParams, 0, pageSize, sortCriteria, sortOrder);
        setLastQuery(queryParams);
        await fetchMovie(fetchParams);
    }, [pageSize, sortCriteria, sortOrder]);

    // Browse function for browsing by letter
    const browseMovies = useCallback(async (letter: string) => {
        setCurrentPage(0);
        const queryParams = createQueryParams('', '', '', '', letter, null);
        const fetchParams = createFetchParams(queryParams, 0, pageSize, sortCriteria, sortOrder);
        setLastQuery(queryParams);
        await fetchMovie(fetchParams);
    }, [pageSize, sortCriteria, sortOrder]);

    // Browse function for browsing by genre
    const browseByGenre = useCallback(async (genreId: number) => {
        setCurrentPage(0);
        const queryParams = createQueryParams('', '', '', '', '', genreId);
        const fetchParams = createFetchParams(queryParams, 0, pageSize, sortCriteria, sortOrder);
        setLastQuery(queryParams);
        await fetchMovie(fetchParams);
    }, [pageSize, sortCriteria, sortOrder]);

    // Pagination functions
    const goToNextPage = useCallback(async () => {
        const nextPage = currentPage + 1;
        setCurrentPage(nextPage);
        const fetchParams = createFetchParams(lastQuery, nextPage, pageSize, sortCriteria, sortOrder);
        await fetchMovie(fetchParams);
    }, [currentPage, lastQuery, pageSize, sortCriteria, sortOrder]);

    const goToPreviousPage = useCallback(async () => {
        if (currentPage > 0) {
            const prevPage = currentPage - 1;
            setCurrentPage(prevPage);
            const fetchParams = createFetchParams(lastQuery, prevPage, pageSize, sortCriteria, sortOrder);
            await fetchMovie(fetchParams);
        }
    }, [currentPage, lastQuery, pageSize, sortCriteria, sortOrder]);

    const setPageSize = useCallback(async (size: number) => {
        setPageSizeState(size);
        setCurrentPage(0); // Reset to first page when changing page size
        const fetchParams = createFetchParams(lastQuery, 0, size, sortCriteria, sortOrder);
        await fetchMovie(fetchParams);
    }, [lastQuery, sortCriteria, sortOrder]);

    const setSortCriteria = useCallback(async (criteria: string) => {
        setSortCriteriaState(criteria);
        setCurrentPage(0); // Reset to first page when changing sort
        const fetchParams = createFetchParams(lastQuery, 0, pageSize, criteria, sortOrder);
        await fetchMovie(fetchParams);
    }, [lastQuery, pageSize, sortOrder]);

    const setSortOrder = useCallback(async (order: string) => {
        setSortOrderState(order);
        setCurrentPage(0); // Reset to first page when changing sort
        const fetchParams = createFetchParams(lastQuery, 0, pageSize, sortCriteria, order);
        await fetchMovie(fetchParams);
    }, [lastQuery, pageSize, sortCriteria]);

    // Run & Return States
    // Removed automatic fetchMovie() call - let session loading handle initial query
    // useEffect(() => {
    //     fetchMovie();
    // }, []); // no argument, as this page is static

    return { data, loading, error, currentPage, hasNextPage, pageSize, sortCriteria, sortOrder, searchMovies, browseMovies, browseByGenre, goToNextPage, goToPreviousPage, setPageSize, setSortCriteria, setSortOrder };
}