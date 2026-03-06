import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import NewPostComponent from "../../components/NewPostComponent/NewPostComponent";

type InputType = "text" | "number" | "textarea" | "select" | "email" | "password";

type InputField = {
  name: string;
  type: InputType;
  placeholder: string;
};

type OptionsType = Record<
  string,
  { id: string | number; value: string | number; label: string }[]
>;

type InputFormProps = {
  formData: Record<string, string>;
  handleFunction: (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => void;
  inputs: InputField[];
  options?: OptionsType;
};

vi.mock("../../components/Header/Header", () => ({
  default: () => <div data-testid="header" />,
}));
vi.mock("../../components/Footer/Footer", () => ({
  default: () => <div data-testid="footer" />,
}));

vi.mock("../../components/Reusables/InputForm/InputForm", () => ({
  default: ({ formData, handleFunction, inputs, options }: InputFormProps) => (
    <form data-testid="input-form">
      {inputs.map((input) => {
        const labelText = input.placeholder;
        if (input.type === "textarea") {
          return (
            <label key={input.name}>
              {labelText}
              <textarea
                name={input.name}
                value={formData[input.name]}
                onChange={handleFunction}
              />
            </label>
          );
        } else if (input.type === "select") {
          return (
            <label key={input.name}>
              {labelText}
              <select
                name={input.name}
                value={formData[input.name]}
                onChange={handleFunction}
              >
                {options?.[input.name]?.map((opt) => (
                  <option key={opt.id} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
          );
        } else {
          return (
            <label key={input.name}>
              {labelText}
              <input
                type={input.type}
                name={input.name}
                value={formData[input.name]}
                onChange={handleFunction}
              />
            </label>
          );
        }
      })}
    </form>
  ),
}));

const navigateMock = vi.fn();

vi.mock("react-router", () => ({
  useNavigate: () => navigateMock,
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

vi.mock("../../config/axiosConfig", () => ({
  privateAxios: { post: vi.fn() },
  publicAxios: { get: vi.fn() },
}));

import { privateAxios, publicAxios } from "../../config/axiosConfig";

const mockCities = [
  { id: 1, cityName: "Budapest" },
  { id: 2, cityName: "Debrecen" },
];

describe("NewPostComponent", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders header, footer, and input form", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    render(<NewPostComponent />);

    expect(screen.getByTestId("header")).toBeInTheDocument();
    expect(screen.getByTestId("footer")).toBeInTheDocument();

    const inputForm = await screen.findByTestId("input-form");
    expect(inputForm).toBeInTheDocument();
  });

  it("fetches cities and updates input options", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    render(<NewPostComponent />);

    await waitFor(() => {
      expect(publicAxios.get).toHaveBeenCalledWith("/api/public/cities");
    });

    const inputForm = await screen.findByTestId("input-form");

    expect(inputForm.textContent).toContain("Budapest");
    expect(inputForm.textContent).toContain("Debrecen");
  });

  it("handles input change", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    render(<NewPostComponent />);

    const titleInput = await screen.findByRole("textbox", { name: /A poszt címe/i });

    fireEvent.change(titleInput, { target: { value: "My Test Post" } });

    expect(titleInput).toHaveValue("My Test Post");
  });

  it("submits form successfully", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });
    vi.mocked(privateAxios.post).mockResolvedValueOnce({ data: { id: 1 } });

    render(<NewPostComponent />);

    const button = screen.getByRole("button", {
      name: /Poszt létrehozása/i,
    });

    fireEvent.click(button);

    await waitFor(() => {
      expect(privateAxios.post).toHaveBeenCalledWith("/post/new", {
        title: "",
        description: "",
        cityName: "",
        reward: "",
      });
    });

    expect(navigateMock).toHaveBeenCalledWith("/myposts");
  });

  it("handles API error correctly", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    vi.mocked(privateAxios.post).mockRejectedValueOnce({
      response: { data: { message: "Error occurred", errors: { title: "Required" } } },
    });

    render(<NewPostComponent />);

    const button = screen.getByRole("button", {
      name: /Poszt létrehozása/i,
    });
    fireEvent.click(button);

    const errorMessage = await screen.findByText("Error occurred");
    expect(errorMessage).toBeInTheDocument();
  });
});