// src/tests/components/Register.test.tsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import Register from "../../components/Register/Register";
import { publicAxios } from "../../config/axiosConfig";
import { AuthProvider } from "../../contextProviders/AuthProvider/AuthProvider";
import type { AxiosResponse } from "axios";

vi.mock("../../config/axiosConfig", () => {
  return {
    publicAxios: {
      get: vi.fn<() => Promise<AxiosResponse>>(),
      post: vi.fn<() => Promise<AxiosResponse>>(),
    },
    privateAxios: {
      interceptors: {
        request: {
          use: vi.fn(),
          eject: vi.fn(),
        },
        response: {
          use: vi.fn(),
          eject: vi.fn(),
        },
      },
    },
  };
});

vi.mock("../../contextProviders/ProccessLoadProvider/ProccessLoadContext", () => ({
  useLoading: () => ({
    setIsLoading: vi.fn(),
    setLoadingMessage: vi.fn(),
  }),
}));

describe("Register component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (publicAxios.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: [],
    } as AxiosResponse);
  });

  it("renders form inputs", () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <Register />
        </AuthProvider>
      </MemoryRouter>
    );

    expect(screen.getByPlaceholderText("Vezetéknév")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Keresztnév")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Születési dátum")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Jelszó")).toBeInTheDocument();
  });

  it("fetches cities on mount", async () => {
    (publicAxios.get as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      data: [{ id: 1, cityName: "Budapest" }],
    } as AxiosResponse);

    render(
      <MemoryRouter>
        <AuthProvider>
          <Register />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(publicAxios.get).toHaveBeenCalledWith("/api/public/cities");
    });
  });

  it("shows success message on successful registration", async () => {
    (publicAxios.post as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      status: 201,
    } as AxiosResponse);

    render(
      <MemoryRouter>
        <AuthProvider>
          <Register />
        </AuthProvider>
      </MemoryRouter>
    );

    fireEvent.change(screen.getByPlaceholderText("Vezetéknév"), {
      target: { value: "Kovacs" },
    });
    fireEvent.change(screen.getByPlaceholderText("Keresztnév"), {
      target: { value: "Janos" },
    });
    fireEvent.change(screen.getByPlaceholderText("Email"), {
      target: { value: "test@example.com" },
    });
    fireEvent.change(screen.getByPlaceholderText("Jelszó"), {
      target: { value: "password123" },
    });

    fireEvent.submit(screen.getByRole("button", { name: /Regisztráció/i }));

    await waitFor(() => {
      expect(screen.getByText("Sikeres regisztráció!")).toBeInTheDocument();
    });
  });

  it("shows error message on failed registration", async () => {
    (publicAxios.post as ReturnType<typeof vi.fn>).mockRejectedValueOnce({
      response: { data: { message: "Email already exists" } },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
          <Register />
        </AuthProvider>
      </MemoryRouter>
    );

    fireEvent.submit(screen.getByRole("button", { name: /Regisztráció/i }));

    await waitFor(() => {
      expect(screen.getByText("Email already exists")).toBeInTheDocument();
    });
  });
});
