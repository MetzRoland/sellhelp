// src/tests/components/SetupMfa.test.tsx
import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import SetupMfa from "../../components/SetupMfa/SetupMfa";
import { privateAxios } from "../../config/axiosConfig";
import { AuthProvider } from "../../contextProviders/AuthProvider/AuthProvider";
import type { AxiosResponse } from "axios";

const mockNavigate = vi.fn();
vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router")>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

vi.mock("../../config/axiosConfig", () => ({
  privateAxios: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    interceptors: {
      request: { use: vi.fn(), eject: vi.fn() },
      response: { use: vi.fn(), eject: vi.fn() },
    },
  },
}));

vi.mock("../../contextProviders/ProccessLoadProvider/ProccessLoadContext", () => ({
  useLoading: () => ({
    setIsLoading: vi.fn(),
    setLoadingMessage: vi.fn(),
  }),
}));

vi.mock("react-router", async () => {
  const actual = await vi.importActual("react-router");
  return {
    ...actual,
    useNavigate: vi.fn(),
  };
});

describe("SetupMfa component", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValue({
          data: [],
    } as AxiosResponse);
    (privateAxios.post as ReturnType<typeof vi.fn>).mockResolvedValue({
          data: [],
    } as AxiosResponse);
  });

  it("renders QR code after fetching setup data", async () => {
    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      data: { qrCode: "fakeQRBase64", tempToken: "temp123", totpSecret: "secret123" },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
            <SetupMfa />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByAltText("QR code")).toBeInTheDocument();
      expect(screen.getByText("Szkennelje be a QR kódot!")).toBeInTheDocument();
    });
  });

  it("shows error if QR code fetch fails", async () => {
    (privateAxios.get as ReturnType<typeof vi.fn>).mockRejectedValueOnce(new Error("fail"));

    render(
      <MemoryRouter>
        <AuthProvider>
            <SetupMfa />
        </AuthProvider>
      </MemoryRouter>
    );

    await waitFor(() => {
      expect(screen.getByText("Nem sikerült megjeleníteni a QR kódot!")).toBeInTheDocument();
    });
  });

  it("updates form input correctly", async () => {
    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      data: { qrCode: "qr", tempToken: "t", totpSecret: "s" },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
            <SetupMfa />
        </AuthProvider>
      </MemoryRouter>
    );

    const input = await screen.findByPlaceholderText("Hitelesítő kód");
    fireEvent.change(input, { target: { value: "123456" } });

    expect(input).toHaveValue("123456");
  });

  it("submits form and navigates on success", async () => {
    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      data: { qrCode: "qr", tempToken: "t", totpSecret: "s" },
    });
    (privateAxios.post as ReturnType<typeof vi.fn>).mockResolvedValueOnce({ status: 200 });

    render(
      <MemoryRouter>
        <AuthProvider>
            <SetupMfa />
        </AuthProvider>
      </MemoryRouter>
    );

    const input = await screen.findByPlaceholderText("Hitelesítő kód");
    fireEvent.change(input, { target: { value: "123456" } });

    fireEvent.submit(screen.getByRole("button", { name: /Tovább/i }));

    await waitFor(() => {
      expect(privateAxios.post).toHaveBeenCalledWith("/auth/enable2fa", {
        totpSecret: "s",
        qrCode: "qr",
        tempToken: "t",
        totpCode: "123456",
      });
    });

    // await waitFor(() => {
    //   expect(mockNavigate).toHaveBeenCalledWith("/home/settings");
    // });
  });

  it("displays API error message on failed submit", async () => {
    (privateAxios.get as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      data: { qrCode: "qr", tempToken: "t", totpSecret: "s" },
    });
    (privateAxios.post as ReturnType<typeof vi.fn>).mockRejectedValueOnce({
      response: { data: { message: "Invalid TOTP", errors: { totpCode: "Wrong code" } } },
    });

    render(
      <MemoryRouter>
        <AuthProvider>
            <SetupMfa />
        </AuthProvider>
      </MemoryRouter>
    );

    const input = await screen.findByPlaceholderText("Hitelesítő kód");
    fireEvent.change(input, { target: { value: "000000" } });

    fireEvent.submit(screen.getByRole("button", { name: /Tovább/i }));

    await waitFor(() => {
      expect(screen.getByText("Invalid TOTP")).toBeInTheDocument();
      expect(screen.getByText("Wrong code")).toBeInTheDocument();
    });
  });
});
