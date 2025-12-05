package com.shiksha.integration;

import com.shiksha.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class S3ConfigurationTest {

    @Autowired
    private S3Client s3Client;

    @Test
    void s3Client_ShouldBeConfigured() {
        assertThat(s3Client).isNotNull();
    }

    @Test
    void s3Client_ShouldListBuckets() {
        ListBucketsResponse response = s3Client.listBuckets();
        assertThat(response.buckets()).isNotEmpty();
        assertThat(response.buckets()).anyMatch(bucket ->
            bucket.name().equals("shiksha-lms-dev-files-f8wro1wy"));
    }

    @Test
    void s3Client_ShouldUploadAndRetrieveFile() {
        String testKey = "test/test-file.txt";
        String testContent = "Hello from S3 test";
        String bucketName = "shiksha-lms-dev-files-f8wro1wy";

        // Upload test file
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(testKey)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromString(testContent));

        // Retrieve and verify
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(testKey)
                .build();

        String retrievedContent = s3Client.getObjectAsBytes(getRequest)
                .asString(StandardCharsets.UTF_8);

        assertThat(retrievedContent).isEqualTo(testContent);

        // Cleanup
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(testKey)
                .build();
        s3Client.deleteObject(deleteRequest);
    }
}