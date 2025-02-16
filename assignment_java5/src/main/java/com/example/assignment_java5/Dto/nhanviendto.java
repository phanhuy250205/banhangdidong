package com.example.assignment_java5.Dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nhanviendto {
    private Long id;
    private String tenNhanVien;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private Long chucVuId;
    private LocalDate ngaysinh;
    private String passwold;
    private String confirmPassword; // Mật khẩu xác nhận (không lưu vào database)
    private boolean termsAccepted;

    private MultipartFile avatarFile; // Nhận file upload từ form
    private String avatar; // Lưu đường dẫn vào database (VARCHAR)
}
