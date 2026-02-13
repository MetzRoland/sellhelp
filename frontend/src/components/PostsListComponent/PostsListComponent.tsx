import { useState, useEffect } from "react";
import { useNavigate } from "react-router";
import { privateAxios } from "../../config/axiosConfig";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import PostView from "../PostView/PostView";
import type { Post, PostListProps } from "./PostsListComponentTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";

import "./PostsListComponent.css";

function PostsListComponent({ title, postFetchingEndpoint }: PostListProps) {
  const navigate = useNavigate();

  const { setIsLoading, setLoadingMessage, isLoading, loadingMessage } =
    useLoading();

  const [posts, setPosts] = useState<Post[]>([]);

  useEffect(() => {
    const fetchPostList = async () => {
      setIsLoading(true);
      setLoadingMessage("A posztok betöltése...");

      try {
        const response = await privateAxios.get(postFetchingEndpoint);

        console.log(response.data);
        setPosts(response.data);
      } catch {
        setPosts([]);
      } finally {
        setIsLoading(false);
        setLoadingMessage("");
      }
    };

    fetchPostList();
  }, [setIsLoading, setLoadingMessage, postFetchingEndpoint]);

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="content-title">{title}</h1>

        <div className="content-container posts-list-container">
          {posts.length === 0 && !isLoading && loadingMessage === "" && (
            <p>Nincsenek posztok</p>
          )}

          {posts.map((post) => {
            return (
              <PostView
                key={post.id}
                post={post}
                handleOnClick={() => {
                    navigate(`/posts/${post.id}`);
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
