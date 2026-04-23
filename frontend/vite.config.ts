import { defineConfig } from "vitest/config";
import react from "@vitejs/plugin-react";
import dns from 'dns'

dns.setDefaultResultOrder('verbatim');

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: "./src/tests/setup.ts",
  }
});
