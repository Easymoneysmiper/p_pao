package com.itpk.usercenter.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${aliyun.oss.endpoint}") // OSS endpoint
    private String ossEndpoint;

    @Value("${aliyun.oss.accessKeyId}") // OSS accessKeyId
    private String ossAccessKeyId;

    @Value("${aliyun.oss.accessKeySecret}") // OSS accessKeySecret
    private String ossAccessKeySecret;

    @Value("${aliyun.oss.bucketName}") // OSS bucketName
    private String ossBucketName;

    /**
     * 上传文件到阿里云OSS
     *
     * @param file 上传的文件
     * @return 文件的访问URL
     */
    public String uploadToOss(MultipartFile file) {
        // 1. 检查文件是否为空
        if (file.isEmpty()) {
            throw new RuntimeException("上传文件为空");
        }

        // 2. 生成唯一的文件名
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

        // 3. 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);

        try {
            // 4. 上传文件到OSS
            ossClient.putObject(new PutObjectRequest(ossBucketName, fileName, file.getInputStream()));

            // 5. 返回文件的访问URL
            return "https://" + ossBucketName + "." + ossEndpoint + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        } finally {
            // 6. 关闭OSS客户端
            ossClient.shutdown();
        }
    }
}