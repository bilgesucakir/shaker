import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// During `npm run dev` the SPA runs on :5173 and proxies /api to the Spring app on :8080,
// so cookies stay same-origin. `npm run build` (run by the Maven frontend plugin) emits
// straight into src/main/resources/static so a single `mvn spring-boot:run` serves everything.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true
  }
})
