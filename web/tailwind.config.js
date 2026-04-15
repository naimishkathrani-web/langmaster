/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        primary: '#1565C0',
        'primary-red': '#C62828',
        'surface-light': '#FFFFFF',
        'surface-dark': '#121212',
        'accent-blue': '#64B5F6',
        'accent-red': '#EF9A9A',
        'tick-neutral': '#90A4AE',
      }
    }
  },
  plugins: []
}
