// src/tests/components/FullUserProfile.test.tsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent, within } from "@testing-library/react";
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
  profilePicture: "",
  accessToken: ""
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

    const lastNameInput = await screen.findByDisplayValue("Kovács");
    expect(lastNameInput).toBeInTheDocument();

    const firstNameInput = await screen.findByDisplayValue("János");
    expect(firstNameInput).toBeInTheDocument();

    const emailNameInput = await screen.findByDisplayValue("janos@example.com");
    expect(emailNameInput).toBeInTheDocument();
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

    const container = lastNameInput.closest("form") || lastNameInput.closest("div");
    if (!container) throw new Error("Cannot find container for input");

    // toggle edit to enable input
    const editButton = within(container).getAllByRole("button", { name: /Módosítás|Szerkesztés/i })[0];
    fireEvent.click(editButton);

    fireEvent.change(lastNameInput, { target: { value: "Nagy" } });

    const saveButton = within(container).getByRole("button", { name: /Mentés/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(privateAxios.patch).toHaveBeenCalled();
      expect(screen.getByText(/Frissítés sikeres/i)).toBeInTheDocument();
    });
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

    const container = lastNameInput.closest("form") || lastNameInput.closest("div");
    if (!container) throw new Error("Cannot find container for input");

    // toggle edit to enable input
    const editButton = within(container).getAllByRole("button", { name: /Módosítás|Szerkesztés/i })[0];
    fireEvent.click(editButton);

    fireEvent.change(lastNameInput, { target: { value: "Nagy" } });

    const saveButton = within(container).getByRole("button", { name: /Mentés/i });
    fireEvent.click(saveButton);

    await waitFor(() => {
      expect(privateAxios.patch).toHaveBeenCalled();
      expect(screen.getByText(/Sikertelen frissítés!/i)).toBeInTheDocument();
    });
  });
});
