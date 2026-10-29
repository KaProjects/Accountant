import axios from "axios";
import {properties} from "../properties";
import {errorDetail} from "./errors";
import {credentialed} from "./session";

/**
 * The sync actions the admin page offers.
 *
 * They re-import the desktop application's exported data into the database the web application
 * reads. The web application cannot write accounting data of its own yet, so this is how real
 * data gets in, and it goes away when that changes.
 */
export const syncActions = (year) => [
    {
        id: "year",
        label: "Sync " + year,
        path: "/sync/" + year,
        caption: "Re-imports the schema, accounts and transactions of " + year + " only.",
    },
    {
        id: "all",
        label: "Sync All Years",
        path: "/sync/all",
        caption: "Re-imports every year found in the data export.",
    },
    {
        id: "validate",
        label: "Sync All Years & Validate",
        path: "/sync/all/validate",
        caption: "Re-imports every year and then checks each one for inconsistencies. Answers with an error if any year fails its checks.",
    },
];

/**
 * Runs one sync action and reports how it went.
 *
 * The endpoints answer in plain text, and the interesting part of a failure is what its body says
 * rather than the status: a validation failure arrives as 422 whose detail is the report of every
 * year, the failed ones included. Both outcomes are therefore reported the same way, and neither
 * throws.
 */
export const runSync = async (path) => {
    try {
        const response = await axios.get(properties.backend + path, credentialed);
        return {
            ok: true,
            status: response.status + " " + response.statusText,
            body: typeof response.data === "string" ? response.data : JSON.stringify(response.data),
        };
    } catch (error) {
        return {
            ok: false,
            status: error.response ? error.response.status + " " + error.response.statusText : "no response",
            body: errorDetail(error),
        };
    }
};
