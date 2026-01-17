package org.sellhelp.backend.services;

import org.apache.coyote.BadRequestException;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserFile;
import org.sellhelp.backend.enums.AuthProvider;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.repositories.UserFileRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserFileService {
    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final UserFileRepository userFileRepository;

    @Autowired
    UserFileService(S3Service s3Service, UserRepository userRepository,
                    UserFileRepository userFileRepository) {
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
    }

    public List<FileDTO> getAllUserFiles(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );
        List<UserFile> files = userFileRepository.findByUser(user);

        List<FileDTO> fileDtos = new ArrayList<>();
        try {

            for (UserFile file : files)
            {
                String url = s3Service.getDownloadURL(file.getFilePath());
                fileDtos.add(new FileDTO(file.getId(), url));
            }

        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Nincs ilyen fájl!");
        }
        catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException("A művelet sikertelen!");
        }
        return fileDtos;
    }

    public FileDTO getUserFile(String email, Integer fileId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );
        UserFile file = userFileRepository.findById(fileId).orElseThrow(
                () -> new RuntimeException("A fájl nem található!")
        );

        // check ownership
        if (file.getUser().getId() != user.getId())
        {throw new InvalidPermissionException("Nincs hozzáférés");}

        try {
            return new FileDTO(file.getId(), s3Service.getDownloadURL(file.getFilePath()));
        }
        catch (NoSuchKeyException e) {
            throw new RuntimeException("Nincs ilyen fájl!");
        }
        catch (Exception e) {
            throw new RuntimeException("A fájlt nem sikerült feltölteni!");
        }
    }

    public void addUserFile(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );
        UserFile newFile = UserFile.builder()
                .user(user)
                .filePath(fileKey(user.getId(), file.getOriginalFilename()))
                .build();
        userFileRepository.save(newFile);
        try {
            s3Service.uploadFileWithKey(newFile.getFilePath(), file);
        } catch (IOException e) {
            throw new RuntimeException("A fájlt nem sikerült feltölteni!");
        }
    }

    public void deleteUserFile(String email, Integer fileId) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        UserFile file = userFileRepository.findById(fileId).orElseThrow(
                () -> new RuntimeException("A fájl nem található!")
        );

        // check ownership
        if (file.getUser().getId() != user.getId())
        {throw new InvalidPermissionException("Nincs hozzáférés");}

        try {
            s3Service.deleteFile(file.getFilePath());
            userFileRepository.delete(file);
        }
        catch (NoSuchKeyException e) {
            throw new RuntimeException("Nincs ilyen fájl!");
        }
        catch (Exception e) {
            throw new RuntimeException("A fájlt nem sikerült feltölteni!");
        }
    }

    public String getProfilePicture(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if(user.getAuthProvider().equals(AuthProvider.GOOGLE) && s3Service.getDownloadURL(user.getProfilePicturePath()) == null){
            return user.getProfilePicturePath();
        }

        if (user.getProfilePicturePath() == null)
        { throw new RuntimeException("Nincs profilkép!");}

        return s3Service.getDownloadURL(user.getProfilePicturePath());
    }

    public void setProfilePicture(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        String key = ppKey(user.getId());

        try {
            s3Service.uploadFileWithKey(key, file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        user.setProfilePicturePath(key);
        userRepository.save(user);
    }

    public void deleteProfilePicture(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if (user.getProfilePicturePath() == null)
        { throw new RuntimeException("Nincs profilkép!");}

        String key = ppKey(user.getId());
        s3Service.deleteFile(key);

        user.setProfilePicturePath(null);
        userRepository.save(user);
    }

    private String ppKey(Integer id)
    {return "users/" + id + "/profilePicture";}

    private String fileKey(Integer id, String fileName)
    {return "users/" + id + "/"+fileName;}
}
