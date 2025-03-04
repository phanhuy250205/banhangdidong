package com.example.assignment_java5.controller;

import com.example.assignment_java5.Dto.nhanviendto;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.repository.nhanvienrepository;
import com.example.assignment_java5.repository.phanloaichucvurepository;
import com.example.assignment_java5.service.FileUploadService;
import com.example.assignment_java5.service.Userservice;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private Userservice userservice;
    @Autowired
    private nhanvienrepository nhanviendrepository;
    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    phanloaichucvurepository phanloaichucvurepository;

    // ✅ Trang đăng ký
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("nhanviendto", new nhanviendto());
        model.addAttribute("roleList", phanloaichucvurepository.findAll()); // 🟢 Lấy tất cả roles
        return "signup";
    }


    // ✅ Trang đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "login";
    }





    // ✅ Xử lý đăng ký người dùng
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("nhanviendto") nhanviendto nhanviendto, Model model) {
        if (!nhanviendto.getPasswold().equals(nhanviendto.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu và xác nhận mật khẩu không khớp");
            return "signup";
        }

        if (!nhanviendto.isTermsAccepted()) {
            model.addAttribute("error", "Bạn phải đồng ý với điều khoản");
            return "signup";
        }

        nhanvien newuser = userservice.register(nhanviendto);
        return "redirect:/user/login";
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        Object currentUserObject = session.getAttribute("currentUser");

        if (currentUserObject == null) {
            return "redirect:/user/login"; // Nếu chưa đăng nhập, chuyển hướng đến login
        }

        nhanvien currentUser;
        if (currentUserObject instanceof nhanviendto) {
            // Nếu session đang lưu DTO, lấy ID để truy vấn lại dữ liệu gốc
            nhanviendto currentUserDTO = (nhanviendto) currentUserObject;
            currentUser = userservice.getById(currentUserDTO.getId());
        } else if (currentUserObject instanceof nhanvien) {
            // Nếu session đang lưu nhanvien, sử dụng luôn
            currentUser = (nhanvien) currentUserObject;
        } else {
            return "redirect:/user/login"; // Tránh lỗi nếu có kiểu dữ liệu không hợp lệ
        }

        // Debug avatar
        System.out.println("🟢 Avatar lấy từ database sau cập nhật: " + currentUser.getAvatar());

        model.addAttribute("updatedUser", currentUser);
        return "profile";
    }



    // ✅ Xử lý đăng nhập
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        // Lấy DTO người dùng sau khi đăng nhập
        Optional<nhanviendto> userDTO = userservice.login(email, password);

        if (userDTO.isPresent()) {
            nhanviendto userDto = userDTO.get();

            // Tìm entity nhanvien từ database dựa trên DTO
            Optional<nhanvien> userEntity = nhanviendrepository.findById(userDto.getId());

            if (userEntity.isPresent()) {
                nhanvien currentUser = userEntity.get();

                // Lưu thông tin vào session
                session.setAttribute("username", currentUser.getTenNhanVien());
                session.setAttribute("currentUser", currentUser);  // Lưu cả đối tượng người dùng vào session

                // Lưu currentUserId vào session (id của user)
                session.setAttribute("currentUserId", currentUser.getChucVu().getId());

                // Lưu role vào session (lấy role như "admin", "customer"...)
                String userRole = currentUser.getChucVu().getTenChucVu(); // Giả sử bạn có một thuộc tính 'role' trong 'ChucVu'
                session.setAttribute("currentUserRole", userRole); // Lưu role vào session

                return "redirect:/api/index/";  // Redirect đến trang chủ sau khi đăng nhập
            }
        }

        // Nếu thông tin đăng nhập không chính xác, trả về trang login với thông báo lỗi
        model.addAttribute("error", "Thông tin đăng nhập không chính xác");
        return "/Java5/login"; // Trả về trang login
    }





    @PostMapping("/update")
    public String updateUser(
            @ModelAttribute("nhanviendto") nhanviendto nhanviendto,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model, HttpSession session) {

        if (nhanviendto.getId() == null) {
            model.addAttribute("error", "ID không hợp lệ");
            return "profile";
        }

        if (newPassword != null && !newPassword.isEmpty() && !newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp!");
            return "profile";
        }

        try {
            nhanvien updatedUser = userservice.update(nhanviendto, avatarFile, newPassword);

            // 🔹 In ra kiểm tra xem avatar đã cập nhật chưa
            System.out.println("🟢 Avatar lấy từ database sau cập nhật: " + updatedUser.getAvatar());

            // 🔹 Cập nhật session
            session.setAttribute("username", updatedUser.getTenNhanVien());
            session.setAttribute("currentUser", convertToDTO(updatedUser));

            model.addAttribute("success", "Cập nhật thông tin thành công!");
            return "redirect:/user/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "profile";
        }
    }


    // ✅ Chuyển đổi từ `nhanviendto` sang `nhanvien`
    private nhanvien convertToModel(nhanviendto dto) {
        if (dto == null) return null;

        nhanvien nv = new nhanvien();
        nv.setId(dto.getId());
        nv.setTenNhanVien(dto.getTenNhanVien());
        nv.setEmail(dto.getEmail());
        nv.setSoDienThoai(dto.getSoDienThoai());
        nv.setDiaChi(dto.getDiaChi());
        nv.setPasswold(dto.getPasswold()); // Nếu mật khẩu mới không có, giữ nguyên mật khẩu cũ

        return nv;
    }

    // ✅ Chuyển đổi từ `nhanvien` sang `nhanviendto`
    private nhanviendto convertToDTO(nhanvien model) {
        return nhanviendto.builder()
                .id(model.getId())
                .tenNhanVien(model.getTenNhanVien())
                .email(model.getEmail())
                .soDienThoai(model.getSoDienThoai())
                .diaChi(model.getDiaChi())
                .chucVuId(model.getChucVu() != null ? model.getChucVu().getId() : null)
                .passwold(model.getPasswold()) // Giữ nguyên mật khẩu
                .avatar(model.getAvatar()) // Gán avatar từ database
                .build();
    }


    // ✅ Đăng xuất người dùng
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";
    }
}
