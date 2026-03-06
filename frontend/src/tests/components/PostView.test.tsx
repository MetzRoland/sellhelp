import { render, screen, fireEvent } from "@testing-library/react";
import { describe, it, expect, vi } from "vitest";
import PostView from "../../components/PostView/PostView";
import type { Post } from "../../components/PostsListComponent/PostsListComponentTypes";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import type {ProfilePictureComponentProps} from "../../components/ProfilePictureComponent/ProfilePictureComponentTypes";

const mockNavigate = vi.fn();

vi.mock("react-router", () => ({
  useNavigate: () => mockNavigate,
}));

vi.mock("../../components/ProfilePictureComponent/ProfilePictureComponent", () => ({
  default: ({ handleOnClick }: ProfilePictureComponentProps) => (
    <img data-testid="profile-picture" onClick={handleOnClick} />
  ),
}));

vi.mock("../../components/Reusables/HelperFunctions/HelperFunctions", () => ({
  formatDate: () => "2024.01.01",
}));

const mockUser: User = {
  id: 5,
  email: "test@email.com",
  firstName: "Test",
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
  description: "This is a test description",
  cityName: "Budapest",
  reward: 1000,
  statusName: "OPEN",
  createdAt: new Date(),

  publisher: mockUser,
  selectedUser: mockUser,

  jobApplications: [],
  comments: [],
};

describe("PostView", () => {
  it("renders post data", () => {
    render(<PostView post={mockPost} handleOnClick={() => {}} />);

    expect(screen.getByText("Test Post")).toBeInTheDocument();
    expect(screen.getByText("This is a test description")).toBeInTheDocument();
    expect(screen.getByText("1000 Ft")).toBeInTheDocument();
    expect(screen.getByText("test@email.com")).toBeInTheDocument();
    expect(screen.getByText(/Megosztva:/)).toBeInTheDocument();
  });

  it("calls handleOnClick when container is clicked", () => {
    const handleClick = vi.fn();

    render(<PostView post={mockPost} handleOnClick={handleClick} />);

    fireEvent.click(screen.getByText("Test Post"));

    expect(handleClick).toHaveBeenCalled();
  });

  it("navigates to user profile when publisher clicked", () => {
    render(<PostView post={mockPost} handleOnClick={() => {}} />);

    fireEvent.click(screen.getByText("test@email.com"));

    expect(mockNavigate).toHaveBeenCalledWith("/users/5");
  });

  it("truncates description longer than 100 characters", () => {
    const longPost: Post = {
      ...mockPost,
      description: "a".repeat(120),
    };

    render(<PostView post={longPost} handleOnClick={() => {}} />);

    expect(screen.getByText("a".repeat(100) + "...")).toBeInTheDocument();
  });

  it("shows 'Nincs díj!' when reward is 0", () => {
    const postNoReward: Post = {
      ...mockPost,
      reward: 0,
    };

    render(<PostView post={postNoReward} handleOnClick={() => {}} />);

    expect(screen.getByText("Nincs díj!")).toBeInTheDocument();
  });
});