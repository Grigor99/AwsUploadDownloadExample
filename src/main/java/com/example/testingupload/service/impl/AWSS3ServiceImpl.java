package com.example.testingupload.service.impl;


import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.example.testingupload.exception.NotFoundException;
import com.example.testingupload.model.AwsFile;
import com.example.testingupload.repo.AwsFileRepository;
import com.example.testingupload.service.abst.AWSS3Service;
import org.apache.http.util.ByteArrayBuffer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AWSS3ServiceImpl implements AWSS3Service {

    @Autowired
    private AwsFileRepository awsFileRepository;

    @Autowired
    private AmazonS3 amazonS3;
    @Value("${aws.s3.bucket.name}")
    private String bucketName;

    @Override
    public void uploadFile(final MultipartFile multipartFile) {
        Runnable runnable = () ->{
            try {
                final File file = convertMultiPartFileToFile(multipartFile);
                uploadFileToS3Bucket(bucketName, file);
                file.delete();
            } catch (final AmazonServiceException ex) {
            }
        };
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(runnable);
        executorService.shutdown();

    }

    private File convertMultiPartFileToFile(final MultipartFile multipartFile) {
        final File file = new File(multipartFile.getOriginalFilename());
        try (final FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        } catch (final IOException ex) {
        }
        return file;
    }

    private void uploadFileToS3Bucket(final String bucketName, final File file) {
        final String uniqueFileName = LocalDateTime.now() + "_" + file.getName();
        AwsFile awsFile = new AwsFile();
        awsFile.setFileKey(uniqueFileName);
        awsFileRepository.save(awsFile);
        final PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, uniqueFileName, file);
        amazonS3.putObject(putObjectRequest);
    }


    @Override
    public byte[] downloadFile(final String keyName) throws NotFoundException {
        byte[] content = null;
        NotFoundException.check(awsFileRepository.findByFileKey(keyName) == null, "content not found");
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, keyName);
        final S3Object s3Object = amazonS3.getObject(getObjectRequest);
        final S3ObjectInputStream stream = s3Object.getObjectContent();
        try {
            content = IOUtils.toByteArray(stream);
            s3Object.close();
        } catch (final IOException ex) {
        }
        return content;
    }

    @Override
    public void deleteFile(String keyName) throws NotFoundException {
        AwsFile awsFile=awsFileRepository.findByFileKey(keyName);
        NotFoundException.check(awsFile==null,"not found item");
        try{
            DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName,keyName);
            amazonS3.deleteObject(deleteObjectRequest);
            awsFileRepository.delete(awsFile);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }
}