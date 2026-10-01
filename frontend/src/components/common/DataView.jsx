import PropTypes from "prop-types";
import Loader from "../Loader";

/**
 * Renders the loading spinner or the failure message until the data has arrived,
 * then the view itself.
 *
 * The content is supplied as a function rather than as plain children so that it
 * is only built once the data exists - passing it as elements would evaluate the
 * view eagerly and dereference data that is still null.
 */
const DataView = ({loaded, error, children}) => (
    <>
        {!loaded && <Loader error={error}/>}
        {loaded && children()}
    </>
);

DataView.propTypes = {
    loaded: PropTypes.bool,
    error: PropTypes.object,
    children: PropTypes.func.isRequired,
};

export default DataView;
