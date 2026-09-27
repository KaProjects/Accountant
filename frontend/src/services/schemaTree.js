/**
 * Drops group nodes whose class is not itself expanded.
 *
 * The tree reports every node the user has ever opened, so collapsing a class
 * must implicitly collapse the groups beneath it.
 */
export function keepReachableNodes(nodeIds) {
    return nodeIds.filter((nodeId) =>
        !nodeId.includes("g") || nodeIds.includes(nodeId.split("g")[0]));
}
