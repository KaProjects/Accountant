// Loaded automatically by react-scripts before every test file.
// Adds the jest-dom matchers (toBeInTheDocument, toHaveTextContent, ...).
import '@testing-library/jest-dom';

// jsdom implements no layout, so it has no ResizeObserver - and recharts' ResponsiveContainer
// constructs one as soon as it renders. Without this, any test that renders a chart, or a
// component that happens to contain one, fails with a ReferenceError rather than an assertion.
global.ResizeObserver = global.ResizeObserver || class {
    observe() {}
    unobserve() {}
    disconnect() {}
};
