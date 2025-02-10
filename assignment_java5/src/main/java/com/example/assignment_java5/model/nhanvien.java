package com.example.assignment_java5.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class nhanvien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tenNhanVien;

    @Column(unique = true, nullable = false)
    private String email;

    private String soDienThoai;
    private String diaChi;

    private String ngayTao;

    private  String passwold;

    private  String avatar;

    @ManyToOne
    @JoinColumn(name = "chuc_vu_id")
    private phanloaichucvu chucVu;
}
