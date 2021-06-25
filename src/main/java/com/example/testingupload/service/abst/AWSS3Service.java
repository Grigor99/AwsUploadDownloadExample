package com.example.testingupload.service.abst;

import com.example.testingupload.exception.NotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.multipart.MultipartFile;

public interface AWSS3Service {
    @Async("taskExecutorUrgent")
    void uploadFile(MultipartFile multipartFile);

    // @Async annotation ensures that the method is executed in a different background thread
    // but not consume the main thread.
    byte[] downloadFile(String keyName) throws NotFoundException;
}
