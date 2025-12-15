package org.sellhelp.backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${s3.accessKeyId}")
    private String s3AccessKeyId;

    @Value("${s3.secretKey}")
    private String s3SecretKey;

    @Value("${s3.region}")
    private String s3Region;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Value("${s3.aws}")
    private boolean isAWS;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsBasicCredentials =
                AwsBasicCredentials.create(s3AccessKeyId, s3SecretKey);

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(s3Region))
                .credentialsProvider(
                        StaticCredentialsProvider.create(awsBasicCredentials))
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(!isAWS) // MinIO only
                                .build()
                );

        if (!isAWS) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }
}
