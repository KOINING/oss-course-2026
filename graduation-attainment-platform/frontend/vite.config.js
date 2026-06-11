import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const LOCAL_API_TARGET = 'http://localhost:8080'
// 远端联调备用地址，当前默认开发代理不使用它。
const REMOTE_API_TARGET = 'http://39.104.52.187'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: LOCAL_API_TARGET,
        changeOrigin: true,
      },
      '/health': {
        target: LOCAL_API_TARGET,

        changeOrigin: true,
      },
    },
  },
})
