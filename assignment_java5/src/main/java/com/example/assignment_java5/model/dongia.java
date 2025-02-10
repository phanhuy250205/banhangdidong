package com.example.assignment_java5.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "danh_gia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class dongia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "san_pham_id")
    private sanpham sanPham;

    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private khachhang khachHang;

    @Column(nullable = false)
    private String noiDung;

    @Column(nullable = false)
    private int danhGia;

    private LocalDateTime ngayDanhGia = LocalDateTime.now();
}
