import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router";
import FullUserProfile from "../../components/FullUserProfile/FullUserProfile";
import { AuthProvider } from "../../contextProviders/AuthProvider/AuthProvider";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import type { AxiosResponse } from "axios";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

vi.mock("../../config/axiosConfig", () => {
  return {
    publicAxios: {
      get: vi.fn<() => Promise<AxiosResponse>>(),
    },
    privateAxios: {
      get: vi.fn<() => Promise<AxiosResponse>>(),
      patch: vi.fn<() => Promise<AxiosResponse>>(),
      interceptors: {
        request: { use: vi.fn(), eject: vi.fn() },
        response: { use: vi.fn(), eject: vi.fn() },
      },
    },
  };
});

vi.mock("../../contextProviders/ProccessLoadProvider/ProccessLoadContext", () => ({
  useLoading: () => ({
    setIsLoading: vi.fn(),
    setLoadingMessage: vi.fn(),
    isLoading: false,
  }),
}));

const mockUser: User = {
  id: 1,
  firstName: "János",
  lastName: "Kovács",
  email: "janos@example.com",
  birthDate: new Date("1990-01-01"),
  cityName: "Budapest",
  role: "ROLE_USER",
  authProvider: "LOCAL",
  mfa: false,
  banned: false,
  createdAt: new Date(),
  accessToken: "",
};

describe("FullUserProfile component", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: mockUser,
    } as AxiosResponse);

    (publicAxios.get as ReturnType<typeof vi.fn>).mockResolvedValue({
      data: [{ id: 1, cityName: "Budapest" }],
    } as AxiosResponse);
  });

  it("renders user data when loaded by id", async () => {
    render(
      <MemoryRouter initialEntries={["/profile/1"]}>
        <AuthProvider>
          <Routes>
            <Route path="/profile/:id" element={<FullUserProfile />} />
          </Routes>
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(privateAxios.get).toHaveBeenCalledWith("/user/users/1");
    });

    expect(await screen.findByDisplayValue("Kovács")).toBeInTheDocument();
    expect(await screen.findByDisplayValue("János")).toBeInTheDocument();
    expect(await screen.findByDisplayValue("janos@example.com")).toBeInTheDocument();
  });

  it("fetches cities when settings mode is enabled", async () => {
    render(
      <MemoryRouter>
        <AuthProvider>
          <FullUserProfile settings />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(publicAxios.get).toHaveBeenCalledWith("/api/public/cities");
    });
  });

  it("shows success message on successful update", async () => {
    (privateAxios.patch as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      status: 200,
    } as AxiosResponse);

    render(
      <MemoryRouter>
        <AuthProvider>
          <FullUserProfile settings />
        </AuthProvider>
      </MemoryRouter>
    );

    const lastNameInput = await screen.findByDisplayValue("Kovács");

    const editButton = lastNameInput
      .closest(".setting-container")
      ?.querySelector("button");

    if (!editButton) throw new Error("Edit button not found");

    fireEvent.click(editButton);

    fireEvent.change(lastNameInput, { target: { value: "Nagy" } });

    fireEvent.click(editButton);

    await waitFor(() => {
      expect(privateAxios.patch).toHaveBeenCalled();
    });

    expect(screen.getByText(/Frissítés sikeres/i)).toBeInTheDocument();
  });

  it("shows error message on failed update", async () => {
    (privateAxios.patch as ReturnType<typeof vi.fn>).mockRejectedValueOnce({
      response: { data: { message: "Sikertelen frissítés!" } },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
          <FullUserProfile settings />
        </AuthProvider>
      </MemoryRouter>
    );

    const lastNameInput = await screen.findByDisplayValue("Kovács");

    const editButton = lastNameInput
      .closest(".setting-container")
      ?.querySelector("button");

    if (!editButton) throw new Error("Edit button not found");

    fireEvent.click(editButton);

    fireEvent.change(lastNameInput, { target: { value: "Nagy" } });

    fireEvent.click(editButton);

    await waitFor(() => {
      expect(privateAxios.patch).toHaveBeenCalled();
    });

    expect(screen.getByText(/Sikertelen frissítés!/i)).toBeInTheDocument();
  });
});