package com.example.s3Test.controller;

import com.example.s3Test.service.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
public class AmazonS3Controller {

    private final AwsS3Service awsS3Service;

    @PostMapping
    public ResponseEntity<List<String>> uploadFiles(@RequestPart(value = "images", required = false) List<MultipartFile> multipartFiles) {
        return ResponseEntity.ok(awsS3Service.uploadFiles(multipartFiles));
    }

    @DeleteMapping
    public ResponseEntity<String> deleteFile(@RequestParam String fileName) {
        awsS3Service.deleteFile(fileName);
        return ResponseEntity.ok(fileName);
    }
}
