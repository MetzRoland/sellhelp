import type { ChatMessage } from "../ChatComponent/ChatComponentTypes";

export interface Chat{
    id: number;
    hostId: number;
    guestId: number;
    chatMessages: ChatMessage[]
}