import {useNavigate} from "react-router-dom";

/**
 * Moves within the application.
 *
 * Every destination used to be reached by assigning to {@code window.location.href}, which makes
 * the browser reload the whole page: the bundle is parsed again, React state is lost, and the
 * session has to be re-established with the backend on every click. Routing it through the router
 * keeps the application running, which is what it was built as.
 */
export const useGoTo = () => {
    const navigate = useNavigate();
    return (path) => () => navigate(path);
};

/**
 * Returns to the start, which also forgets the year being looked at: the year is a query
 * parameter and the start has none.
 */
export const useGoHome = () => {
    const navigate = useNavigate();
    return () => navigate("/");
};
