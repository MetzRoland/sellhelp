import type { FormFields } from "../genericTypes/FormFields";

export interface FullPostViewProps {
    fetchEndpoint?: string;
}

export interface PostCommentFields{
    message?: string;
}

export type PostComment = FormFields<PostCommentFields>;

export interface PostCommentValidationErrors{
    message?: string;
}