package org.sellhelp.backend.controllers;

import org.apache.coyote.BadRequestException;
import org.sellhelp.backend.dtos.responses.FileDTO;
import org.sellhelp.backend.dtos.responses.ProfilePictureDTO;
import org.sellhelp.backend.services.UserFileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/user/files")
public class UserFilesController {
    private final UserFileService userFileService;

    UserFilesController(UserFileService userFileService) {
        this.userFileService = userFileService;
    }

    @GetMapping()
    public ResponseEntity<List<FileDTO>> getAllFiles() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        return ResponseEntity.status(HttpStatus.OK).body(userFileService.getAllUserFiles(email));
    }

    @GetMapping("/download/{fileId}")
    public FileDTO getUserFile(@PathVariable Integer fileId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        return userFileService.getUserFile(email, fileId);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> addUserFile(@RequestParam("file") MultipartFile file) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        userFileService.addUserFile(email, file);

        return ResponseEntity.ok("Fájl sikeresen feltöltve.");
    }

    @DeleteMapping("/delete/{fileId}")
    public ResponseEntity<String> deleteUserFile(@PathVariable Integer fileId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        userFileService.deleteUserFile(email, fileId);

        return ResponseEntity.ok("Profilkép törölve!");
    }













    @GetMapping("/pp")
    public ResponseEntity<ProfilePictureDTO> getProfilePicture() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        ProfilePictureDTO profilePictureDTO = userFileService.getOwnProfilePicture(email);

        return ResponseEntity.ok(profilePictureDTO);
    }

    @GetMapping("/users/{userId}/pp")
    public ResponseEntity<ProfilePictureDTO> showUserProfilePicture(@PathVariable Integer userId){
        return ResponseEntity.ok(userFileService.getUserProfilePicture(userId));
    }

    @PostMapping("/pp")
    public ResponseEntity<String> setProfilePicture(@RequestParam("file") MultipartFile file) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();

        // Could be faked by setting type manually, but prevents normal accidents
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A fájl egy kép kell legyen!");
        }

        userFileService.setProfilePicture(email, file);

        return ResponseEntity.ok("Profilkép firssítve");
    }

    @DeleteMapping("/pp")
    public ResponseEntity<String> removeProfilePicture() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = userDetails.getUsername();
        userFileService.deleteProfilePicture(email);

        return ResponseEntity.ok("Profilkép törölve!");
    }
}
