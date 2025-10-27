import {useNavigate} from "react-router-dom";

function CheckoutButton() {
    const navigate = useNavigate();

    const handleClick = () => {
        navigate("/cart");
    };


    return (
        <button
            onClick={handleClick}
            className="px-4 py-2 rounded-md text-sm font-medium transition-all duration-200
                       bg-gradient-to-r from-blue-500 via-purple-500 to-pink-500
                       text-white shadow-lg hover:shadow-xl hover:scale-105"
        >
            Checkout
        </button>
    );
}

export default CheckoutButton;