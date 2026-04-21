import { useEffect, useRef, useState } from "react";
import { useParams } from "react-router";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";
import type { IMessage } from "@stomp/stompjs";
import { useAuth } from "../../contextProviders/AuthProvider/AuthContext";
import { privateAxios } from "../../config/axiosConfig";
import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

type ChatMessage = {
  chatId: number;
  senderId: number;
  message: string;
};

const ChatComponent = () => {
  const [chatId, setChatId] = useState<number | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState("");

  const [guestUser, setGuestUser] = useState<User | null>(null);

  const clientRef = useRef<Client | null>(null);

  const { user } = useAuth();

  const { guestUserId } = useParams();

  const user1Id = user?.id;

  useEffect(() => {
    const initChat = async () => {
      const res = await privateAxios.post(
        `/api/chat/get-or-create?&otherUserId=${guestUserId}`
      );

      const data = res.data;
      console.log("Chat created:", data);

      setChatId(data.id);
      console.log(data);

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
      debug: (str) => console.log(str),

      onConnect: () => {
        console.log("Connected to WS");

        stompClient.subscribe(`/topic/chat/${chatId}`, (msg: IMessage) => {
          const body: ChatMessage = JSON.parse(msg.body);
          console.log(body);

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
    }

    fetchGuestUser();
  }, [guestUserId]);

  const sendMessage = () => {
    if (!clientRef.current || !input.trim() || !chatId) return;

    clientRef.current.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({
        chatId,
        senderId: user1Id,
        message: input,
      }),
    });

    setInput("");
  };

  return (
    <div style={{ padding: 20 }}>
      <h2>Chat Test</h2>

      <div
        style={{
          border: "1px solid gray",
          height: 300,
          overflowY: "scroll",
          padding: 10,
          marginBottom: 10,
        }}
      >
        {messages.map((m, i) => (
          <div key={i}>
            {user?.id === m.senderId ? (
              <b>Te: {m.message}</b>
            ) : (
              <b>{guestUser?.lastName + " " + guestUser?.firstName}: {m.message}</b>
            )}
          </div>
        ))}
      </div>

      <input
        value={input}
        onChange={(e) => setInput(e.target.value)}
        placeholder="Type message..."
        style={{ width: "70%", marginRight: 10 }}
      />

      <button onClick={sendMessage}>Send</button>
    </div>
  );
};

export default ChatComponent;