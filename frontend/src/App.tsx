import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MovieList from './features/movie-list/pages/movie_list';
import Movie from './features/movies/movie';
import Star from './features/stars/star';
import ShoppingCart from './features/shopping-cart/pages/shopping_cart';
import Payment from './features/payment/pages/payment';
import { MovieSearchProvider } from './contexts/MovieSearchContext';



function App() {
    return (
        <MovieSearchProvider>
            <BrowserRouter>
                <div className="App">
                    <Routes>
                        <Route path="/movie/:id" element={<Movie/>}/>
                        <Route path="/star/:id" element={<Star/>}/>
                        <Route path="/cart" element={<ShoppingCart/>}/>
                        <Route path="/payment" element={<Payment/>}/>
                        <Route path="/" element={<MovieList/>}/>
                    </Routes>
                </div>
            </BrowserRouter>
        </MovieSearchProvider>
    );
}

export default App;
