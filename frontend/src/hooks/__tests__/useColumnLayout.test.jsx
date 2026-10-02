import {useRef} from "react";
import {render, screen} from "@testing-library/react";
import {useColumnLayout} from "../useColumnLayout";

// jsdom lays nothing out, so every offset reads as nought; the table is given a layout of its own.
// The test setup resets every mock before each test, so the layout is given before each one too.
const offsets = {Statement: [0, 200], Initial: [200, 120], January: [320, 80]};

beforeEach(() => {
    jest.spyOn(HTMLElement.prototype, "offsetLeft", "get").mockImplementation(function offsetLeft() {
        return offsets[this.textContent]?.[0] ?? 0;
    });
    jest.spyOn(HTMLElement.prototype, "offsetWidth", "get").mockImplementation(function offsetWidth() {
        return this.tagName === "TABLE" ? 400 : offsets[this.textContent]?.[1] ?? 0;
    });
});

afterEach(() => jest.restoreAllMocks());

const Measured = () => {
    const tableRef = useRef(null);
    const layout = useColumnLayout(tableRef, "data");
    return (
        <>
            <table ref={tableRef}>
                <thead><tr><th>Statement</th><th>Initial</th><th>January</th></tr></thead>
            </table>
            <output>{JSON.stringify(layout)}</output>
        </>
    );
};

describe("useColumnLayout", () => {
    it("reads where the table put each of its columns", () => {
        render(<Measured/>);

        expect(JSON.parse(screen.getByRole("status").textContent)).toEqual({
            width: 400,
            columns: [{left: 0, width: 200}, {left: 200, width: 120}, {left: 320, width: 80}],
        });
    });

    it("has nothing to report while there is no table", () => {
        const Nothing = () => {
            const tableRef = useRef(null);
            return <output>{JSON.stringify(useColumnLayout(tableRef, "data"))}</output>;
        };
        render(<Nothing/>);

        expect(screen.getByRole("status")).toHaveTextContent("null");
    });
});
