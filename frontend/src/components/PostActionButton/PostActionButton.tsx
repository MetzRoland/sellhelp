import React from "react";
import type { Post } from "../PostsListComponent/PostsListComponentTypes";

interface PostActionButtonProps {
  post: Post;
  applied: boolean;
  applyToPost: (postId: number) => void;
  cancelApplication: (postId: number) => void;
  startWorkingOnPost: (postId: number) => void;
  rejectApply: (postId: number) => void;
}

const PostActionButton: React.FC<PostActionButtonProps> = ({
  post,
  applied,
  applyToPost,
  cancelApplication,
  startWorkingOnPost,
  rejectApply,
}) => {
  const actions = [
    {
      condition: !applied && post.statusName === "new",
      text: "Jelentkezés",
      onClick: () => applyToPost(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition: applied && post.statusName === "new",
      text: "Kiválasztás folyamatban",
      onClick: undefined,
      disabled: true,
      highlight: true,
    },
    {
      condition: applied && post.statusName === "new",
      text: "Jelentkezés visszavonása",
      onClick: () => cancelApplication(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition: applied && post.statusName === "accepted",
      text: "Feladat elkezdése",
      onClick: () => startWorkingOnPost(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition: applied && (post.statusName === "accepted" || post.statusName === "started"),
      text: "Munka visszamondása",
      onClick: () => rejectApply(post.id),
      disabled: false,
      highlight: false,
    },
  ];

  return (
    <>
      {actions
        .filter(a => a.condition)
        .map((action, idx) => (
          <button
            key={idx}
            type="button"
            className={`btn ${action.highlight ? "btn-highlight" : ""}`}
            disabled={action.disabled}
            onClick={action.onClick}
          >
            {action.text}
          </button>
        ))}
    </>
  );
};

export default PostActionButton;
