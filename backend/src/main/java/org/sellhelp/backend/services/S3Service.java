package org.sellhelp.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    @Autowired
    public S3Service(S3Client s3Client, S3Presigner s3Presigner,
                     @Value("${s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;

        if (!bucketExists(bucketName))
        {
            s3Client.createBucket(request -> request.bucket(bucketName));
        }

    }

    private boolean fileExists(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();
            HeadObjectResponse response = s3Client.headObject(headObjectRequest);

            return response != null;
        }
        catch (NoSuchKeyException e) {
            return false;
        }
        catch (S3Exception e) {
            throw new RuntimeException("Error checking file: " + fileName + ". " + e.awsErrorDetails().errorMessage(), e);
        }
    }

    public void uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty())
        {throw new IllegalArgumentException("File is empty");}

        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getOriginalFilename())
                .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    public void uploadFileWithKey(String key, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    public String uploadFileFromUrl(String imageUrl, Integer userId) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Failed to download image, HTTP status: " + status);
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("URL does not point to an image");
            }

            try (InputStream inputStream = connection.getInputStream()) {

                String objectKey = ppKey(userId);

                PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(objectKey)
                        .contentType(contentType)
                        .build();

                s3Client.putObject(
                        putObjectRequest,
                        RequestBody.fromInputStream(inputStream, connection.getContentLengthLong())
                );

                return objectKey;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload profile picture from URL", e);
        }
    }


    public byte[] downloadFile(String fileName) {
        try {
            return s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build()
            ).asByteArray();

        } catch (NoSuchKeyException e) {
            throw new RuntimeException("File not found: " + fileName, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error downloading file: " + fileName, e);
        }
    }

    public void deleteFile(String fileName){
        if(!fileExists(fileName)){
            throw new RuntimeException("File not found: " + fileName);
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }

    boolean bucketExists(String bucketName) {
        try {
            s3Client.headBucket(request -> request.bucket(bucketName));
            return true;
        }
        catch (NoSuchBucketException exception) {
            return false;
        }
    }

    public String getDownloadURL(String objectKey) {
        try {
            String fileName = Paths.get(objectKey).getFileName().toString();

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(getObjectRequest)
                    .signatureDuration(Duration.ofMinutes(15)) // URL valid for 15 minutes
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();

        } catch (NoSuchKeyException e) {
            return null;
        } catch (S3Exception e) {
            throw new RuntimeException("Error downloading file: " + objectKey, e);
        }
    }

    public String userFileKey(Integer userId, String fileName) {
        return "users/" + userId + "/" + fileName;
    }

    public String ppKey(Integer userId) {
        return "users/" + userId + "/pp";
    }

    public String postFileKey(Integer postId, String fileName) {
        return "posts/" + postId + "/" + fileName;
    }

    public String reviewFileKey(Integer reviewId, String fileName) {
        return "reviews/" + reviewId + "/" + fileName;
    }

//    not used
//    public String generateUploadUrl(String objectKey) {
//        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                .bucket(bucketName)
//                .key(objectKey)
//                .build();
//
//        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
//                .putObjectRequest(putObjectRequest)
//                .signatureDuration(Duration.ofMinutes(15)) // URL valid for 15 min
//                .build();
//
//        return s3Presigner.presignPutObject(presignRequest).url().toString();
//    }
}