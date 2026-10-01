import {createContext, useContext} from "react";

/**
 * The state the main bar and the views share.
 *
 * It is context rather than props because it is not passed in one direction: the chart view
 * publishes the values the main bar offers in its selector, and the main bar publishes the year
 * every view reads. Handing the whole of it to every route as props - which is what this replaced
 * - meant each view's signature claimed state it never touched, and adding one field changed
 * every signature.
 */
const AppStateContext = createContext(null);

export const AppStateProvider = AppStateContext.Provider;

export const useAppState = () => {
    const state = useContext(AppStateContext);

    if (state === null) {
        throw new Error("useAppState was called outside an AppStateProvider");
    }
    return state;
};
