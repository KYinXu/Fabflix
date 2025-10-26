import {useNavigate} from "react-router-dom";

function CheckoutButton() {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate("/cart");
    };


    return (
        <div className="flex justify-center mt-6">
            <button
                onClick={handleClick}
                className="px-6 py-3 rounded-lg font-semibold transition-all duration-200
                           bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500
                           text-white shadow-lg hover:shadow-xl hover:scale-105"
            >
                Checkout
            </button>
        </div>
    );
}

export default CheckoutButton;