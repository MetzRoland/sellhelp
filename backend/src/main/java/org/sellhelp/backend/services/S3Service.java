package org.sellhelp.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.IOException;
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
        if (file.isEmpty())
        {throw new IllegalArgumentException("File is empty");}

        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build(),
                RequestBody.fromBytes(file.getBytes())
        );
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
            throw new RuntimeException("Key not found: " + objectKey, e);
        } catch (S3Exception e) {
            throw new RuntimeException("Error downloading file: " + objectKey, e);
        }
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