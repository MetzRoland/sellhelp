import axios from "axios";

const baseUrl = "http://localhost:8080";

export const publicAxois = axios.create({
  baseURL: baseUrl,
  headers: {
    "Content-Type": "application/json",
  }
});

export const privateAxois = axios.create({
  baseURL: baseUrl,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true
});