import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MovieList from './features/movie-list/pages/movie_list';
import Movie from './features/movies/movie';

function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
          <Route path="/" element={<MovieList />} />
          <Route path="/movie/:id" element={<Movie />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
