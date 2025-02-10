package com.example.assignment_java5.Dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class khachhangdto {
    private Long id;
    private String tenKhachHang;
    private String email;
    private String soDienThoai;
    private String diaChi;
    private String anh;
}
