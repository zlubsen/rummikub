module.exports = {
  purge: [],
  theme: {
    inset: {
      '-16': '-4rem',
      '-1': '-0.5rem',
      '1': '0.25rem',
      '2': '0.5rem',

    },
    extend: {},
  },
  variants: {
    backgroundColor: ['responsive', 'hover'],
    width: ['responsive', 'hover'],
    // display: ['responsive', 'hover', 'focus'],
  },
  plugins: [
    require('@tailwindcss/custom-forms'),
  ],
}
