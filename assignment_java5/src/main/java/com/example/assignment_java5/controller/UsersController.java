package com.example.assignment_java5.controller;

import com.example.assignment_java5.Dto.nhanviendto;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.service.Userservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession; // Đảm bảo bạn sử dụng đúng import từ jakarta
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private Userservice userservice;

    // Trang đăng ký
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("nhanviendto", new nhanviendto());  // Thêm đối tượng vào model
        return "/Java5/signup";  // Trả về form đăng ký (register.html)
    }


    // Trang đăng nhập
    @GetMapping("/login")
    public String showLoginPage() {
        return "/Java5/login";  // Trả về trang đăng nhập (login.html)
    }

    @GetMapping("/update")
    public String profile(HttpSession session, Model model) {
        nhanviendto currentUser = (nhanviendto) session.getAttribute("currentUser");
        if (currentUser != null) {
            model.addAttribute("updatedUser", convertToModel(currentUser));
            return "/Java5/profile";
        }
        return "redirect:/user/login";
    }

    // Đăng ký người dùng
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("nhanviendto") nhanviendto nhanviendto , Model model) {
        //Kiểm tra mật khẩu và xác nhận mật khẩu
        if (!nhanviendto.getPasswold().equals(nhanviendto.getConfirmPassword())){
            model.addAttribute("error" , "Mật khẩu và xác nhận mật khẩu không khớp");
            return "/Java5/signup";
        }
        if (!nhanviendto.isTermsAccepted()){
            model.addAttribute("error", "Bạn phải đồng ý với điều khoản");
            return "/Java5/signup";
        }
       nhanvien newuser = userservice.register(nhanviendto);
        model.addAttribute("newuser", newuser);
        return "redirect:/user/login";  // Sau khi đăng ký thành công, chuyển hướng tới trang đăng nhập
    }

    // Đăng nhập người dùng
    @PostMapping("/login")
    public String loginUser(@RequestParam String email, @RequestParam String password, Model model, HttpSession session) {
        Optional<nhanviendto> user = userservice.login(email, password);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());

            // Lưu thông tin người dùng vào session
            session.setAttribute("username", user.get().getTenNhanVien());  // Lưu thông tin vào session
            session.setAttribute("currentUser", user.get());
            return "/Java5/index";  // Trang chính sau khi đăng nhập thành công
        } else {
            model.addAttribute("error", "Thông tin đăng nhập không chính xác");
            return "/Java5/login";  // Trang đăng nhập nếu thông tin không chính xác
        }
    }

    @GetMapping("/profile")
    public String viewProfile(HttpSession session, Model model) {
        // Lấy thông tin người dùng từ session
        nhanviendto currentUserDTO = (nhanviendto) session.getAttribute("currentUser");

        if (currentUserDTO == null) {
            model.addAttribute("error", "Bạn cần đăng nhập để xem trang này");
            return "redirect:/user/login";
        }

        // Chuyển đổi từ nhanviendto sang nhanvien
        nhanvien currentUser = convertToModel(currentUserDTO);

        // Thêm thông tin người dùng vào model
        model.addAttribute("updatedUser", currentUser);

        return "/Java5/profile";
    }


    private nhanvien convertToModel(nhanviendto dto) {
        if (dto == null) return null;

        nhanvien model = new nhanvien();
        model.setId(dto.getId());
        model.setTenNhanVien(dto.getTenNhanVien());
        model.setEmail(dto.getEmail());
        model.setSoDienThoai(dto.getSoDienThoai());
        model.setDiaChi(dto.getDiaChi());
        return model;
    }


    @PostMapping("/update")
    public String updateUser(@ModelAttribute("nhanviendto") nhanviendto nhanviendto,
                             @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                             @RequestParam(value = "newPassword", required = false) String newPassword,
                             @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
                             Model model, HttpSession session) {

        // Kiểm tra ID người dùng
        if (nhanviendto.getId() == null) {
            model.addAttribute("error", "ID không hợp lệ");
            return "/Java5/profile";  // Trả lại trang profile với lỗi ID không hợp lệ
        }

        // Kiểm tra mật khẩu xác nhận
        if (newPassword != null && !newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu không khớp!");
            return "/Java5/profile";  // Trở lại trang profile nếu mật khẩu không khớp
        }

        // Cập nhật thông tin người dùng trong cơ sở dữ liệu
        nhanvien updatedUser = userservice.update(nhanviendto, avatar, newPassword);  // Cập nhật thông tin người dùng

        if (updatedUser != null) {
            // Lưu thông tin người dùng đã cập nhật vào session để sử dụng ở các trang khác
            session.setAttribute("username", updatedUser.getTenNhanVien());
            session.setAttribute("currentUser", convertToDTO(updatedUser));  // Lưu nhanviendto vào session

            // Thêm thông tin người dùng đã cập nhật vào model
            model.addAttribute("updatedUser", updatedUser);

            // Trả về trang profile để thông báo thành công
            return "/Java5/profile";
        } else {
            model.addAttribute("error", "Có lỗi xảy ra trong quá trình cập nhật thông tin");
            return "/Java5/profile";  // Trả về trang profile với thông báo lỗi
        }
    }

    // Phương thức chuyển đổi từ nhanvien sang nhanviendto
    private nhanviendto convertToDTO(nhanvien model) {
        nhanviendto dto = new nhanviendto();
        dto.setId(model.getId());
        dto.setTenNhanVien(model.getTenNhanVien());
        dto.setEmail(model.getEmail());
        dto.setSoDienThoai(model.getSoDienThoai());
        dto.setDiaChi(model.getDiaChi());
        dto.setChucVuId(model.getChucVu().getId());  // Giả sử bạn cần lấy ID của chucVu
        return dto;
    }


    // Đăng xuất người dùng
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/user/login";  // Chuyển hướng về trang đăng nhập
    }
}
