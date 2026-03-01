import { privateAxios, publicAxios } from "../../../config/axiosConfig"
import { useLoading } from "../../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { File } from "../../../types/FileType";
import { useEffect, useState } from "react"
import openSVG from '../../../assets/open-in-new-white.svg';

import "./FileDisplay.css"

interface FileDisplayProps {
  type: string;
  id: number;
  canEdit: boolean | undefined;
}

function FileDisplay({ type, id, canEdit }: FileDisplayProps){
  const [files, setFiles] = useState<File[]>([]);
  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();
  const [filesError, setFilesError] = useState(false);
  const [blur, setBlur] = useState(false);

  function getGetAllEndpoint()
  {
    switch (type)
    {
      case "post":
        return `post/files/all/${id}`;
      case "user":
        return `user/files/public/${id}`;
      default:
        return `-`;
    }
  }

  function getUploadFileEndpoint()
  {
    switch (type)
    {
      case "post":
        return `post/files/upload/${id}`;
      case "user":
        return `user/files/upload`;
      default:
        return `-`;
    }
  }

  function getDeleteFileEndpoint(fileId: number)
  {
    switch (type)
    {
      case "post":
        return `post/files/${fileId}/delete`;
      case "user":
        return `user/files/delete/${fileId}`;
      default:
        return `-`;
    }
  }

  useEffect(() => {
    const fetchFiles = async () => {
      setIsLoading(true);
      try {
        setLoadingMessage("Fájlok lekérése...");

        const response = await publicAxios.get<File[]>(getGetAllEndpoint())
        if (response.data.length > 0)
        {
          setFiles(response.data);
        }
      } catch {
        setFilesError(true);
      }
      finally {
        setIsLoading(false);
      }
    }
    fetchFiles();
  }, []);

  async function deleteFile(fileId: number) {
    try {
      setBlur(true);
      const response = await privateAxios.delete(getDeleteFileEndpoint(fileId));
      console.log(response);

      // Remove the deleted file from the state
      setFiles((prevFiles) => prevFiles.filter((file) => file.fileId !== fileId));
    } catch (error) {
      console.error('Error deleting file:', error);
    }
    finally {
      setBlur(false);
    }
  }

  return (
    <>
      <hr />
      <div className={`file-display-container ${files.length < 1 && "disappear"}`}>
        <div className="file-display-title">
          <h1>Fájlok</h1>
          {canEdit && <button type="button" className="setting-btn">Fájl feltöltése</button>}
        </div>
        <div className={`file-display-list ${blur && "blurred"}`}>
          {files.map((f) => {
            console.log(files);
            console.log(f);
            console.log("mapping id :"+f.fileId);
            return (
              <div className="one-file" key={f.fileId}>
                <a href={f.openUrl} className="file-name" target="_blank" rel="noopener noreferrer">
                  {f.fileName}
                  <img src={openSVG} alt="Megnyitás" />
                </a>
                <div className="file-actions">
                  <a href={f.downloadUrl} className="download-btn">Letöltés</a>
                  {canEdit && <button type="button" className="delete-btn" onClick={() => {deleteFile(f.fileId)}}>Törlés</button>}
                </div>
              </div>
            ); 
          })
        } 
        </div>
      </div>
    </>
  )
}

export default FileDisplay;