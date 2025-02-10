package com.example.assignment_java5.service.impl;

import com.example.assignment_java5.Dto.nhanviendto;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.model.phanloaichucvu;
import com.example.assignment_java5.repository.nhanvienrepository;
import com.example.assignment_java5.repository.phanloaichucvurepository;
import com.example.assignment_java5.service.FileUploadService;
import com.example.assignment_java5.service.Userservice;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.management.relation.Role;
import java.io.IOException;
import java.util.Optional;

@Service
class UserserviceImpl implements Userservice {

    @Autowired
    private nhanvienrepository nhanvienRepository;

    @Autowired
    private phanloaichucvurepository phanloaichucvurepository;

    @Autowired
    private FileUploadService fileUploadService;

    @Override
    public nhanvien register(nhanviendto nhanviendto) {
        // Tạo đối tượng nhanvien từ nhanviendto
        nhanvien nhanvien = new nhanvien();
        nhanvien.setTenNhanVien(nhanviendto.getTenNhanVien());
        nhanvien.setEmail(nhanviendto.getEmail());
        nhanvien.setSoDienThoai(nhanviendto.getSoDienThoai());
        nhanvien.setDiaChi(nhanviendto.getDiaChi());
        nhanvien.setPasswold(nhanviendto.getPasswold());  // Lưu mật khẩu không mã hóa

        // Lấy vai trò với id 3 từ bảng phanloaichucvu
        phanloaichucvu role = phanloaichucvurepository.findById(3L)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        String currentDatetime = java.time.LocalDate.now().toString();
        nhanvien.setNgayTao(currentDatetime);
        // Gán vai trò cho nhân viên
        nhanvien.setChucVu(role);

        // Lưu đối tượng nhanvien vào cơ sở dữ liệu
        return nhanvienRepository.save(nhanvien);
    }


    @Override
    public Optional<nhanviendto> login(String email, String password) {
        Optional<nhanvien> nhanvien = nhanvienRepository.findByEmail(email);

        if (nhanvien.isPresent()) {
            // Kiểm tra mật khẩu so với mật khẩu trong cơ sở dữ liệu (không mã hóa)
            if (password.equals(nhanvien.get().getPasswold())) {
                nhanviendto dto = new nhanviendto();
                dto.setId(nhanvien.get().getId());
                dto.setTenNhanVien(nhanvien.get().getTenNhanVien());
                dto.setEmail(nhanvien.get().getEmail());
                dto.setSoDienThoai(nhanvien.get().getSoDienThoai());
                dto.setDiaChi(nhanvien.get().getDiaChi());
                dto.setChucVuId(nhanvien.get().getChucVu().getId());

                return Optional.of(dto);
            }
        }
        return Optional.empty();  // Trả về nếu đăng nhập thất bại
    }

    @Override
    public nhanvien update(nhanviendto nhanviendto, MultipartFile avatar, String newPassword) {
        if (nhanviendto == null) {
            throw new IllegalArgumentException("Thông tin người dùng không hợp lệ");
        }

        Optional<nhanvien> existingNhanvien = nhanvienRepository.findById(nhanviendto.getId());

        if (existingNhanvien.isPresent()) {
            nhanvien nhanvien = existingNhanvien.get();

            // Cập nhật các thông tin cơ bản
            nhanvien.setTenNhanVien(nhanviendto.getTenNhanVien());
            nhanvien.setEmail(nhanviendto.getEmail());
            nhanvien.setSoDienThoai(nhanviendto.getSoDienThoai());
            nhanvien.setDiaChi(nhanviendto.getDiaChi());

            // Cập nhật mật khẩu nếu có
            if (newPassword != null && !newPassword.isEmpty()) {
                // Kiểm tra mật khẩu có khác mật khẩu cũ không nếu cần
                if (!newPassword.equals(nhanvien.getPasswold())) {
                    nhanvien.setPasswold(newPassword);
                }
            }

            // Kiểm tra nếu có file avatar
            if (avatar != null && !avatar.isEmpty()) {
                try {
                    // Gọi phương thức uploadFile để lưu avatar và nhận tên file
                    String avatarFileName = fileUploadService.uploadFile(avatar, "nhanvien");

                    // Lưu tên file avatar vào đối tượng nhanvien
                    nhanvien.setAvatar(avatarFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Lỗi tải lên avatar");
                }
            }

            // Lưu thông tin đã cập nhật vào cơ sở dữ liệu
            return nhanvienRepository.save(nhanvien);
        } else {
            // Nếu không tìm thấy nhân viên, ném exception hoặc trả về thông báo rõ ràng
            throw new EntityNotFoundException("Nhân viên không tồn tại");
        }
    }



}
