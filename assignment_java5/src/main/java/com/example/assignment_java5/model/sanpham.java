package com.example.assignment_java5.model;


import jakarta.persistence.*;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class sanpham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;

    @Column(name = "ten_san_pham")
    private  String tensanpham;

    @Column(name = "mo_ta")
    private  String mota;

    @Column(nullable = false)
    private BigDecimal gia;

    @Column(name = "so_luong")
    private int soLuong;
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    private String anh;
    private String thuongHieu;
    private String model;
    private String cauHinh;
    private String trangThai = "Còn hàng";
    private BigDecimal giamGia = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "phan_loai_id")  // Chú ý: Cột này phải khớp với cột trong cơ sở dữ liệu
    private phanloaihang phanLoaiHang;  // PhanLoaiHang phải là entity ánh xạ với bảng `phan_loai_hang`


}
