import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import PostsListComponent from "../../components/PostsListComponent/PostsListComponent";
import type { Post } from "../../components/PostsListComponent/PostsListComponentTypes";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

const mockNavigate = vi.fn();
vi.mock("react-router", () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock("../../components/Header/Header", () => ({
  default: () => <div data-testid="header" />,
}));
vi.mock("../../components/Footer/Footer", () => ({
  default: () => <div data-testid="footer" />,
}));

vi.mock("../../components/PostView/PostView", () => ({
  default: ({ post, handleOnClick }: { post: Post; handleOnClick: () => void }) => (
    <div data-testid="post" onClick={handleOnClick}>
      {post.title}
    </div>
  ),
}));

vi.mock("../../components/Reusables/InputForm/InputForm", () => ({
  default: () => <div data-testid="input-form" />,
}));

const setIsLoadingMock = vi.fn();
const setLoadingMessageMock = vi.fn();

vi.mock("../../contextProviders/ProccessLoadProvider/ProccessLoadContext", () => ({
  useLoading: () => ({
    setIsLoading: setIsLoadingMock,
    setLoadingMessage: setLoadingMessageMock,
    isLoading: false,
    loadingMessage: "",
  }),
}));

vi.mock("../../config/axiosConfig", () => ({
  privateAxios: { get: vi.fn() },
  publicAxios: { get: vi.fn() },
}));

import { privateAxios, publicAxios } from "../../config/axiosConfig";

const mockUser: User = {
  id: 1,
  email: "user@test.com",
  firstName: "Test",
  lastName: "User",
  mfa: false,
  birthDate: new Date(),
  banned: false,
  cityName: "Budapest",
  role: "USER",
  createdAt: new Date(),
  authProvider: "local",
  accessToken: "token",
};

const mockPost: Post = {
  id: 1,
  title: "Test Post",
  description: "Test description",
  cityName: "Budapest",
  reward: 1000,
  statusName: "OPEN",
  createdAt: new Date(),

  publisher: mockUser,
  selectedUser: mockUser,
  jobApplications: [],
  comments: [],
};

describe("PostsListComponent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders header, footer and title", () => {
    render(
      <PostsListComponent title="Post List" postFetchingEndpoint="/posts" />
    );

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();
    expect(screen.getByText("Post List")).toBeInTheDocument();
  });

  it("fetches and displays posts", async () => {
    vi.mocked(privateAxios.get).mockResolvedValueOnce({ data: [mockPost] });
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: [] });

    render(
      <PostsListComponent title="Post List" postFetchingEndpoint="/posts" />
    );

    const post = await screen.findByText("Test Post");
    expect(post).toBeInTheDocument();
  });

  it("shows empty message when no posts", async () => {
    vi.mocked(privateAxios.get).mockResolvedValueOnce({ data: [] });
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: [] });

    render(
      <PostsListComponent title="Post List" postFetchingEndpoint="/posts" />
    );

    await waitFor(() => {
      expect(screen.getByText("Nincsenek posztok")).toBeInTheDocument();
    });
  });

  it("navigates when post clicked", async () => {
    vi.mocked(privateAxios.get).mockResolvedValueOnce({ data: [mockPost] });
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: [] });

    render(
      <PostsListComponent title="Post List" postFetchingEndpoint="/posts" />
    );

    const post = await screen.findByText("Test Post");
    fireEvent.click(post);

    expect(mockNavigate).toHaveBeenCalledWith("/posts/1");
  });
});