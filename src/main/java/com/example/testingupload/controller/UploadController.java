package com.example.testingupload.controller;

import com.example.testingupload.exception.NotFoundException;
import com.example.testingupload.service.abst.AWSS3Service;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/up")
public class UploadController {
    private final AWSS3Service awss3Service;

    public UploadController(AWSS3Service awss3Service) {
        this.awss3Service = awss3Service;
    }

    @PostMapping
    public ResponseEntity<?> upload(@RequestPart MultipartFile multipartFile){
        awss3Service.uploadFile(multipartFile);
        return ResponseEntity.status(200)
                .header("Custom-Header","success")
                .body("done");
    }

    @GetMapping(value= "/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam(value= "fileName") final String keyName) throws NotFoundException {
        final byte[] data = awss3Service.downloadFile(keyName);
        final ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + keyName + "\"")
                .body(resource);
    }
}
