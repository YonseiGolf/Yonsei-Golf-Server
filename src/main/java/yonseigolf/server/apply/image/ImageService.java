package yonseigolf.server.apply.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

@Service
public class ImageService {

    private final S3Client s3Client;
    @Value("${AWS_S3_BUCKET}")
    private String bucketName;

    @Autowired
    public ImageService(S3Client s3Client) {

        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile file, String randomId) {

        StringBuilder sb = new StringBuilder();
        String fileName = file.getOriginalFilename() + randomId;
        String contentType = file.getContentType();

        // Upload file
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key("store-image/" + fileName)
                .acl("public-read")
                .contentDisposition("inline")
                .contentType(contentType)
                .build();

        String url = sb.append("https://")
                .append(bucketName)
                .append(".s3.ap-northeast-2.amazonaws.com/store-image/")
                .append(fileName)
                .toString();

        try {
            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            return url;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to upload file", e);
        }
    }
}
