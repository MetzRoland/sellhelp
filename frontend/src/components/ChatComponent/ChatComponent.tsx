import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import type { IMessage } from "@stomp/stompjs";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { privateAxios } from "../../config/axiosConfig";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";
import { formatDate } from "../Reusables/HelperFunctions/HelperFunctions";
import UserListItem from "../UserListItem/UserListItem";
import type { ChatMessage } from "./ChatComponentTypes";
import ProfilePictureComponent from "../ProfilePictureComponent/ProfilePictureComponent";

import "./ChatComponent.css";

function ChatComponent() {
  const [chatId, setChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");

  const [guestUser, setGuestUser] = useState<User | null>(null);

  const clientRef = useRef<Client | null>(null);

  const { user } = useAuth();
  const { guestUserId } = useParams();

  const [files, setFiles] = useState<File[]>([]);

  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const user1Id = user?.id;

  useEffect(() => {
    const initChat = async () => {
      const res = await privateAxios.post(
        `/api/chat/get-or-create?&otherUserId=${guestUserId}`,
      );

      const data = res.data;

      setChatId(data.id);

      if (data.chatMessages) {
        setMessages(data.chatMessages);
      }
    };

    initChat();
  }, [guestUserId]);

  useEffect(() => {
    if (!chatId) return;

    const socket = new SockJS("https://api.sellhelp.org/ws-chat");

    const stompClient = new Client({
      webSocketFactory: () => socket,

      onConnect: () => {
        stompClient.subscribe(`/topic/chat/${chatId}`, (msg: IMessage) => {
          const body: ChatMessage = JSON.parse(msg.body);
          setMessages((prev) => [...prev, body]);
        });
      },
    });

    stompClient.activate();
    clientRef.current = stompClient;

    return () => {
      stompClient.deactivate();
    };
  }, [chatId]);

  useEffect(() => {
    const fetchGuestUser = async () => {
      const response = await privateAxios.get(`/user/users/${guestUserId}`);
      setGuestUser(response.data);
    };

    fetchGuestUser();
  }, [guestUserId]);

  useEffect(() => {
    const container = messagesEndRef.current?.parentElement;
    if (!container || messages.length === 0) return;

    const lastMessage = messages[messages.length - 1];

    const isOwnMessage = lastMessage.senderId === user?.id;

    const isNearBottom =
      container.scrollHeight - container.scrollTop - container.clientHeight <
      100;

    if (isNearBottom || isOwnMessage) {
      container.scrollTo({
        top: container.scrollHeight,
        behavior: "smooth",
      });
    }
  }, [messages, user?.id]);

  const sendMessage = async () => {
    if (!chatId || (!input.trim() && files.length === 0)) return;

    if (files.length > 0) {
      const formData = new FormData();

      formData.append("senderId", String(user1Id));
      formData.append("message", input);

      files.forEach((file) => {
        formData.append("files", file);
      });

      for (const pair of formData.entries()) {
        console.log(pair[0], pair[1]);
      }

      const res = await privateAxios.post(
        `/api/chat/${chatId}/message-with-files`,
        formData
      );

      console.log(res);

      const newMessage = res.data;

      setMessages((prev) => [...prev, newMessage]);

      clientRef.current?.publish({
        destination: "/app/chat.send",
        body: JSON.stringify({
          chatId,
          senderId: user1Id,
          message: input,
        }),
      });

      setFiles([]);
      setInput("");
      return;
    }

    clientRef.current?.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        chatId,
        senderId: user1Id,
        message: input,
      }),
    });

    setInput("");
  };

  if (!user || !guestUser) {
    return <div className="message">Betöltés...</div>;
  }

  return (
    <div className="chat-fullscreen">
      <div className="chat-header">
        <ProfilePictureComponent
          additionalSytleClass="profile-picture-skeleton-img-small"
          userId={guestUser.id}
        />
        <h2>
          {guestUser.lastName} {guestUser.firstName}
        </h2>
      </div>

      <div className="chat-messages-container">
        {messages.map((m, i) => {
          const isOwn = user?.id === m.senderId;

          return (
            <UserListItem
              key={i}
              userId={isOwn ? user.id : guestUser.id}
              email={
                isOwn ? "Te" : `${guestUser.lastName} ${guestUser.firstName}`
              }
              message={m.message}
              disableNavigation
              isMyChatMessage={isOwn}
              isChatMessage={true}
              date={formatDate(m.sentAt)}
            />
          );
        })}
        <div ref={messagesEndRef} />
      </div>

      <div className="chat-input-bar">
        <input
          className="input-element"
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Írj üzenetet..."
        />

        <button className="setting-btn" onClick={sendMessage}>
          Küldés
        </button>
      </div>
    </div>
  );
}

export default ChatComponent;
