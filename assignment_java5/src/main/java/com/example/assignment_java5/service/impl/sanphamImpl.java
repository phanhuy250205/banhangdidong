package com.example.assignment_java5.service.impl;

import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.repository.sanphamrepository;
import com.example.assignment_java5.service.sanphamservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class sanphamImpl implements sanphamservice {
   @Autowired
   private sanphamrepository sanPhamrepository;

    @Override
    public List<sanpham> getallSanpham() {
        // Trả về tất cả các sản phẩm từ repository
        return sanPhamrepository.findAll();
    }

    @Override
    public Optional<sanpham> getSanPhamById(Long id) {
        return sanPhamrepository.findById(Math.toIntExact(id));
    }

    @Override
    public sanpham createSanPham(sanpham sanPham) {
        return  sanPhamrepository.save(sanPham);
    }

    @Override
    public sanpham updateSanPham(Long id, sanpham Sanpham) {
        Optional<sanpham> existingSanPham = sanPhamrepository.findById(Math.toIntExact(id));
        if (existingSanPham.isPresent()) {
            sanpham updateSanPham = existingSanPham.get();
            updateSanPham.setTensanpham(Sanpham.getTensanpham());
            updateSanPham.setMota(Sanpham.getMota());
            updateSanPham.setGia(Sanpham.getGia());
            updateSanPham.setSoLuong(Sanpham.getSoLuong());
            updateSanPham.setAnh(Sanpham.getAnh());
            updateSanPham.setThuongHieu(Sanpham.getThuongHieu());
            updateSanPham.setModel(Sanpham.getModel());
            updateSanPham.setCauHinh(Sanpham.getCauHinh());
            updateSanPham.setGiamGia(Sanpham.getGiamGia());

            return  sanPhamrepository.save(updateSanPham);

        }
        return  null;
    }

    @Override
    public void deleteSanPhamById(Long id) {
            sanPhamrepository.deleteById(Math.toIntExact(id));
    }

    @Override
    public List<sanpham> searchSanPham(String searchTerm) {
        return sanPhamrepository.findByTensanphamContainingOrMotaContaining(searchTerm, searchTerm);
    }
}
