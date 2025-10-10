
export interface Star {
  id: string;
  name: string;
  birth_year?: number;
  movies?: Movie[];
}

export interface Movie {
    id: string;
    title: string;
    year: number;
    director: string;
    genres?: Genre[];
    rating?: Rating;
}

export interface Genre {
    id: number;
    name: string;
}

export interface Rating {
    ratings: number;
    vote_count: number;
}