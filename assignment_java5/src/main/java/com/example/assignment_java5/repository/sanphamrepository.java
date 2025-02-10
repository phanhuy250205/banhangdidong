package com.example.assignment_java5.repository;

import com.example.assignment_java5.model.sanpham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface sanphamrepository extends JpaRepository<sanpham,Integer> {
    // Tìm kiếm sản phẩm dựa trên tên hoặc mô tả, tìm chuỗi con
    List<sanpham> findByTensanphamContainingOrMotaContaining(String tensanpham, String searchTerm);
}
