import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import NewPostComponent from "../../components/NewPostComponent/NewPostComponent";

vi.mock("../../components/Header/Header", () => ({
  default: () => <div data-testid="header" />,
}));

vi.mock("../../components/Footer/Footer", () => ({
  default: () => <div data-testid="footer" />,
}));

type InputType = "text" | "number" | "textarea" | "select";

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

vi.mock("../../components/Reusables/InputForm/InputForm", () => ({
  default: ({ formData, handleFunction, inputs, options }: InputFormProps) => (
    <div data-testid="input-form">
      {inputs.map((input) => {
        if (input.type === "textarea") {
          return (
            <label key={input.name}>
              {input.placeholder}
              <textarea
                name={input.name}
                value={formData[input.name]}
                onChange={handleFunction}
              />
            </label>
          );
        }

        if (input.type === "select") {
          return (
            <label key={input.name}>
              {input.placeholder}
              <select
                name={input.name}
                value={formData[input.name]}
                onChange={handleFunction}
              >
                <option value="">--</option>
                {options?.[input.name]?.map((opt) => (
                  <option key={opt.id} value={opt.value}>
                    {opt.label}
                  </option>
                ))}
              </select>
            </label>
          );
        }

        return (
          <label key={input.name}>
            {input.placeholder}
            <input
              type={input.type}
              name={input.name}
              value={formData[input.name]}
              onChange={handleFunction}
            />
          </label>
        );
      })}
    </div>
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
  })
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

    expect(await screen.findByTestId("input-form")).toBeInTheDocument();
  });

  it("fetches cities and updates select options", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    render(<NewPostComponent />);

    await waitFor(() => {
      expect(publicAxios.get).toHaveBeenCalledWith("/api/public/cities");
    });

    expect(await screen.findByText("Budapest")).toBeInTheDocument();
    expect(screen.getByText("Debrecen")).toBeInTheDocument();
  });

  it("handles input change", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    render(<NewPostComponent />);

    const input = await screen.findByLabelText(/A poszt címe/i);

    fireEvent.change(input, { target: { value: "My Test Post" } });

    expect(input).toHaveValue("My Test Post");
  });

  it("submits form successfully", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });
    vi.mocked(privateAxios.post).mockResolvedValueOnce({ data: { id: 1 } });

    render(<NewPostComponent />);

    await screen.findByTestId("input-form");

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

    expect(navigateMock).toHaveBeenCalledWith("/posts/1");
  });

  it("handles API error correctly", async () => {
    vi.mocked(publicAxios.get).mockResolvedValueOnce({ data: mockCities });

    vi.mocked(privateAxios.post).mockRejectedValueOnce({
      response: {
        data: {
          message: "Error occurred",
          errors: { title: "Required" },
        },
      },
    });

    render(<NewPostComponent />);

    await screen.findByTestId("input-form");

    fireEvent.click(
      screen.getByRole("button", { name: /Poszt létrehozása/i })
    );

    expect(await screen.findByText("Error occurred")).toBeInTheDocument();
  });
});