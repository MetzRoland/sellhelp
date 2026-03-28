package org.sellhelp.backend.security;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class FileTypeDetector {
    Tika mytika = new Tika();

    public String detectType(MultipartFile file)
    {
        try {
            return mytika.detect(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Fájltípus vizsgálata sikertelen!");
        }
    }
}
