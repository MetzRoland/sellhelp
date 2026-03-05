import { describe, it, expect, beforeEach, vi } from "vitest";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router";
import UserList from "../../components/UserList/UserList";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

interface MockUserProfileViewProps {
  userAccount: User;
  handleUserBanning?: (userId: number, isBanned: boolean) => void;
}

interface MockInputFormProps {
  formData: {
    userName?: string;
  };
  handleFunction: (e: { target: { name: string; value: string } }) => void;
}

vi.mock(
  "../../contextProviders/ProccessLoadProvider/ProccessLoadContext",
  () => ({
    useLoading: vi.fn(),
  }),
);

vi.mock("../../components/Header/Header", () => ({
  default: () => <div>Header</div>,
}));

vi.mock("../../components/Footer/Footer", () => ({
  default: () => <div>Footer</div>,
}));

vi.mock("../../components/UserProfileView/UserProfileView", () => ({
  default: ({ userAccount, handleUserBanning }: MockUserProfileViewProps) => (
    <div data-testid="user-profile">
      <span>
        {userAccount.firstName} {userAccount.lastName}
      </span>

      {handleUserBanning && (
        <button
          data-testid={`ban-btn-${userAccount.id}`}
          onClick={() =>
            handleUserBanning(userAccount.id, userAccount.banned)
          }
        >
          {userAccount.banned ? "Unban" : "Ban"}
        </button>
      )}
    </div>
  ),
}));

vi.mock("../../components/Reusables/InputForm/InputForm", () => ({
  default: ({ formData, handleFunction }: MockInputFormProps) => (
    <input
      data-testid="mock-input"
      value={formData.userName ?? ""}
      onChange={(e) =>
        handleFunction({
          target: { name: "userName", value: e.target.value },
        })
      }
    />
  ),
}));

vi.mock("../../config/axiosConfig", () => ({
  privateAxios: {
    get: vi.fn(),
    put: vi.fn(),
  },
  publicAxios: {
    get: vi.fn(),
  },
}));

describe("UserList Component Core Logic", () => {
  const setIsLoading = vi.fn();
  const setLoadingMessage = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();

    vi.mocked(useLoading).mockReturnValue({
      isLoading: false,
      loadingMessage: "",
      setIsLoading,
      setLoadingMessage,
    });

    vi.mocked(privateAxios.get).mockImplementation((url: string) => {
      if (url === "/api/public/roles") {
        return Promise.resolve({
          data: [
            { id: 1, roleName: "ROLE_USER" },
            { id: 2, roleName: "ROLE_MODERATOR" },
          ],
        });
      }

      if (url === "/superuser/users" || url === "/user/users") {
        return Promise.resolve({
          data: [
            {
              id: 1,
              firstName: "John",
              lastName: "Doe",
              email: "john@example.com",
              banned: false,
              role: "ROLE_USER",
              cityName: "Budapest",
            },
            {
              id: 2,
              firstName: "Jane",
              lastName: "Smith",
              email: "jane@example.com",
              banned: true,
              role: "ROLE_MODERATOR",
              cityName: "Pecs",
            },
          ],
        });
      }

      return Promise.resolve({ data: [] });
    });

    vi.mocked(publicAxios.get).mockResolvedValue({
      data: [
        { id: 1, cityName: "Budapest" },
        { id: 2, cityName: "Pecs" },
      ],
    });

    vi.mocked(privateAxios.put).mockResolvedValue({
      data: { banned: true },
    });
  });

  it("renders users and header/footer", async () => {
    render(
      <MemoryRouter>
        <UserList isAdmin />
      </MemoryRouter>,
    );

    const userProfiles = await screen.findAllByTestId("user-profile");

    expect(userProfiles).toHaveLength(2);
    expect(screen.getByText("Header")).toBeInTheDocument();
    expect(screen.getByText("Footer")).toBeInTheDocument();
  });

  it("can ban a user", async () => {
    render(
      <MemoryRouter>
        <UserList isAdmin />
      </MemoryRouter>,
    );

    const banBtn = await screen.findByTestId("ban-btn-1");

    fireEvent.click(banBtn);

    await waitFor(() => {
      expect(privateAxios.put).toHaveBeenCalledWith("/superuser/users/ban/1");
    });
  });

  it("can unban a banned user", async () => {
    render(
      <MemoryRouter>
        <UserList isAdmin />
      </MemoryRouter>,
    );

    const unbanBtn = await screen.findByTestId("ban-btn-2");

    fireEvent.click(unbanBtn);

    await waitFor(() => {
      expect(privateAxios.put).toHaveBeenCalledWith(
        "/superuser/users/unban/2",
      );
    });
  });

  it("filters users by username", async () => {
    render(
      <MemoryRouter>
        <UserList />
      </MemoryRouter>,
    );

    const input = await screen.findByTestId("mock-input");

    fireEvent.change(input, { target: { value: "Jane" } });

    await waitFor(() => {
      const userProfiles = screen.getAllByTestId("user-profile");

      expect(userProfiles).toHaveLength(1);
      expect(userProfiles[0]).toHaveTextContent("Jane Smith");
    });
  });
});