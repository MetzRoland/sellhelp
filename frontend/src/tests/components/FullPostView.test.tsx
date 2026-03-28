import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import type { Mock } from "vitest";
import FullPostView from "../../components/FullPostView/FullPostView";
import type { Post } from "../../components/PostsListComponent/PostsListComponentTypes";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import * as AuthContext from "../../contextProviders/AuthProvider/AuthContext";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import type { LoginForm } from "../../components/Login/LoginTypes";
import type { GoogleRegister } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import type { AuthContextType } from "../../contextProviders/AuthProvider/AuthProviderTypes";

vi.mock("react-router", () => ({
  useParams: () => ({ id: "1" }),
  useNavigate: () => vi.fn(),
  Link: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock("../../components/Header/Header", () => ({
  default: () => <div data-testid="header" />,
}));
vi.mock("../../components/Footer/Footer", () => ({
  default: () => <div data-testid="footer" />,
}));

vi.mock("../../components/Reusables/InputForm/InputForm", () => ({
  default: () => <div data-testid="input-form" />,
}));

vi.mock("../../components/UserListItem/UserListItem", () => ({
  default: ({ email }: { email: string }) => <div>{email}</div>,
}));

vi.mock("../../components/PostActionButton/PostActionButton", () => ({
  default: () => <div data-testid="post-action-button" />,
}));

vi.mock("../../components/Reusables/FileDisplay/FileDisplay", () => ({
  default: () => <div data-testid="file-display" />,
}));

const setIsLoadingMock = vi.fn();
const setLoadingMessageMock = vi.fn();
vi.mock(
  "../../contextProviders/ProccessLoadProvider/ProccessLoadContext",
  () => ({
    useLoading: () => ({
      setIsLoading: setIsLoadingMock,
      setLoadingMessage: setLoadingMessageMock,
    }),
  }),
);

vi.mock("../../contextProviders/AuthProvider/AuthContext", () => ({
  useAuth: () => ({
    user: { id: 1, role: "ROLE_USER" },
    isAuthenticated: true,
  }),
}));

vi.mock("../../config/axiosConfig", () => ({
  privateAxios: {
    get: vi.fn(),
    post: vi.fn(),
    patch: vi.fn(),
    delete: vi.fn(),
  },
  publicAxios: { get: vi.fn() },
}));

const mockUser: User = {
  id: 1,
  email: "owner@test.com",
  firstName: "Owner",
  lastName: "User",
  mfa: false,
  birthDate: new Date(),
  banned: false,
  cityName: "Budapest",
  role: "ROLE_USER",
  createdAt: new Date(),
  authProvider: "local",
  accessToken: "token",
};

const mockPost: Post = {
  id: 1,
  title: "Test Post",
  description: "Test Description",
  cityName: "Budapest",
  reward: 1000,
  statusName: "new",
  createdAt: new Date(),
  publisher: mockUser,
  selectedUser: {
    ...mockUser,
    id: 3,
    email: "selected@test.com",
    firstName: "Selected",
    lastName: "User",
  },
  jobApplications: [],
  comments: [],
};

describe("FullPostView", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("renders header, footer and title", async () => {
    (privateAxios.get as Mock).mockResolvedValueOnce({ data: mockPost }); // fetch post
    (publicAxios.get as Mock).mockResolvedValueOnce({ data: [] }); // fetch cities
    (privateAxios.get as Mock).mockResolvedValue({ data: [] }); // fetchApplied, optional

    render(<FullPostView fetchEndpoint="/post/posts/" />);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();

    const title = await screen.findByText("Test Post");
    expect(title).toBeInTheDocument();
  });

  it("shows InputForm and allows comment input for owner", async () => {
    (privateAxios.get as Mock).mockResolvedValueOnce({ data: mockPost });
    (publicAxios.get as Mock).mockResolvedValueOnce({ data: [] });
    (privateAxios.get as Mock).mockResolvedValue({ data: [] }); // fetchApplied

    render(<FullPostView fetchEndpoint="/post/posts/" />);

    const inputForm = await screen.findByTestId("input-form");
    expect(inputForm).toBeInTheDocument();

    const textarea = screen.getByPlaceholderText("Kommentelj valamit...");
    expect(textarea).toBeInTheDocument();

    fireEvent.change(textarea, { target: { value: "New comment" } });
    expect(textarea).toHaveValue("New comment");
  });

  it("displays 'Nincsenek kommentek!' when no comments", async () => {
    (privateAxios.get as Mock).mockResolvedValueOnce({ data: mockPost });
    (publicAxios.get as Mock).mockResolvedValueOnce({ data: [] });
    (privateAxios.get as Mock).mockResolvedValue({ data: [] }); // fetchApplied

    render(<FullPostView fetchEndpoint="/post/posts/" />);

    const noComments = await screen.findByText("Nincsenek kommentek!");
    expect(noComments).toBeInTheDocument();
  });

  it("renders PostActionButton for normal user", async () => {
    const mockNormalUser: User = {
      ...mockUser,
      id: 2,
      email: "normal@test.com",
      firstName: "Normal",
      lastName: "User",
    };

    const mockAuthContext: AuthContextType = {
      user: mockNormalUser,
      isAuthenticated: true,
      authLoading: false,
      loginLocal: async () => {},
      verifyTotp: async () => {},
      finishGoogleRegistration: async () => {},
      logout: async () => {},
      authError: null,
      setAuthError: () => {},
      tempToken: null,
      setTempToken: () => {},
      validationErrors: {} as LoginForm,
      setValidationErrors: () => {},
      googleRegisterErrors: {} as GoogleRegister,
      setGoogleRegisterErrors: () => {},
      accessToken: null,
      setAccessToken: () => {},
      handleGoogleLogin: () => {},
    };

    (privateAxios.get as Mock).mockResolvedValueOnce({ data: mockPost });
    (publicAxios.get as Mock).mockResolvedValueOnce({ data: [] });
    (privateAxios.get as Mock).mockResolvedValue({ data: [] });

    const useAuthSpy = vi
      .spyOn(AuthContext, "useAuth")
      .mockReturnValue(mockAuthContext);

    render(<FullPostView fetchEndpoint="/post/posts/" />);

    const actionBtn = await screen.findByTestId("post-action-button");
    expect(actionBtn).toBeInTheDocument();

    useAuthSpy.mockRestore();
  });

  it("shows PageNotFound if post fetch fails", async () => {
    const mockedPrivateGet = privateAxios.get as Mock;
    mockedPrivateGet.mockRejectedValueOnce(new Error("404"));
    mockedPrivateGet.mockResolvedValue({ data: [] });

    render(<FullPostView fetchEndpoint="/post/posts/" />);

    const pageNotFound = await screen.findByText("A poszt nem található!");
    expect(pageNotFound).toBeInTheDocument();
  });
});
