package com.example.assignment_java5.service;

import com.example.assignment_java5.model.sanpham;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;

public interface sanphamservice {
    List<sanpham> getallSanpham();  // Lấy tất cả sản phẩm
    Optional<sanpham> getSanPhamById(Long id);  // Lấy sản phẩm theo ID

    // 🟢 Tạo sản phẩm mới kèm theo ảnh
    sanpham createSanPham(sanpham sanPham, List<MultipartFile> files);

    // 🟢 Cập nhật sản phẩm (hỗ trợ cập nhật ảnh)
    sanpham updateSanPham(Long id, sanpham sanPham, List<MultipartFile> files);

    void deleteSanPhamById(Long id);  // Xóa sản phẩm theo ID

    // 🟢 Tìm kiếm sản phẩm theo tên hoặc mô tả
    List<sanpham> searchSanPham(String searchTerm);
}
