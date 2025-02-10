package com.example.assignment_java5.service;

import com.example.assignment_java5.model.khachhang;

import java.util.List;
import java.util.Optional;

public interface khachhangservice {

    List<khachhang> getAllKhachHang();
    Optional<khachhang> getKhachHangById(Long id);
    khachhang createKhachHang(khachhang khachHang);
    khachhang updateKhachHang(Long id, khachhang khachHang);
    void deleteKhachHang(Long id);
}
