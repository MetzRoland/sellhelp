import { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import type { Post } from "../PostsListComponent/PostsListComponentTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import PageNotFound from "../PageNotFound/PageNotFound";
import type {
  NewPostForm,
  NewPostValidationErrors,
} from "../NewPostComponent/NewPostComponentTypes";
import InputForm from "../Reusables/InputForm/InputForm";
import type { City } from "../Register/RegisterTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { AxiosError } from "axios";
import { formatDate } from "../Reusables/HelperFunctions/HelperFunctions";

import "./FullPostView.css";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";

function FullPostView() {
  const { id } = useParams();
  const [post, setPost] = useState<Post | null>(null);
  const [cities, setCities] = useState<City[]>([]);
  const [comment, setComment] = useState<string | null>(null);
  const { user } = useAuth();

  const { setIsLoading, setLoadingMessage } = useLoading();

  const navigate = useNavigate();

  const newPostInputs = [
    { name: "title", type: "text", placeholder: "A poszt címe" },
    { name: "description", type: "text", placeholder: "Leírás" },
    { name: "cityName", type: "select", placeholder: "Válasszon települést" },
    { name: "reward", type: "number", placeholder: "Munkadíj" },
  ] as const;

  const [disabledInputsMap, setDisabledInputsMap] = useState<
    Record<string, boolean>
  >(
    newPostInputs.reduce(
      (acc, input) => {
        acc[input.name] = true;
        return acc;
      },
      {} as Record<string, boolean>,
    ),
  );

  const settingInputsMap: Record<string, boolean> = newPostInputs.reduce(
    (acc, input) => {
      acc[input.name] = true;

      return acc;
    },
    {} as Record<string, boolean>,
  );

  const [formData, setFormData] = useState<NewPostForm>({
    title: "",
    description: "",
    cityName: "",
    reward: "",
  });

  const [newPostError, setNewPostError] = useState("");

  const [validationErrors, setValidationErrors] =
    useState<NewPostValidationErrors>({});

  useEffect(() => {
    if (!post) return;

    setFormData({
      title: post?.title,
      description: post?.description,
      reward: String(post?.reward),
      cityName: post?.cityName,
    });
  }, [post]);

  useEffect(() => {
    const fetchCities = async () => {
      try {
        setIsLoading(true);
        const response = await publicAxios.get("/api/public/cities");
        setCities(response.data);
      } catch {
        setCities([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchCities();
  }, [setIsLoading]);

  const cityOptions = cities.map((city) => ({
    id: city.id,
    value: city.cityName,
    label: city.cityName,
  }));

  const getUpdatedField = (
    original: Post,
    updated: Partial<NewPostForm>,
    fieldName: keyof NewPostForm,
  ): Partial<NewPostForm> => {
    const newValue = updated[fieldName];

    if (typeof newValue === "string" && newValue.trim() === "") return {};

    if (newValue === original[fieldName]) {
      return {};
    }

    return { [fieldName]: newValue };
  };

  const handleInputChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setValidationErrors({});
    setNewPostError("");
  };

  useEffect(() => {
    const fetchPostById = async () => {
      setIsLoading(true);
      setLoadingMessage("Poszt betöltése...");

      try {
        const response = await privateAxios.get<Post>(`/post/posts/${id}`);

        console.log(response.data);
        setPost(response.data);
      } catch {
        setPost(null);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchPostById();
  }, [id, setIsLoading, setLoadingMessage]);

  const toggleDisabled = async (inputName: keyof NewPostForm) => {
    const isCurrentlyDisabled = disabledInputsMap[inputName];

    setDisabledInputsMap((prev) => ({
      ...prev,
      [inputName]: !isCurrentlyDisabled,
    }));

    if (!isCurrentlyDisabled && post) {
      const payload = getUpdatedField(post, formData, inputName);

      if (Object.keys(payload).length === 0) {
        setFormData((prev) => ({ ...prev, [inputName]: post[inputName] }));
        return;
      }

      try {
        setIsLoading(true);
        setLoadingMessage("Adatok frissítése...");

        const response = await privateAxios.patch(
          `/post/update/${post.id}`,
          payload,
        );

        if (response.status === 200) {
          //setSuccess(true);
          setPost((prev) => (prev ? ({ ...prev, ...payload } as Post) : prev));
          setNewPostError("");
          setValidationErrors({});
        }
      } catch (err) {
        const error = err as AxiosError<{
          message?: string;
          errors?: NewPostForm;
        }>;

        //setSuccess(false);
        setValidationErrors(error.response?.data?.errors ?? {});
        setNewPostError(
          error.response?.data?.message ?? "Sikertelen frissítés!",
        );
      } finally {
        setIsLoading(false);
      }
    }
  };

  const deletePostById = async () => {
    setIsLoading(true);
    setLoadingMessage("Poszt törlése...");

    try {
      const response = await privateAxios.delete(`/post/delete/${post?.id}`);
      console.log(response.data);

      navigate("/myposts");
    } catch (err) {
      const error = err as AxiosError<{
        message?: string;
        errors?: NewPostForm;
      }>;

      setNewPostError(error.response?.data?.message ?? "Sikertelen törlés!");
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  const handleCommentInputChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    const { value } = e.target;

    setComment(value);
  }

  const addNewComment = async () => {
    setIsLoading(true);
    setLoadingMessage("Komment mentése...");

    try {
      const response = await privateAxios.post(
        `/post/posts/${post?.id}/comment`,
        {message: comment},
      );

      console.log(response.data);

      const fetchPostResponse = await privateAxios.get<Post>(`/post/posts/${id}`);

      console.log(response.data);
      setPost(fetchPostResponse.data);
    } catch {
      console.log("");
    }
    finally{
        setIsLoading(false);
        setLoadingMessage("");
        setComment("");
    }
  };

  if (user === null) {
    return;
  }

  if (post === null) {
    return <PageNotFound message="A poszt nem található!" />;
  }

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="container-title">{post.title}</h1>

        <div className="content-container full-post-view-container">
          <div className="post-details">
            {post.publisher.id === user.id ? (
              <>
                <InputForm<NewPostForm>
                  inputs={newPostInputs}
                  formData={formData}
                  handleFunction={handleInputChange}
                  errorMessage={validationErrors}
                  options={{ cityName: cityOptions }}
                  disabledInputsMap={disabledInputsMap}
                  settingInputsMap={settingInputsMap}
                  disabledToggle={toggleDisabled}
                />

                <button type="button" className="btn" onClick={deletePostById}>
                  Poszt törlése
                </button>
              </>
            ) : (
              <InputForm<NewPostForm>
                inputs={newPostInputs}
                formData={formData}
                handleFunction={handleInputChange}
                options={{ cityName: cityOptions }}
                disabledInputsMap={disabledInputsMap}
              />
            )}

            <h2>Kommentek:</h2>

            <div className="new-comment-div">
              <textarea
                className="input-element textarea-element"
                placeholder="Kommentelj valamit..."
                
                onChange={handleCommentInputChange}
              ></textarea>

              <button
                type="button"
                className="setting-btn"
                onClick={addNewComment}
              >
                Küldés
              </button>
            </div>

            {post.comments.length === 0 && <p>Nincsenek kommentek!</p>}

            {post.comments.map((comment) => {
              return (
                <div key={comment.id}>
                  <p>{comment.message}</p>
                  <p>{formatDate(comment.createdAt.toString())}</p>
                  <ProfilePictureComponent userId={user.id} />
                </div>
              );
            })}
          </div>

          {newPostError && (
            <p className="message error error-process-status">{newPostError}</p>
          )}
        </div>
      </div>

      <Footer />
    </>
  );
}

export default FullPostView;
