import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { privateAxios, publicAxios } from "../../config/axiosConfig";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import PostView from "../PostView/PostView";
import type { Post, PostListProps } from "./PostsListComponentTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { PostFilter } from "./PostsListComponentTypes";
import type { PostInputField } from "./PostsListComponentTypes";
import InputForm from "../Reusables/InputForm/InputForm";
import type { City } from "../Register/RegisterTypes";

import "./PostsListComponent.css";

const filterDateOptions = [
  { id: 1, value: "0d", label: "Összes időszak" },
  { id: 2, value: "1d", label: "Elmúlt 1 nap" },
  { id: 3, value: "7d", label: "Elmúlt 1 hét" },
  { id: 4, value: "30d", label: "Elmúlt 1 hónap" },
];

function PostsListComponent({
  title,
  postFetchingEndpoint,
  navigateToPostEndpoint = "/posts/",
}: PostListProps) {
  const navigate = useNavigate();

  const { setIsLoading, setLoadingMessage, isLoading, loadingMessage } =
    useLoading();

  const postFilterInputs: PostInputField[] = [
    { name: "postTitle", type: "text", placeholder: "Poszt Cím" },
    { name: "postDescription", type: "text", placeholder: "Poszt leírás" },
    { name: "reward", type: "text", placeholder: "Minimum munka díj" },
    { name: "postDate", type: "select", placeholder: "Megosztás dátuma" },
    { name: "city", type: "select", placeholder: "Válasszon települést...", userTitle: "Település"},
  ] as const;

  const [filterFormData, setFilterFormData] = useState<PostFilter>({
    postTitle: "",
    postDescription: "",
    publisherEmail: "",
    postDate: "",
    reward: "",
  });

  const [posts, setPosts] = useState<Post[]>([]);
  const [cities, setCities] = useState<City[]>([]);

  const [filteredPosts, setFilteredPosts] = useState<Post[]>([]);

  const cityOptions = cities.map((city) => ({
    id: city.id,
    value: city.cityName,
    label: city.cityName,
  }));

  const handleInputUpdate = (
    e: React.ChangeEvent<
      HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement
    >,
  ) => {
    const { name, value } = e.target;

    setFilterFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  useEffect(() => {
    const fetchPostList = async () => {
      setIsLoading(true);
      setLoadingMessage("A posztok betöltése...");

      try {
        const response = await privateAxios.get(postFetchingEndpoint);
        const citiesResponse = await publicAxios.get("/api/public/cities");

        setPosts(response.data);
        setFilteredPosts(response.data);
        setCities(citiesResponse.data);
      } catch {
        setPosts([]);
        setFilteredPosts([]);
        setCities([]);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchPostList();
  }, [setIsLoading, setLoadingMessage, postFetchingEndpoint]);

  useEffect(() => {
    const filtered = posts.filter((post) => {
      const matchesEmail =
        !filterFormData.publisherEmail ||
        post.publisher.email
          .toLowerCase()
          .includes(filterFormData.publisherEmail.toLowerCase());

      const matchesPostTitle =
        !filterFormData.postTitle ||
        post.title
          .toLowerCase()
          .includes(filterFormData.postTitle.toLowerCase());

      const matchesPostDescription =
        !filterFormData.postDescription ||
        post.description
          .toLowerCase()
          .includes(filterFormData.postDescription.toLowerCase());

      const matchesPostReward = post.reward >= Number(filterFormData.reward);

      const matchesCity =
        !filterFormData.city || post.cityName === filterFormData.city;

      let matchesPostDate = true;

      const selectedValue = filterFormData.postDate;

      if (selectedValue && selectedValue !== "0d") {
        const days = parseInt(selectedValue.replace("d", ""), 10);

        const now = new Date();
        const compareDate = new Date(now);
        compareDate.setDate(now.getDate() - days);

        const postDate = new Date(post.createdAt);

        matchesPostDate = postDate >= compareDate;
      }

      return (
        matchesPostTitle &&
        matchesEmail &&
        matchesPostDescription &&
        matchesPostReward &&
        matchesCity &&
        matchesPostDate
      );
    });

    setFilteredPosts(filtered);
  }, [posts, filterFormData]);

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="content-title">{title}</h1>

        <div className="content-container filter-container">
          <p className="message">Szűrési feltételek</p>

          <InputForm<PostFilter>
            inputs={postFilterInputs}
            formData={filterFormData}
            handleFunction={handleInputUpdate}
            options={{ postDate: filterDateOptions, city: cityOptions }}
          />
        </div>

        <div className="posts-list-container">
          {filteredPosts.length === 0 &&
            !isLoading &&
            loadingMessage === "" && <p>Nincsenek posztok</p>}

          {filteredPosts.map((post) => {
            return (
              <PostView
                key={post.id}
                post={post}
                handleOnClick={() => {
                  navigate(navigateToPostEndpoint + `${post.id}`);
                }}
              />
            );
          })}
        </div>
      </div>

      <Footer />
    </>
  );
}

export default PostsListComponent;
