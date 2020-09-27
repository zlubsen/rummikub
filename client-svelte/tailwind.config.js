const production = !process.env.ROLLUP_WATCH;
module.exports = {
  purge: {
    content: [
      './src/**/*.html',
      './src/**/*.svelte',
      './src/index.html'
    ],
    enabled: production},
  theme: {
    cursor: {
      auto: 'auto',
      default: 'default',
      pointer: 'pointer',
      wait: 'wait',
      text: 'text',
      move: 'move',
      'ew-resize': 'ew-resize',
    },
    flex: {
      '0': '0 0 auto',
      '1': '1 1 0%',
      auto: '1 1 auto',
      initial: '0 1 auto',
      none: 'none',
    },
    extend: {},
    future: { // for tailwind 2.0 compat
      purgeLayersByDefault: true,
      removeDeprecatedGapUtilities: true,
    },
  },
  variants: {
    backgroundColor: ['responsive', 'hover'],
    width: ['responsive', 'hover'],
    cursor: ['responsive', 'hover'],
    // display: ['responsive', 'hover', 'focus'],
  },
  plugins: [
    require('@tailwindcss/custom-forms'),
    require('tailwindcss-font-inter')()
  ],
}
