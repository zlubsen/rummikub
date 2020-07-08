// const production = process.env.NODE_ENV !== 'development'
// const production = !process.env.ROLLUP_WATCH
//
// module.exports = {
//     plugins: [
//         // require('postcss-import')(),
//         require('tailwindcss'),
//         ...(production ? [purgecss] : [])
//     ]
// };

// const plugins =
//     process.env.NODE_ENV === 'production'
//         ? ['tailwindcss', 'autoprefixer', '@fullhuman/postcss-purgecss']
//         : ['tailwindcss'];
//
// module.exports = {plugins};

const production = !process.env.ROLLUP_WATCH;
const purgecss = require("@fullhuman/postcss-purgecss");

module.exports = {
    plugins: [
        require("postcss-import")(),
        require("tailwindcss"),
        require("autoprefixer"),
        // Only purge css on production
        production &&
        purgecss({
            content: ["./**/*.html", "./**/*.svelte"],
            whitelistPatterns: [/svelte-/],
            defaultExtractor: content => content.match(/[A-Za-z0-9-_:/]+/g) || []
        })
    ]
};