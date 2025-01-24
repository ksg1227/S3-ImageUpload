package com.example.s3Test.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.path.report}")
    private String reportDirName;

    private final AmazonS3 amazonS3;

    // 단일 파일 업로드
    public String uploadSingleFile(MultipartFile multipartFile) {
        if(multipartFile == null || multipartFile.isEmpty()) {
            return null;
        }

        String fileName = createFileName(multipartFile.getOriginalFilename());
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try(InputStream inputStream = multipartFile.getInputStream()){
            amazonS3.putObject(new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.");
        }

        return fileName;
    }


    // 다중 파일 업로드
    public List<String> uploadFiles(List<MultipartFile> multipartFiles) {

        if(multipartFiles == null || multipartFiles.isEmpty()) {
            return null;
        }

        List<String> fileNameList = new ArrayList<>();

        // forEach 구문을 활용하여 multipartFiles 리스트를 통해 넘어온 파일들을 순차적으로 fileNameList에 추가
        multipartFiles.forEach(file -> {
            fileNameList.add(uploadSingleFile(file));
        });

        return fileNameList;
    }


    // 중복된 파일명을 식별하기 위한 UUID
    public String createFileName(String fileName) {
        return reportDirName + "/" + UUID.randomUUID().toString().concat(getFileExtension(fileName));
    }

    // "." 의 존재 유무만 판단
    private String getFileExtension(String fileName) {
        try{
            return fileName.substring(fileName.lastIndexOf("."));
        } catch(StringIndexOutOfBoundsException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "잘못된 형식의 파일" + fileName + ") 입니다.");
        }

    }

    // S3에서 파일 삭제
    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(bucket, fileName));
        System.out.println(bucket);
    }

}
