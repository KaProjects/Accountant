import {useNavigate} from "react-router-dom";
import {useAppState} from "../state/appState";

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
 * Returns to the start and forgets the year being looked at.
 *
 * The year was previously forgotten by clearing it from session storage and letting the reload
 * read the default back out. Without a reload it has to be set, or the bar would keep showing the
 * old year.
 */
export const useGoHome = () => {
    const navigate = useNavigate();
    const {setYear} = useAppState();

    return () => {
        setYear(new Date().getFullYear());
        navigate("/");
    };
};
