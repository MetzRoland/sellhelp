import React from "react";
import type { Post } from "../PostsListComponent/PostsListComponentTypes";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";

interface PostActionButtonProps {
  post: Post;
  applied: boolean;
  applyToPost: (postId: number) => void;
  cancelApplication: (postId: number) => void;
  startWorkingOnPost: (postId: number) => void;
  rejectApply: (postId: number) => void;
  postCompletedByEmployee: (postId: number) => void;
  postClosedUnsucessFully: (postId: number) => void;
  workRejectedByEmployer: (postId: number) => void;
  postClosed: (postId: number) => void;
}

const PostActionButton: React.FC<PostActionButtonProps> = ({
  post,
  applied,
  applyToPost,
  cancelApplication,
  startWorkingOnPost,
  rejectApply,
  postCompletedByEmployee,
  postClosedUnsucessFully,
  workRejectedByEmployer,
  postClosed,
}) => {
  const { user } = useAuth();

  if (user === null) {
    return;
  }

  const actions = [
    {
      condition:
        !applied && post.statusName === "new" && user.id !== post.publisher.id,
      text: "Jelentkezés",
      onClick: () => applyToPost(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition:
        applied && post.statusName === "new" && user.id !== post.publisher.id,
      text: "Kiválasztás folyamatban",
      onClick: undefined,
      disabled: true,
      highlight: true,
    },
    {
      condition:
        applied && post.statusName === "new" && user.id !== post.publisher.id,
      text: "Jelentkezés visszavonása",
      onClick: () => cancelApplication(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition:
        applied &&
        post.statusName === "accepted" &&
        user.id !== post.publisher.id,
      text: "Feladat elkezdése",
      onClick: () => startWorkingOnPost(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition:
        applied &&
        post.statusName !== "new" &&
        post.statusName !== "closed" &&
        post.statusName !== "unsuccessful_result_closed" &&
        user.id !== post.publisher.id,
      text: "Munka visszamondása",
      onClick: () => rejectApply(post.id),
      disabled: false,
      highlight: false,
    },
    {
      condition:
        applied &&
        post.statusName === "started" &&
        user.id !== post.publisher.id,
      text: "Munka leadása",
      onClick: () => postCompletedByEmployee(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition: post.statusName === "started" && user.id === post.publisher.id,
      text: "Várakozás a feladat leadására",
      onClick: () => undefined,
      disabled: true,
      highlight: true,
    },
    {
      condition:
        applied &&
        post.statusName === "completed_by_employee" &&
        user.id !== post.publisher.id,
      text: "Várakozás a munkáltató válaszára",
      onClick: () => undefined,
      disabled: true,
      highlight: false,
    },
    {
      condition:
        post.statusName === "completed_by_employee" &&
        user.id === post.publisher.id,
      text: "Poszt sikeres lezárása",
      onClick: () => postClosed(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition:
        post.statusName === "completed_by_employee" &&
        user.id === post.publisher.id,
      text: "Munka visszautasítása",
      onClick: () => workRejectedByEmployer(post.id),
      disabled: false,
      highlight: false,
    },
    {
      condition:
        post.statusName === "work_rejected" && user.id === post.publisher.id,
      text: "Poszt sikertelen lezárása",
      onClick: () => postClosedUnsucessFully(post.id),
      disabled: false,
      highlight: false,
    },
    {
      condition:
        post.statusName === "work_rejected" && user.id === post.publisher.id,
      text: "Munka újra kiadása",
      onClick: () => startWorkingOnPost(post.id),
      disabled: false,
      highlight: true,
    },
    {
      condition:
        post.statusName === "work_rejected" && user.id !== post.publisher.id,
      text: "Várakozás a munkáltató válaszára",
      onClick: () => undefined,
      disabled: true,
      highlight: false,
    },
    {
      condition: post.statusName === "closed",
      text:
        user.id !== post.publisher.id
          ? "Poszt sikeresen teljesítve"
          : "Poszt sikeresen elvégezve a munkavallaló által",
      onClick: () => undefined,
      disabled: true,
      highlight: true,
    },
    {
      condition: post.statusName === "unsuccessful_result_closed",
      text:
        user.id === post.publisher.id
          ? "Poszt sikertelenül lezárva"
          : "Poszt sikertelenül lezárva a munkáltató által",
      onClick: () => undefined,
      disabled: true,
      highlight: true,
    },
  ];

  return (
    <>
      {actions
        .filter((a) => a.condition)
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
