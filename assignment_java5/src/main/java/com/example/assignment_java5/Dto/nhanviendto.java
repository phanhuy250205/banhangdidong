package com.example.assignment_java5.Dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

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
    private  String passwold;
    private String confirmPassword;  // Mật khẩu xác nhận (không cần lưu vào cơ sở dữ liệu)
    private boolean termsAccepted;
    private MultipartFile avatar;
}
