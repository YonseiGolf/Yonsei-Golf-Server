package yonseigolf.server.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class MinioConfig {
    @Value("${AWS_ACCESS_KEY}")
    private String accessKey;
    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .endpointOverride(URI.create("http://birdiehyun.store:9000")) // MinIO 주소
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKey, secretKey)
            ))
            .region(Region.AP_NORTHEAST_2) // 아무 리전이나 설정
            .build();
    }
}