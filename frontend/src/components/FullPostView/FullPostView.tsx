import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router";
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
import UserListItem from "../UserListItem/UserListItem";
import PostActionButton from "../PostActionButton/PostActionButton";
import type { FullPostViewProps } from "./FullPostViewTypes";
import { formatDate } from "../Reusables/HelperFunctions/HelperFunctions";
import FileDisplay from "../Reusables/FileDisplay/FileDisplay";

import "./FullPostView.css";

function FullPostView({ fetchEndpoint = "/post/posts/" }: FullPostViewProps) {
  const { id } = useParams();
  const [post, setPost] = useState<Post | null>(null);
  const [cities, setCities] = useState<City[]>([]);
  const [comment, setComment] = useState<string | null>(null);
  const [applied, setApplied] = useState<boolean>(false);
  const { user, isAuthenticated } = useAuth();

  const { setIsLoading, setLoadingMessage } = useLoading();

  const navigate = useNavigate();

  const postUpdateInputs = [
    { name: "title", type: "text", placeholder: "A poszt címe" },
    { name: "description", type: "textarea", placeholder: "Leírás" },
    { name: "cityName", type: "select", placeholder: "Válasszon települést" },
    { name: "reward", type: "number", placeholder: "Munkadíj" },
  ] as const;

  const [formData, setFormData] = useState<NewPostForm>({
    title: "",
    description: "",
    cityName: "",
    reward: "",
  });

  const isClosed =
    post?.statusName === "closed" ||
    post?.statusName === "unsuccessful_result_closed";

  const [disabledInputsMap, setDisabledInputsMap] = useState<
    Record<string, boolean>
  >(
    postUpdateInputs.reduce(
      (acc, input) => {
        acc[input.name] = true;
        return acc;
      },
      {} as Record<string, boolean>,
    ),
  );

  const settingInputsMap: Record<string, boolean> = postUpdateInputs.reduce(
    (acc, input) => {
      acc[input.name] = true;

      return acc;
    },
    {} as Record<string, boolean>,
  );

  const [newPostError, setNewPostError] = useState("");

  const [validationErrors, setValidationErrors] =
    useState<NewPostValidationErrors>({});

  const isOwner = user && post && post.publisher.id === user.id;
  const isPrivileged =
    user && (user.role === "ROLE_MODERATOR" || user.role === "ROLE_ADMIN");
  const isNormalUser = user && user.role === "ROLE_USER";

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
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
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
        let response;

        if (isAuthenticated) {
          response = await privateAxios.get<Post>(fetchEndpoint + `${id}`);
        } else {
          response = await publicAxios.get<Post>(fetchEndpoint + `${id}`);
        }
        //const response = await privateAxios.get<Post>(fetchEndpoint + `${id}`);

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
  }, [id, setIsLoading, setLoadingMessage, fetchEndpoint, isAuthenticated]);

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

  const deletePostById = async (postId: number, baseEndpoint: string) => {
    setIsLoading(true);
    setLoadingMessage("Poszt törlése...");

    try {
      const response = await privateAxios.delete(baseEndpoint + `${postId}`);
      console.log(response.data);

      navigate(
        !baseEndpoint.includes("superuser") ? "/myposts" : "/postManagement",
      );
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

  const handleCommentInputChange = (
    e: React.ChangeEvent<HTMLTextAreaElement>,
  ) => {
    const { value } = e.target;

    setComment(value);
  };

  const addNewComment = async (postId: number) => {
    setIsLoading(true);
    setLoadingMessage("Komment mentése...");

    try {
      const response = await privateAxios.post(
        `/post/posts/${postId}/comment`,
        { message: comment },
      );

      console.log(response.data);

      const fetchPostResponse = await privateAxios.get<Post>(
        `/post/posts/${id}`,
      );

      console.log(response.data);
      setPost(fetchPostResponse.data);
    } catch {
      setPost(null);
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
      setComment("");
    }
  };

  const selectUserToPost = async (
    e: React.MouseEvent<HTMLButtonElement>,
    applicationId: number,
  ) => {
    e.stopPropagation();

    setIsLoading(true);
    setLoadingMessage("Felhasználó kiválasztása...");

    try {
      const response = await privateAxios.post(
        `post/chooseApplicant/${applicationId}`,
      );

      setPost(response.data);
    } catch {
      setPost(null);
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  const rejectUserApply = async (
    e: React.MouseEvent<HTMLButtonElement>,
    postId: number,
  ) => {
    e.stopPropagation();

    setIsLoading(true);
    setLoadingMessage("Felhasználó visszavonása...");

    try {
      const response = await privateAxios.get(
        `/post/posts/${postId}/rejectApply`,
      );

      const postResponse = await privateAxios.get(`/post/posts/${postId}`);
      setPost(postResponse.data);

      console.log(response.data);
    } catch {
      setPost(null);
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  useEffect(() => {
    async function fetchApplied() {
      if (user?.role === "ROLE_USER") {
        const response = await privateAxios.get(
          `/post/posts/${id}/applied-status`,
        );

        setApplied(response.data);
      }
    }

    fetchApplied();
  }, [applied, id, user?.role]);

  const applyToPost = async (postId: number) => {
    setIsLoading(true);
    setLoadingMessage("Jelentkezés...");

    try {
      const response = await privateAxios.post(`/post/posts/${postId}/apply`);
      console.log(response.data);

      const postResponse = await privateAxios.get(`/post/posts/${postId}`);
      setPost(postResponse.data);

      const appliedStatusRespone = await privateAxios.get(
        `/post/posts/${post?.id}/applied-status`,
      );

      setApplied(appliedStatusRespone.data);
    } catch {
      setPost(null);
    } finally {
      setIsLoading(false);
      setLoadingMessage("");
    }
  };

  const changePostStatus = async (targetStatusName: string, postId: number) => {
    try {
      const response = await privateAxios.patch(
        `/post/${postId}/changeStatus`,
        {
          targetStatusName: targetStatusName,
        },
      );
      console.log(response.data);

      const postResponse = await privateAxios.get(`/post/posts/${postId}`);
      setPost(postResponse.data);
    } catch {
      setPost(null);
    }
  };

  const startWorkingOnPost = async (postId: number) => {
    await changePostStatus("started", postId);
  };

  const postCompletedByEmployee = async (postId: number) => {
    await changePostStatus("completed_by_employee", postId);
  };

  const postClosedUnsucessFully = async (postId: number) => {
    await changePostStatus("unsuccessful_result_closed", postId);
  };

  const workRejectedByEmployer = async (postId: number) => {
    await changePostStatus("work_rejected", postId);
  };

  const postClosed = async (postId: number) => {
    await changePostStatus("closed", postId);
  };

  const rejectApply = async (postId: number) => {
    try {
      const response = await privateAxios.get(
        `/post/posts/${postId}/rejectApply`,
      );
      console.log(response.data);

      const postResponse = await privateAxios.get(`/post/posts/${postId}`);
      setPost(postResponse.data);

      setApplied(false);
    } catch {
      setPost(null);
    }
  };

  const cancelApplication = async (postId: number) => {
    try {
      const response = await privateAxios.post(
        `/post/posts/${postId}/cancelApply`,
      );
      console.log(response.data);

      const postResponse = await privateAxios.get(`/post/posts/${postId}`);
      setPost(postResponse.data);

      setApplied(false);
    } catch {
      setPost(null);
    }
  };

  if (post === null) {
    return <PageNotFound message="A poszt nem található!" />;
  }

  if (
    (isAuthenticated &&
      user &&
      post.publisher.id !== user.id &&
      post.statusName !== "new" &&
      !applied && isNormalUser) ||
    (!isAuthenticated && !user && post.statusName !== "new")
  ) {
    return <PageNotFound message="A poszt már nem elérhető!" />;
  }

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="container-title">{post.title}</h1>

        <div className="content-container full-post-view-container">
          <div className="post-details">
            <p className="post-date">
              Megosztva: {formatDate(post.createdAt.toString())}
            </p>

            {isOwner ? (
              <>
                {!isClosed ? (
                  <InputForm<NewPostForm>
                    inputs={postUpdateInputs}
                    formData={formData}
                    handleFunction={handleInputChange}
                    errorMessage={validationErrors}
                    options={{ cityName: cityOptions }}
                    disabledInputsMap={disabledInputsMap}
                    settingInputsMap={settingInputsMap}
                    disabledToggle={toggleDisabled}
                  />
                ) : (
                  <InputForm<NewPostForm>
                    inputs={postUpdateInputs}
                    formData={formData}
                    handleFunction={handleInputChange}
                    options={{ cityName: cityOptions }}
                    disabledInputsMap={disabledInputsMap}
                  />
                )}

                <button
                  type="button"
                  className="btn btn-danger"
                  onClick={() => deletePostById(post.id, "/post/delete/")}
                  disabled={isClosed}
                >
                  Poszt törlése
                </button>
              </>
            ) : (
              <InputForm<NewPostForm>
                inputs={postUpdateInputs}
                formData={formData}
                handleFunction={handleInputChange}
                options={{ cityName: cityOptions }}
                disabledInputsMap={disabledInputsMap}
              />
            )}

            <FileDisplay
              type="post"
              id={post.id}
              canEdit={(isOwner && !isClosed) || false}
            />

            {((isNormalUser && !isOwner) || !isAuthenticated || isPrivileged) && (
              <>
                <h2>Poszt létrehozó:</h2>

                <UserListItem
                  key={post.publisher.id}
                  userId={post.publisher.id}
                  email={post.publisher.email}
                  date={post.publisher.createdAt.toString()}
                />
              </>
            )}

            <h2>Kommentek:</h2>

            {isNormalUser && (
              <div className="new-comment-div">
                <textarea
                  className="input-element textarea-element"
                  placeholder="Kommentelj valamit..."
                  value={comment ?? ""}
                  onChange={handleCommentInputChange}
                  disabled={isClosed}
                ></textarea>

                <button
                  type="button"
                  className="setting-btn"
                  onClick={() => addNewComment(post.id)}
                  disabled={isClosed}
                >
                  Küldés
                </button>
              </div>
            )}

            <div className="user-list-container">
              {post.comments.length === 0 && <p>Nincsenek kommentek!</p>}

              {[...post.comments]
                .sort(
                  (a, b) =>
                    new Date(b.createdAt).getTime() -
                    new Date(a.createdAt).getTime(),
                )
                .map((comment) => (
                  <UserListItem
                    key={comment.id}
                    userId={comment.publisher.id}
                    email={comment.publisher.email}
                    date={new Date(comment.createdAt).toString()}
                    message={comment.message}
                    highlightLabel={
                      comment.publisher.id === post.publisher.id
                        ? "<<Létrehozó>>"
                        : undefined
                    }
                  />
                ))}
            </div>

            {isPrivileged && (
              <button
                type="button"
                className="btn"
                onClick={() =>
                  deletePostById(post.id, `/superuser/posts/delete/`)
                }
              >
                Admin törlés
              </button>
            )}

            {(isOwner || isPrivileged) && (
              <>
                <h2>Jelentkezések:</h2>

                <div className="user-list-container">
                  {post.jobApplications.length === 0 && (
                    <p>Nincsenek Jelentkezők!</p>
                  )}

                  {[...post.jobApplications]
                    .sort(
                      (a, b) =>
                        new Date(b.appliedAt).getTime() -
                        new Date(a.appliedAt).getTime(),
                    )
                    .map((jobApplication) => (
                      <UserListItem
                        key={jobApplication.id}
                        userId={jobApplication.applicant.id}
                        email={jobApplication.applicant.email}
                        date={new Date(
                          jobApplication.appliedAt,
                        ).toLocaleString()}
                        actionText="Kiválaszt"
                        btnDisabled={(post.selectedUser && user.role === "ROLE_USER") || user.role !== "ROLE_USER"}
                        onActionClick={(e) =>
                          selectUserToPost(e, jobApplication.id)
                        }
                      />
                    ))}
                </div>

                <h2>Munkavállaló:</h2>

                {post.selectedUser === null ? (
                  <p>Nincs még senki kiválasztva!</p>
                ) : (
                  <UserListItem
                    userId={post.selectedUser.id}
                    email={post.selectedUser.email}
                    date={post.jobApplications
                      .find((a) => a.applicant.id === post.selectedUser?.id)
                      ?.appliedAt.toString()}
                    actionText="Visszavonás"
                    onActionClick={(e) => rejectUserApply(e, post.id)}
                    btnDisabled={isClosed || user.role !== "ROLE_USER"}
                  />
                )}
              </>
            )}

            {isNormalUser && (
              <PostActionButton
                post={post}
                applied={applied}
                applyToPost={applyToPost}
                cancelApplication={cancelApplication}
                startWorkingOnPost={startWorkingOnPost}
                rejectApply={rejectApply}
                postCompletedByEmployee={postCompletedByEmployee}
                postClosedUnsucessFully={postClosedUnsucessFully}
                workRejectedByEmployer={workRejectedByEmployer}
                postClosed={postClosed}
              />
            )}
          </div>

          <p>{post.statusName}</p>

          {newPostError && (
            <p className="message error error-process-status">{newPostError}</p>
          )}

          {!isAuthenticated && (
            <Link className="btn btn-highlight" to="/login">
              Jelentkezéshez jelentkezz be
            </Link>
          )}
        </div>
      </div>

      <Footer />
    </>
  );
}

export default FullPostView;
