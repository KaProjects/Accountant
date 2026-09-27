import {keepReachableNodes} from "../schemaTree";

describe("keepReachableNodes", () => {
    it("keeps class nodes as they are", () => {
        expect(keepReachableNodes(["c0", "c1"])).toEqual(["c0", "c1"]);
    });

    it("keeps a group whose class is also expanded", () => {
        expect(keepReachableNodes(["c0", "c0g1"])).toEqual(["c0", "c0g1"]);
    });

    it("drops a group whose class is no longer expanded", () => {
        expect(keepReachableNodes(["c0g1"])).toEqual([]);
    });

    it("drops only the unreachable groups", () => {
        expect(keepReachableNodes(["c0", "c0g0", "c1g0"])).toEqual(["c0", "c0g0"]);
    });

    it("handles an empty selection", () => {
        expect(keepReachableNodes([])).toEqual([]);
    });
});
