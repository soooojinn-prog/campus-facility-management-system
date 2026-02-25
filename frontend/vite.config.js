import react from '@vitejs/plugin-react-swc';
import {defineConfig} from 'vite';

// https://vite.dev/config/
export default defineConfig({
  server: {
    open: true, port: 8000, proxy: {
      '/api': {
        target: 'http://localhost:8080', changeOrigin: true,
      },
    },
  }, plugins: [react()], base: '/',
});
