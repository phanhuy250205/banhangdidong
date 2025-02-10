package com.example.assignment_java5.service;

import com.example.assignment_java5.Dto.nhanviendto;
import com.example.assignment_java5.model.nhanvien;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface Userservice {
    //Đăng ký người dùng mới
    nhanvien register(nhanviendto nhanviendto);

    //Đăng nhập người dùng
    Optional<nhanviendto> login(String email, String password);

    //Cập nhật thông tin người dùng
    nhanvien update(nhanviendto nhanviendto , MultipartFile avatar , String newPassword);


}
