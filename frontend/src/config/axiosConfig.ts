import axios from "axios";

const baseUrl = import.meta.env.VITE_API_BASE_URL;

export const publicAxios = axios.create({
  baseURL: baseUrl,
  headers: {
    "Content-Type": "application/json",
  }
});

export const privateAxios = axios.create({
  baseURL: baseUrl,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true
});

export const refreshAxios = axios.create({
  baseURL: baseUrl,
  withCredentials: true,
});

console.log(baseUrl);