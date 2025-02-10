package com.example.assignment_java5.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class khachhang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenKhachHang;

    @Column(unique = true, nullable = false)
    private String email;

    private String soDienThoai;
    private String diaChi;
    private String anh;
}
