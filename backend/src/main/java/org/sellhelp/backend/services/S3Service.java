package org.sellhelp.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final String bucketName;

    @Autowired
    public S3Service(S3Client s3Client, @Value("${s3.bucket}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;

        if (!bucketExists(bucketName))
        {
            s3Client.createBucket(request -> request.bucket(bucketName));
        }

    }

    public boolean fileExists(String fileName) {
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
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(file.getOriginalFilename())
                .build(),
                RequestBody.fromBytes(file.getBytes())
        );
    }

    public byte[] downloadFile(String fileName) {
        if(!fileExists(fileName)){
            throw new RuntimeException("File not found: " + fileName);
        }

        ResponseBytes<GetObjectResponse> objectAsBytes = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build()
        );

        return objectAsBytes.asByteArray();
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
}