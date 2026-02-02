import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { describe, it, vi, beforeEach, expect } from "vitest";
import { MemoryRouter } from "react-router";
import Login from "../../components/Login/Login";
import type { AuthContextType } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import type { GoogleRegister } from "../../contextProviders/AuthProvider/AuthProviderTypes";

// Mock useNavigate from react-router
const mockNavigate = vi.fn();
vi.mock("react-router", async (importOriginal) => {
  const actual = await importOriginal<typeof import("react-router")>();
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

// Mock Auth Context
let authMockState: AuthContextType;
const mockLoginLocal = vi.fn();
const mockVerifyTotp = vi.fn();
const mockHandleGoogleLogin = vi.fn();
const mockSetValidationErrors = vi.fn();
const mockSetAuthError = vi.fn();

vi.mock("../../contextProviders/AuthProvider/AuthContext", () => ({
  useAuth: () => authMockState,
}));

// Mock Loading Context
vi.mock(
  "../../contextProviders/ProccessLoadProvider/ProccessLoadContext",
  () => ({
    useLoading: () => ({
      setIsLoading: vi.fn(),
      setLoadingMessage: vi.fn(),
    }),
  }),
);

// Mock Header/Footer
vi.mock("../Header/Header", () => ({ default: () => <div data-testid="header" /> }));
vi.mock("../Footer/Footer", () => ({ default: () => <div data-testid="footer" /> }));

const renderLogin = (isAdminLogin = false) => {
  render(
    <MemoryRouter>
      <Login isAdminLogin={isAdminLogin} />
    </MemoryRouter>,
  );
};

beforeEach(() => {
  vi.clearAllMocks();

  authMockState = {
    isAuthenticated: false,
    authLoading: false,
    logout: vi.fn().mockResolvedValue(undefined),
    loginLocal: mockLoginLocal,
    tempToken: null,
    setTempToken: vi.fn(),
    verifyTotp: mockVerifyTotp,
    validationErrors: {
      email: "",
      password: "",
      totpCode: "",
    },
    setGoogleRegisterErrors: vi.fn(),
    accessToken: null,
    setAccessToken: vi.fn(),
    setValidationErrors: mockSetValidationErrors,
    authError: "",
    setAuthError: mockSetAuthError,
    user: null,
    handleGoogleLogin: mockHandleGoogleLogin,
    googleRegisterErrors: {} as GoogleRegister,
    finishGoogleRegistration: vi.fn().mockResolvedValue(undefined)
  };
});

describe("Login component", () => {
  it("renders login form", () => {
    renderLogin();

    expect(screen.getByRole("button", { name: "Bejelentkezés" })).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Email")).toBeInTheDocument();
    expect(screen.getByPlaceholderText("Jelszó")).toBeInTheDocument();
  });

  it("submits login form and calls loginLocal", async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText("Email"), "test@test.com");
    await user.type(screen.getByPlaceholderText("Jelszó"), "password123");
    await user.click(screen.getByRole("button", { name: "Bejelentkezés" }));

    expect(mockLoginLocal).toHaveBeenCalledWith("/auth/login", {
      email: "test@test.com",
      password: "password123",
    });
  });

  it("calls Google login when clicking Google button", async () => {
    const user = userEvent.setup();
    renderLogin();

    await user.click(screen.getByRole("button", { name: /google/i }));

    expect(mockHandleGoogleLogin).toHaveBeenCalled();
  });

  it("renders TOTP form when tempToken exists", () => {
    authMockState.tempToken = "temp-token";
    renderLogin();

    expect(screen.getByPlaceholderText("Hitelesítő kód")).toBeInTheDocument();
  });

  it("calls verifyTotp when submitting TOTP form", async () => {
    authMockState.tempToken = "temp-token";
    const user = userEvent.setup();
    renderLogin();

    await user.type(screen.getByPlaceholderText("Hitelesítő kód"), "123456");
    await user.click(screen.getByRole("button", { name: "Ellenőrzés" }));

    expect(mockVerifyTotp).toHaveBeenCalledWith({
      totpCode: "123456",
      tempToken: "temp-token",
    });
  });

  it("navigates to inactive profile if user is blocked", async () => {
    authMockState.authError = "A felhasználó le van tiltva!";
    renderLogin();

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/profileInactive");
    });
  });

  it("renders admin login title when isAdminLogin is true", () => {
    renderLogin(true);

    expect(screen.getByText("Admin Belépés")).toBeInTheDocument();
  });
});
