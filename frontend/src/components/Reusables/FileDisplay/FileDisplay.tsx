import { publicAxios } from "../../../config/axiosConfig"
import { useLoading } from "../../../contextProviders/ProccessLoadProvider/ProccessLoadContext";
import type { File } from "../../../types/FileType";
import { useEffect, useState } from "react"
import openSVG from '../../../assets/open-in-new-white.svg';
import downloadSVG from '../../../assets/download-white.svg';

import "./FileDisplay.css"

interface FileDisplayProps {
  endpoint: string;
}

function FileDisplay({ endpoint }: FileDisplayProps){
  const [files, setFiles] = useState<File[]>([]);
  const { setIsLoading, setLoadingMessage, isLoading } = useLoading();
  const [filesError, setFilesError] = useState(false);

  useEffect(() => {
    const fetchFiles = async () => {
      setIsLoading(true);
      try {
        setLoadingMessage("Fájlok lekérése...");

        const response = await publicAxios.get<File[]>(endpoint)
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
  
  return (
    <>
      <hr />
      <div className={`file-display-container ${files.length < 1 && "disappear"}`}>
        <h1>Fájlok</h1>
        <div className="file-display-list">
          {files.map((f) => {
            return (
              <div className="one-file" key={f.id}>
                <a href={f.openUrl} target="_blank" rel="noopener noreferrer">
                  {f.fileName}
                  <img src={openSVG} alt="Megnyitás" />
                </a>
                {/* <div className="file-actions">
                  <img src={openSVG} alt="Megnyitás" />
                  <img src={downloadSVG} alt="Letöltés" />
                </div> */}
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