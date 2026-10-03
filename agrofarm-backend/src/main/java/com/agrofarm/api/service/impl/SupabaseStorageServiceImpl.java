package com.agrofarm.api.service.impl;

import com.agrofarm.api.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class SupabaseStorageServiceImpl implements StorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket.name}")
    private String bucketName;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        try {
            // Generate a unique file name: e.g., avatars/abc-123.png
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar.jpg");
            String extension = "";
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
            String fileName = UUID.randomUUID() + extension;

            // Supabase upload endpoint: POST /storage/v1/object/{bucket}/{wildcard}
            String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucketName, fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            headers.set("apikey", serviceKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
            ));

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(file.getBytes(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to upload image to Supabase Storage: " + response.getBody());
            }

            // Return the public URL
            // Format: https://<project>.supabase.co/storage/v1/object/public/<bucket>/<fileName>
            return String.format("%s/storage/v1/object/public/%s/%s", supabaseUrl, bucketName, fileName);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read image bytes", e);
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            // Extract the filename from the public URL
            // Public URL: .../storage/v1/object/public/{bucket}/{fileName}
            String marker = String.format("/storage/v1/object/public/%s/", bucketName);
            int index = fileUrl.indexOf(marker);
            if (index == -1) {
                return; // Not a matching URL from this bucket
            }
            String fileName = fileUrl.substring(index + marker.length());

            // Supabase delete endpoint: DELETE /storage/v1/object/{bucket}
            // Payload: { "prefixes": ["fileName"] }
            String deleteUrl = String.format("%s/storage/v1/object/%s", supabaseUrl, bucketName);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(serviceKey);
            headers.set("apikey", serviceKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Supabase expects a JSON body with a prefixes list
            DeletePayload payload = new DeletePayload(Collections.singletonList(fileName));
            HttpEntity<DeletePayload> requestEntity = new HttpEntity<>(payload, headers);

            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);

        } catch (Exception e) {
            // Log warning, but don't fail the whole user operation if cleanup fails
            System.err.println("Warning: Failed to delete image from Supabase: " + e.getMessage());
        }
    }

    // Record or static class for Supabase batch delete payload
    private record DeletePayload(List<String> prefixes) {}
}