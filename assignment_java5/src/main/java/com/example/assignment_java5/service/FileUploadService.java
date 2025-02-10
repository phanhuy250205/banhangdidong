package com.example.assignment_java5.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileUploadService {

    // Đường dẫn thư mục lưu ảnh
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // Hàm upload file
    public String uploadFile(MultipartFile file, String targetTable) throws IOException {
        // Kiểm tra xem file có rỗng không
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Tạo tên file duy nhất để tránh trùng lặp
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Tạo đường dẫn lưu file
        Path path = Paths.get(UPLOAD_DIR + fileName);

        // Lưu file vào thư mục đã chỉ định
        Files.write(path, file.getBytes());

        // Trả về tên file hoặc đường dẫn
        return fileName;
    }
}
