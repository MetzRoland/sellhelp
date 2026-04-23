import { useState, useEffect } from "react";
import Header from "../Header/Header";
import Footer from "../Footer/Footer";
import { privateAxios } from "../../config/axiosConfig";
import type { Chat } from "./ChatsListTypes";
import { useLoading } from "../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import UserListItem from "../UserListItem/UserListItem";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { formatDate } from "../Reusables/HelperFunctions/HelperFunctions";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";

import "./ChatsList.css";

function ChatsList() {
  const [chats, setChats] = useState<Chat[]>([]);
  const [guestUsers, setGuestUsers] = useState<User[]>([]);

  const { user } = useAuth();

  const { setLoadingMessage, setIsLoading } = useLoading();

  useEffect(() => {
    const fetchChats = async () => {
      try {
        setIsLoading(true);
        setLoadingMessage("Chatek betöltése...");

        const response = await privateAxios.get("/api/chat/chats");

        setChats(response.data);
      } catch {
        setChats([]);
      } finally {
        setIsLoading(false);
      }
    };

    fetchChats();
  }, [setIsLoading, setLoadingMessage]);

  useEffect(() => {
    const fetchGuestUser = async (chats: Chat[]) => {
      try {
        chats.map(async (chat) => {
          if (user?.id === chat.guestId) {
            const response = await privateAxios.get(
              `/user/users/${chat.hostId}`,
            );

            setGuestUsers((prev) => [...prev, response.data]);
          } else if (user?.id === chat.hostId) {
            const response = await privateAxios.get(
              `/user/users/${chat.guestId}`,
            );

            setGuestUsers((prev) => [...prev, response.data]);
          }
        });
      } catch {
        setGuestUsers([]);
      }
    };

    fetchGuestUser(chats);
  }, [chats, user?.id]);

  return (
    <>
      <Header />

      <div className="main-container">
        <h1 className="content-title">Összes chat</h1>

        <div className="content-container chat-list-container">
          {chats.length === 0 ? (
            <p>Nincsenek chatek!</p>
          ) : (
            <div className="user-list-container">
              {chats.map((chat) => {
                const lastMessage =
                  chat.chatMessages?.[chat.chatMessages.length - 1];

                const otherUser = guestUsers.filter(
                  (guestUser) =>
                    (guestUser.id === chat.guestId &&
                      user?.id !== guestUser.id) ||
                    (guestUser.id === chat.hostId && user?.id !== guestUser.id),
                )[0];

                return (
                  <UserListItem
                    userId={otherUser?.id}
                    email={
                      otherUser
                        ? otherUser?.lastName + " " + otherUser?.firstName
                        : ""
                    }
                    message={lastMessage?.message ?? "Nincs még üzenet!"}
                    date={
                      lastMessage?.sentAt ? formatDate(lastMessage?.sentAt) : ""
                    }
                    onClickNavigationLink={`/chats/${otherUser?.id}`}
                    highlightLabel={
                      lastMessage?.senderId === user?.id ? "Te:" : ""
                    }
                  />
                );
              })}
            </div>
          )}
        </div>
      </div>

      <Footer />
    </>
  );
}

export default ChatsList;
