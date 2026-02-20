import type { User } from "../../contextProviders/AuthProvider/AuthProviderTypes";

export interface PostListProps{
    title: string;
    postFetchingEndpoint: string;
    navigateToPostEndpoint?: string;
}

export interface JobApplication{
    id: number;
    applicant: User,
    postId: number;
    appliedAt: Date;
}

export interface PostComment{
    id: number;
    message: string;
    publisher: User;
    createdAt: Date;
}

export interface Post{
    id: number;
    title: string;
    description: string;
    cityName: string;
    reward: number;
    statusName: string;
    createdAt: Date;

    publisher: User;
    jobApplications: JobApplication[];
    comments: PostComment[];
    selectedUser: User;
}

export interface PostFilter {
  postTitle?: string;
  postDescription?: string;
  postDate?: string;
  reward?: string;
  publisherEmail?: string;
  city?: string;
}

export interface PostInputField {
  name: keyof PostFilter;
  type: "text" | "select";
  placeholder: string;
}