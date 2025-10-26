import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MovieList from './features/movie-list/pages/movie_list';
import Movie from './features/movies/movie';
import Star from './features/stars/star';
import Login from './features/login/pages/login';
import { Navigate, Outlet } from 'react-router-dom';
import { useState, useEffect } from 'react';


const PrivateRoutes = () => {
    const [authorized, setAuthorized] = useState<boolean | null>(null);

    useEffect(() => {
        const validate = async () => {
            const response = await fetch("http://localhost:8080/fabflix_war_exploded/movies", {
                method: "GET",
                credentials: "include",
            });
            setAuthorized(response.status !== 401);
        };
        validate();
    }, []);

    if (authorized === null) return <p>Loading...</p>;
    return authorized ? <Outlet/> : <Navigate to="/login" />;
};


function App() {
  return (
    <BrowserRouter>
      <div className="App">
        <Routes>
            <Route element = {<PrivateRoutes/>}>
                <Route path="/" element={<MovieList/>}/>
                <Route path="/movie/:id" element={<Movie/>}/>
                <Route path="/star/:id" element={<Star/>}/>
            </Route>
            <Route path="/login" element={<Login />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
