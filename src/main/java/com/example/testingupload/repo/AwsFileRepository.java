package com.example.testingupload.repo;

import com.example.testingupload.model.AwsFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwsFileRepository extends CrudRepository<AwsFile,Long> {
    AwsFile findByFileKey(String key);
}
