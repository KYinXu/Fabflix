import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MovieList from './features/movie-list/pages/movie_list';
import Movie from './features/movies/movie';
import Star from './features/stars/star';
import Login from './features/login/pages/login';
import ShoppingCart from './features/shopping-cart/pages/shopping_cart';
import Payment from './features/payment/pages/payment';


import { Navigate, Outlet } from 'react-router-dom';
import { useState, useEffect } from 'react';


const PrivateRoutes = () => {
    const [authorized, setAuthorized] = useState<boolean | null>(null);

    useEffect(() => {
        const validate = async () => {
            const response = await fetch(`${import.meta.env.VITE_BACKEND_URL}/movies`, {
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
                <Route path="/cart" element={<ShoppingCart/>}/>
                <Route path="/payment" element={<Payment/>}/>
                </Route>
            <Route path="/login" element={<Login />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
