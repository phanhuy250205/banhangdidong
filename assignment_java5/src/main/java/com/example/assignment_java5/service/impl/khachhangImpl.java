package com.example.assignment_java5.service.impl;

import com.example.assignment_java5.model.khachhang;
import com.example.assignment_java5.repository.khachhangrepository;
import com.example.assignment_java5.service.khachhangservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class khachhangImpl implements khachhangservice {

    @Autowired
    private khachhangrepository Khachhangrepository;

    @Override
    public List<khachhang> getAllKhachHang() {
        return Khachhangrepository.findAll();
    }

    @Override
    public Optional<khachhang> getKhachHangById(Long id) {
        return Khachhangrepository.findById(Math.toIntExact(id));
    }

    @Override
    public khachhang createKhachHang(khachhang khachHang) {
        return Khachhangrepository.save(khachHang);
    }

    @Override
    public khachhang updateKhachHang(Long id, khachhang khachHang) {
        Optional<khachhang> existingKhachHang = Khachhangrepository.findById(Math.toIntExact(id));
        if (existingKhachHang.isPresent()) {
            khachhang updatedKhachHang = existingKhachHang.get();
            updatedKhachHang.setTenKhachHang(khachHang.getTenKhachHang());
            updatedKhachHang.setEmail(khachHang.getEmail());
            updatedKhachHang.setSoDienThoai(khachHang.getSoDienThoai());
            updatedKhachHang.setDiaChi(khachHang.getDiaChi());
            updatedKhachHang.setAnh(khachHang.getAnh());
            return Khachhangrepository.save(updatedKhachHang);
        }
        return null; // or throw exception
    }

    @Override
    public void deleteKhachHang(Long id) {
        Khachhangrepository.deleteById(Math.toIntExact(id));
    }
}
