import {columnScale} from "../columnScale";

const layout = {
    width: 600,
    columns: [
        {name: "Cash Flow Statement", left: 0, width: 200},
        {name: "Initial", left: 200, width: 120},
        {name: "January", left: 320, width: 80},
        {name: "February", left: 400, width: 100},
        {name: "Total", left: 500, width: 100},
    ],
};

describe("columnScale", () => {
    const scale = () => columnScale(["Initial", "January", "February"], layout);

    it("makes every band as wide as the narrowest column charted", () => {
        expect(scale().bandwidth()).toBe(80);
    });

    it("centres each category's band on the column of the same name", () => {
        const band = scale();

        expect(band("January") + band.bandwidth() / 2).toBe(360);
        expect(band("February") + band.bandwidth() / 2).toBe(450);
        expect(band("Initial") + band.bandwidth() / 2).toBe(260);
    });

    it("places nothing for a category the table has no column for", () => {
        expect(scale()("March")).toBeUndefined();
    });

    it("takes the domain and range recharts sets, and hands them back", () => {
        const band = scale().domain(["Initial", "January"]).range([200, 600]);

        expect(band.domain()).toEqual(["Initial", "January"]);
        expect(band.range()).toEqual([200, 600]);
        expect(band("January") + band.bandwidth() / 2).toBe(360);
    });

    it("copies itself with what it was set to", () => {
        const copy = scale().domain(["January"]).range([200, 600]).copy();

        expect(copy.domain()).toEqual(["January"]);
        expect(copy("January")).toBe(scale()("January"));
    });
});
