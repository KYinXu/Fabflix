
function AddToCartButton({ movie } : { movie: any }) {

    const handleClick = async (event: React.MouseEvent<HTMLButtonElement>) => {
        event?.stopPropagation();
        const BASE_URL = `${import.meta.env.VITE_BACKEND_URL}/cart`;
        try{

            const postRequest: RequestInit = { // defines how fetch will send
                method: 'POST',
                credentials: "include",
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify([
                    {
                        movieId: movie.id,
                        title: movie.title,
                        quantity: 1,
                    },
                ])
            };
            const response = await fetch(BASE_URL, postRequest);
            const jsonResponse = await response.json();
            console.log("RESPONSE: ", jsonResponse)

        } catch (err: any) {
            console.log(err);
        }
    };

    return (
        <div className="flex justify-center mt-6">
            <button
                onClick={handleClick}
                className="px-6 py-3 rounded-lg font-semibold transition-all duration-200
                           bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500
                           text-white shadow-lg hover:shadow-xl hover:scale-105"
            >
                Add to Cart
            </button>
        </div>
    );
}

export default AddToCartButton;