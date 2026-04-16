package org.sellhelp.backend.services;

import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.entities.User;
import org.sellhelp.backend.entities.UserFile;
import org.sellhelp.backend.exceptions.InvalidPermissionException;
import org.sellhelp.backend.exceptions.UserNotFoundException;
import org.sellhelp.backend.exceptions.WrongFileTypeException;
import org.sellhelp.backend.repositories.UserFileRepository;
import org.sellhelp.backend.repositories.UserRepository;
import org.sellhelp.backend.security.FileTypeDetector;
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
    private final FileTypeDetector fileTypeDetector;

    @Autowired
    public UserFileService(S3Service s3Service, UserRepository userRepository,
                           UserFileRepository userFileRepository, FileTypeDetector fileTypeDetector) {
        this.s3Service = s3Service;
        this.userRepository = userRepository;
        this.userFileRepository = userFileRepository;
        this.fileTypeDetector = fileTypeDetector;
    }

    public List<FileDTO> getAllUserFiles(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );
        List<UserFile> files = userFileRepository.findByUser(user);

        List<FileDTO> fileDtos = new ArrayList<>();
        try {

            for (UserFile file : files) {
                fileDtos.add(s3Service.createFileDTO(file.getId(), file.getFilePath()));
            }

        }
        catch (NoSuchKeyException e) {
            throw new RuntimeException("Nincs ilyen fájl!");
        }
        catch (Exception e) {
            System.out.println(e.toString());
            throw new RuntimeException("A művelet sikertelen!");
        }

        return fileDtos;
    }

    public List<FileDTO> getAllUserFilesByUserId(Integer userId) {
        List<UserFile> files = userFileRepository.findByUserId(userId);

        List<FileDTO> fileDtos = new ArrayList<>();
        try {

            for (UserFile file : files) {
                fileDtos.add(s3Service.createFileDTO(file.getId(), file.getFilePath()));
            }

        }
        catch (NoSuchKeyException e) {
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


        try {
            return s3Service.createFileDTO(file.getId(), file.getFilePath());
        } catch (NoSuchKeyException e) {
            throw new RuntimeException("Nincs ilyen fájl!");
        }
        catch (Exception e) {
            throw new RuntimeException("A fájlt nem sikerült feltölteni!");
        }
    }

    public FileDTO getUserFileByFileId(Integer fileId) {
        UserFile file = userFileRepository.findById(fileId).orElseThrow(
                () -> new UserNotFoundException("A fájl nem található!")
        );

        try {
            return s3Service.createFileDTO(file.getId(), file.getFilePath());
        } catch (NoSuchKeyException e) {
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

        String key = s3Service.userFileKey(user.getId(), file.getOriginalFilename());
        if (userFileRepository.findByFilePath(key).isPresent())
        {throw new RuntimeException("Ez a fájl már létezik");}

        if (userFileRepository.countByUser(user) >= 10)
        {
            throw new RuntimeException("Maximum 10 fájlt lehet feltölteni.");
        }

        UserFile newFile = UserFile.builder()
                .user(user)
                .filePath(key)
                .build();

        try {
            s3Service.uploadFileWithKey(newFile.getFilePath(), file);
            userFileRepository.save(newFile);
        }
        catch (IOException e) {
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

        if (file.getUser().getId() != user.getId()) {
            throw new InvalidPermissionException("Nincs hozzáférés");
        }

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

    public ProfilePictureDTO getProfilePictureByUser(User user) {
        if (user.getProfilePicturePath() == null) {
            return new ProfilePictureDTO(null);
        }

        return new ProfilePictureDTO(s3Service.getDownloadURL(user.getProfilePicturePath()));
    }

    public ProfilePictureDTO getUserProfilePicture(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("A felhasználó nem található!"));

        return getProfilePictureByUser(user);
    }

    public ProfilePictureDTO getOwnProfilePicture(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        return getProfilePictureByUser(user);
    }

    public void setProfilePicture(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new UserNotFoundException("A felhasználó nem található!")
        );

        if (!fileTypeDetector.detectType(file).startsWith("image/"))
        {throw new WrongFileTypeException("A fájl kép kell legyen!");}

        String key = s3Service.ppKey(user.getId());

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

        if (user.getProfilePicturePath() == null) {
            throw new RuntimeException("Nincs profilkép!");
        }

        String key = s3Service.ppKey(user.getId());
        s3Service.deleteFile(key);

        user.setProfilePicturePath(null);
        userRepository.save(user);
    }
}
