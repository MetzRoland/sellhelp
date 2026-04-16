import { privateAxios, publicAxios } from "../../../config/axiosConfig";
import { AxiosError } from "axios";
import { useLoading } from "../../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { File } from "../../../types/FileType";
import { useEffect, useState } from "react";
import openSVG from "../../../assets/open-in-new-white.svg";
import { useDropzone } from "react-dropzone";

import "./FileDisplay.css";

interface FileDisplayProps {
  type: string;
  id: number;
  canEdit: boolean | undefined;
}

function FileDisplay({ type, id, canEdit }: FileDisplayProps) {
  const [files, setFiles] = useState<File[]>([]);
  const { setIsLoading, setLoadingMessage } = useLoading();
  const [filesError, setFilesError] = useState<string | null>(null);
  const [blur, setBlur] = useState(false);
  const [isUploading, setIsUploading] = useState(false);
  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  function getGetAllEndpoint() {
    switch (type) {
      case "post":
        return `post/files/all/${id}`;
      case "user":
        return `user/files/public/${id}`;
      default:
        return "-";
    }
  }

  function getUploadFileEndpoint() {
    switch (type) {
      case "post":
        return `post/files/upload/${id}`;
      case "user":
        return `user/files/upload`;
      default:
        return "-";
    }
  }

  function getDeleteFileEndpoint(fileId: number) {
    switch (type) {
      case "post":
        return `post/files/${fileId}/delete`;
      case "user":
        return `user/files/delete/${fileId}`;
      default:
        return "-";
    }
  }

  useEffect(() => {
    fetchFiles();
  }, []);

  async function fetchFiles() {
    setIsLoading(true);
    try {
      setLoadingMessage("Fájlok lekérése...");
      const response = await publicAxios.get<File[]>(getGetAllEndpoint());
      if (response.data.length > 0) {
        setFiles(response.data);
      }
    } catch {
      setFiles([]);
    } finally {
      setIsLoading(false);
    }
  };

  async function deleteFile(fileId: number) {
    try {
      setBlur(true);
      const response = await privateAxios.delete(getDeleteFileEndpoint(fileId));
      console.log(response);
      setFiles((prevFiles) => prevFiles.filter((file) => file.fileId !== fileId));
      setFilesError(null);
    } catch (error) {
      console.error("Error deleting file:", error);
      setFilesError("Error deleting file");
    } finally {
      setBlur(false);
    }
  }

  const onDrop = async (acceptedFiles: globalThis.File[]) => {
    if (acceptedFiles.length < 1) {
      return;
    }

    const tooLarge = acceptedFiles.find(file => file.size > MAX_FILE_SIZE);

    if (tooLarge) {
      console.log("FILE TOO LARGE")
      setFilesError("A fájl mérete nem lehet nagyobb mint 10 MB.");
      return;
    }

    try {
      setFilesError(null);
      setIsLoading(true);
      setLoadingMessage("Fájl feltöltése...");

      const formData = new FormData();
      acceptedFiles.forEach((file) => {
        formData.append("file", file);
      });

    
      await privateAxios.post(getUploadFileEndpoint(), formData, {
          headers: {
            "Content-Type": "multipart/form-data",
          },
      });
      fetchFiles();
    }
    catch (err) {
      const error = err as AxiosError<{ message?: string; }>;

      setFilesError(error?.response?.data?.message || "Fájl feltöltés sikertelen.");
    }
    finally {
      setIsUploading(false);
      setIsLoading(false);
    }
  };

  // Use the useDropzone hook for drag-and-drop functionality
  const { getRootProps, getInputProps } = useDropzone({
    onDrop: onDrop,
    multiple: true, // Allow multiple file uploads
  });

  return (
    <>
      <div className={`file-display-container ${(files.length < 1 && !canEdit) && "disappear"}`}>
        <div className="file-display-title">
          <div>
            <h1>Fájlok</h1>
            {canEdit && files.length+"/10"}
          </div>
          {canEdit && (
            <button
              type="button"
              className="setting-btn"
              onClick={() => setIsUploading((prev) => !prev)}
            >
              {isUploading ? "Mégse" : "Fájl feltöltése"}
            </button>
          )}
        </div>

        {isUploading ? (
          <>
          <h3 className="file-message">
            {filesError}
          </h3>
          <div
            className="upload-area"
            {...getRootProps()}
            >
            <input {...getInputProps()} />
            <p>Húzd ide a fájlt, vagy kattints a fájl választásához!</p>
            <p>Max 10 MB</p>
          </div>
          </>
        ) : (
          <div className={`file-display-list ${blur && "blurred"}`}>
            {filesError && (
              <h3 className="file-message">
                {filesError}
              </h3>
            )}
            {files.map((f) => (
              <div className="one-file" key={f.fileId}>
                <a href={f.openUrl} className="" target="_blank" rel="noopener noreferrer">
                  <span className="file-name">{f.fileName}</span>
                  <img src={openSVG} alt="Megnyitás" />
                </a>
                <div className="file-actions">
                  <a href={f.downloadUrl} className="download-btn">
                    Letöltés
                  </a>
                  {canEdit && (
                    <button
                      type="button"
                      className="delete-btn"
                      onClick={() => {
                        deleteFile(f.fileId);
                      }}
                    >
                      Törlés
                    </button>
                  )}
                </div>
              </div>
            ))}
            {files.length < 1 && "Nincsenek fájlok"}
          </div>
        )}
      </div>
    </>
  );
}

export default FileDisplay;