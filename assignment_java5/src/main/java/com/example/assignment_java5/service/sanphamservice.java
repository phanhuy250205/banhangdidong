package com.example.assignment_java5.service;

import com.example.assignment_java5.model.sanpham;

import java.util.List;
import java.util.Optional;

public interface sanphamservice {
    List<sanpham> getallSanpham();  // Lấy tất cả sản phẩm
    Optional<sanpham> getSanPhamById(Long id);  // Lấy sản phẩm theo ID
    sanpham createSanPham(sanpham sanPham);  // Tạo sản phẩm mới
    sanpham updateSanPham(Long id, sanpham sanPham);  // Cập nhật sản phẩm
    void deleteSanPhamById(Long id);  // Xóa sản phẩm theo ID

    // Phương thức tìm kiếm sản phẩm
    List<sanpham> searchSanPham(String searchTerm);  // Tìm kiếm sản phẩm theo tên hoặc mô tả
}
