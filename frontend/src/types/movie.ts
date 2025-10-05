export interface Movie {
  id: string;
  title: string;
  year: number;
  director: string;
  stars?: Star[];
  genres?: Genre[];
  rating?: Rating;
}

export interface Star {
  id: string;
  name: string;
  birth_year?: number;
}

export interface Genre {
  id: number;
  name: string;
}

export interface Rating {
  movie_id: string;
  ratings: number;
  vote_count: number;
}

