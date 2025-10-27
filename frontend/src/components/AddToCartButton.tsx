
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
            await fetch(BASE_URL, postRequest);

        } catch (err: any) {
            console.error('Failed to add item to cart:', err);
        }
    };

    return (
        <button
            onClick={handleClick}
            className="px-4 py-2 rounded font-medium transition-colors duration-200 border-2"
            style={{
                backgroundColor: 'var(--theme-bg-secondary)',
                borderColor: 'var(--theme-secondary)',
                color: 'var(--theme-text-primary)'
            }}
            onMouseEnter={(e) => {
                const target = e.target as HTMLElement;
                target.style.backgroundColor = 'var(--theme-secondary)';
                target.style.color = 'white';
            }}
            onMouseLeave={(e) => {
                const target = e.target as HTMLElement;
                target.style.backgroundColor = 'var(--theme-bg-secondary)';
                target.style.color = 'var(--theme-text-primary)';
            }}
        >
            Add to Cart
        </button>
    );
}

export default AddToCartButton;