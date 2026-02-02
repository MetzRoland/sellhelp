import axios from "axios";

const baseUrl = "http://localhost:8080";

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