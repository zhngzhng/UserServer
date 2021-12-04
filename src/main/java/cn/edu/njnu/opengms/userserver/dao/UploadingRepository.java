package cn.edu.njnu.opengms.userserver.dao;

import cn.edu.njnu.opengms.userserver.entity.Uploading;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UploadingRepository extends MongoRepository<Uploading, String> {
    List<Uploading> findByEmail(String email);
}
