package com.example.assignment_java5.service.impl;

import com.example.assignment_java5.model.HinhAnhSanPham;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.repository.sanphamrepository;
import com.example.assignment_java5.service.FileUploadService;
import com.example.assignment_java5.service.HinhAnhSanPhamService;
import com.example.assignment_java5.service.sanphamservice;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class sanphamImpl implements sanphamservice {

    @Autowired
    private sanphamrepository sanPhamRepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private HinhAnhSanPhamService hinhAnhSanPhamService;

    @Override
    public List<sanpham> getallSanpham() {
        return sanPhamRepository.findAll();
    }

    @Override
    public Optional<sanpham> getSanPhamById(Long id) {
        return sanPhamRepository.findById(Math.toIntExact(id));
    }


    public sanpham createSanPham(sanpham sanPham, List<MultipartFile> files) {
        // ✅ Lưu sản phẩm trước
        sanpham savedSanPham = sanPhamRepository.save(sanPham);

        // ✅ Nếu có ảnh, lưu vào bảng `hinh_anh_san_pham`
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        // ✅ Lưu ảnh vào thư mục và lấy đường dẫn
                        String imageUrl = fileUploadService.uploadFile(file, "products");

                        // ✅ Tạo đối tượng `HinhAnhSanPham` và lưu vào DB
                        HinhAnhSanPham hinhAnhSanPham = new HinhAnhSanPham();
                        hinhAnhSanPham.setSanPham(savedSanPham);
                        hinhAnhSanPham.setUrlHinhAnh(imageUrl);
                        hinhAnhSanPhamService.saveHinhAnh(hinhAnhSanPham);

                    } catch (IOException e) {
                        throw new RuntimeException("❌ Lỗi khi upload ảnh: " + e.getMessage());
                    }
                }
            }
        }
        return savedSanPham;
    }


    public sanpham updateSanPham(Long id, sanpham sanPham, List<MultipartFile> files) {
        Optional<sanpham> existingSanPham = sanPhamRepository.findById(Math.toIntExact(id));
        if (existingSanPham.isPresent()) {
            sanpham updateSanPham = existingSanPham.get();
            updateSanPham.setTensanpham(sanPham.getTensanpham());
            updateSanPham.setMota(sanPham.getMota());
            updateSanPham.setGia(sanPham.getGia());
            updateSanPham.setSoLuong(sanPham.getSoLuong());
            updateSanPham.setThuongHieu(sanPham.getThuongHieu());
            updateSanPham.setModel(sanPham.getModel());
            updateSanPham.setCauHinh(sanPham.getCauHinh());
            updateSanPham.setGiamGia(sanPham.getGiamGia());

            // ✅ Nếu có ảnh mới, xóa ảnh cũ và lưu ảnh mới
            if (files != null && !files.isEmpty()) {
                // Xóa ảnh cũ
                List<HinhAnhSanPham> oldImages = hinhAnhSanPhamService.getHinhAnhBySanPhamId(id);
                for (HinhAnhSanPham oldImage : oldImages) {
                    fileUploadService.deleteFile(oldImage.getUrlHinhAnh());
                    hinhAnhSanPhamService.deleteHinhAnh(oldImage.getId());
                }

                // Lưu ảnh mới
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        try {
                            String imageUrl = fileUploadService.uploadFile(file, "products");
                            HinhAnhSanPham hinhAnhSanPham = new HinhAnhSanPham();
                            hinhAnhSanPham.setSanPham(updateSanPham);
                            hinhAnhSanPham.setUrlHinhAnh(imageUrl);
                            hinhAnhSanPhamService.saveHinhAnh(hinhAnhSanPham);
                        } catch (IOException e) {
                            throw new RuntimeException("❌ Lỗi khi upload ảnh: " + e.getMessage());
                        }
                    }
                }
            }
            return sanPhamRepository.save(updateSanPham);
        }
        return null;
    }

    @Override
    public void deleteSanPhamById(Long id) {
        Optional<sanpham> optionalSanPham = sanPhamRepository.findById(Math.toIntExact(id));
        if (optionalSanPham.isPresent()) {
            sanpham sanPham = optionalSanPham.get();

            // ✅ Xóa ảnh khỏi thư mục trước khi xóa sản phẩm
            List<HinhAnhSanPham> hinhAnhs = hinhAnhSanPhamService.getHinhAnhBySanPhamId(id);
            for (HinhAnhSanPham hinhAnh : hinhAnhs) {
                fileUploadService.deleteFile(hinhAnh.getUrlHinhAnh());
                hinhAnhSanPhamService.deleteHinhAnh(hinhAnh.getId());
            }

            sanPhamRepository.deleteById(Math.toIntExact(id));
        } else {
            throw new RuntimeException("❌ Không tìm thấy sản phẩm để xóa!");
        }
    }

    @Override
    public List<sanpham> searchSanPham(String searchTerm) {
        return sanPhamRepository.findByTensanphamContainingOrMotaContaining(searchTerm, searchTerm);
    }



}
