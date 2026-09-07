/**
 * postcss.config.js - Required by Create React App (react-scripts 5)
 * to process Tailwind CSS directives (@tailwind base/components/utilities).
 *
 * WHY THIS FILE IS NEEDED:
 * src/index.css contains "@tailwind base;" etc.
 * CRA uses PostCSS to process CSS files.
 * This config tells PostCSS to run two plugins:
 *   1. tailwindcss  - reads tailwind.config.js and generates CSS utility classes
 *   2. autoprefixer - adds vendor prefixes (-webkit-, -moz-) for browser compatibility
 *
 * Without this file, Tailwind classes like "bg-blue-600" will NOT be generated
 * and the entire UI will be unstyled.
 */
module.exports = {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
};
