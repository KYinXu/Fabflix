export interface SearchState {
    movieQuery: string;
    starQuery: string;
    directorQuery: string;
    yearQuery: string;
}

// Interface for query parameters used in API calls
export interface QueryParams {
    titleQuery: string;
    starQuery: string;
    directorQuery: string;
    yearQuery: string;
    letter: string;
    genreId: number | null;
    searchMode?: 'simple' | 'token';
}

// Interface for complete fetch parameters
export interface FetchParams extends QueryParams {
    page: number;
    pageSize: number;
    sortCriteria: string;
    sortOrder: string;
}

export interface SessionState {
    browseType: 'title' | 'genre';
    selectedLetter: string;
    selectedGenreId: number | null;
    searchState: SearchState;
    sortCriteria: string;
    sortOrder: string;
    pageSize: number;
    currentPage: number;
}

// Interface for current state context
export interface CurrentState {
    browseType: 'title' | 'genre';
    selectedLetter: string;
    selectedGenreId: number | null;
    searchValues: SearchState;
    sortCriteria: string;
    sortOrder: string;
    pageSize: number;
    currentPage: number;
}

// Helper function to create session state objects
export const createSessionState = (
    browseType: 'title' | 'genre',
    selectedLetter: string,
    selectedGenreId: number | null,
    searchState: SearchState,
    sortCriteria: string,
    sortOrder: string,
    pageSize: number,
    currentPage: number
): SessionState => ({
    browseType,
    selectedLetter,
    selectedGenreId,
    searchState,
    sortCriteria,
    sortOrder,
    pageSize,
    currentPage
});

// Helper functions for specific state changes
export const createSearchState = (currentState: CurrentState, searchValues: SearchState): SessionState =>
    createSessionState('title', 'All', null, searchValues, currentState.sortCriteria, currentState.sortOrder, currentState.pageSize, currentState.currentPage);

export const createLetterBrowseState = (currentState: CurrentState, letter: string): SessionState =>
    createSessionState('title', letter, null, currentState.searchValues, currentState.sortCriteria, currentState.sortOrder, currentState.pageSize, currentState.currentPage);

export const createGenreBrowseState = (currentState: CurrentState, genreId: number): SessionState =>
    createSessionState('genre', '', genreId, currentState.searchValues, currentState.sortCriteria, currentState.sortOrder, currentState.pageSize, currentState.currentPage);

export const createSortState = (currentState: CurrentState, sortCriteria: string, sortOrder: string): SessionState =>
    createSessionState(currentState.browseType, currentState.selectedLetter, currentState.selectedGenreId, currentState.searchValues, sortCriteria, sortOrder, currentState.pageSize, currentState.currentPage);

export const createPageSizeState = (currentState: CurrentState, pageSize: number): SessionState =>
    createSessionState(currentState.browseType, currentState.selectedLetter, currentState.selectedGenreId, currentState.searchValues, currentState.sortCriteria, currentState.sortOrder, pageSize, currentState.currentPage);

export const createBrowseTypeState = (currentState: CurrentState, browseType: 'title' | 'genre'): SessionState =>
    createSessionState(browseType, browseType === 'title' ? 'All' : '', null, currentState.searchValues, currentState.sortCriteria, currentState.sortOrder, currentState.pageSize, currentState.currentPage);

export const createPaginationState = (currentState: CurrentState, page: number): SessionState =>
    createSessionState(currentState.browseType, currentState.selectedLetter, currentState.selectedGenreId, currentState.searchValues, currentState.sortCriteria, currentState.sortOrder, currentState.pageSize, page);