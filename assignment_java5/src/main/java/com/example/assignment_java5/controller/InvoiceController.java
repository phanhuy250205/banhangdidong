package com.example.assignment_java5.controller;

import com.example.assignment_java5.model.*;
import com.example.assignment_java5.repository.chitietdonhangreponsitory;
import com.example.assignment_java5.repository.donhangrepository;
import com.example.assignment_java5.repository.hoadonrepository;
import com.example.assignment_java5.repository.sanphamrepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/invoice")
public class InvoiceController {

    private final donhangrepository donHangRepository;
    private final chitietdonhangreponsitory chiTietDonHangRepository;

    @Autowired
    private sanphamrepository sanphamRepository;

    @Autowired
    private hoadonrepository hoadonRepository;

    @Autowired
    public InvoiceController(donhangrepository donHangRepository, chitietdonhangreponsitory chiTietDonHangRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
    }

    @GetMapping("/view")
    public String viewInvoice(@RequestParam Long donHangId, Model model) {
        Optional<donhang> optionalDonHang = donHangRepository.findById(donHangId);
        if (optionalDonHang.isEmpty()) {
            return "redirect:/cart/view";
        }

        donhang donHang = optionalDonHang.get();
        nhanvien customer = donHang.getNhanVien(); // Lấy thông tin nhân viên

        // Kiểm tra dữ liệu đã đúng chưa
        System.out.println("Họ tên: " + customer.getTenNhanVien());
        System.out.println("Số điện thoại: " + customer.getSoDienThoai());
        System.out.println("Địa chỉ: " + customer.getDiaChi());

        model.addAttribute("donHang", donHang);
        model.addAttribute("customer", customer);
        model.addAttribute("cartItems", chiTietDonHangRepository.findByDonHang(donHang));
        model.addAttribute("totalAmount", donHang.getTongTien());

        return "hoadon"; // Trả về template Thymeleaf
    }
    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmInvoice(@RequestParam Long donHangId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        Optional<donhang> optionalDonHang = donHangRepository.findById(donHangId);
        if (optionalDonHang.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Không tìm thấy đơn hàng.");
            return ResponseEntity.badRequest().body(response);
        }

        donhang donHang = optionalDonHang.get();
        nhanvien nguoiMua = donHang.getNhanVien(); // Người mua

        // Lấy nhân viên xác nhận từ session
        nhanvien nhanVienXacNhan = (nhanvien) session.getAttribute("currentUser");
        if (nhanVienXacNhan == null) {
            response.put("status", "error");
            response.put("message", "Bạn cần đăng nhập để xác nhận đơn hàng!");
            return ResponseEntity.badRequest().body(response);
        }

        // ✅ Lưu hóa đơn vào DB
        hoadon hoaDon = new hoadon();
        hoaDon.setNguoiMua(nguoiMua);
        hoaDon.setNhanVien(nhanVienXacNhan);
        hoaDon.setNgayLap(LocalDateTime.now());
        hoaDon.setTongTien(donHang.getTongTien());
        hoadonRepository.save(hoaDon);

        // ✅ Cập nhật trạng thái đơn hàng
        donHang.setTrangThai("Đã thanh toán");
        donHangRepository.save(donHang);

        // ✅ Trả về JSON chứa `hoadonId`
        response.put("status", "success");
        response.put("message", "Mua hàng thành công!");
        response.put("hoadonId", hoaDon.getId());

        return ResponseEntity.ok(response);
    }


}

