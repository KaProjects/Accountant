import {useEffect, useState} from "react";

/**
 * Where the columns of a table actually are: {width, columns: [{left, width}]}, one per header
 * cell, measured from the left edge of the table. Null until the table is there to be measured.
 *
 * A chart that lines its points up under the table needs this from the table itself, because the
 * table sizes its columns to their contents. It is measured again whenever the table changes size -
 * a wider window, different figures - and whenever `changed` does, which the caller passes the
 * data the table is drawn from.
 */
export function useColumnLayout(tableRef, changed) {
    const [layout, setLayout] = useState(null);

    useEffect(() => {
        const table = tableRef.current;
        if (table === null || table === undefined) {
            setLayout(null);
            return undefined;
        }

        const measure = () => setLayout({
            width: table.offsetWidth,
            columns: Array.from(table.querySelectorAll("thead th")).map((cell) => ({
                left: cell.offsetLeft,
                width: cell.offsetWidth,
            })),
        });
        measure();

        const observer = new ResizeObserver(measure);
        observer.observe(table);
        return () => observer.disconnect();
    }, [tableRef, changed]);

    return layout;
}
