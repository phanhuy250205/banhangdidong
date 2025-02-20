package com.example.assignment_java5.controller;

import com.example.assignment_java5.model.chitietdonhang;
import com.example.assignment_java5.model.donhang;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.repository.chitietdonhangreponsitory;
import com.example.assignment_java5.repository.donhangrepository;
import com.example.assignment_java5.repository.nhanvienrepository;
import com.example.assignment_java5.repository.sanphamrepository;
import com.example.assignment_java5.service.CartService;
import com.example.assignment_java5.service.impl.CartServiceImpl;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import jakarta.transaction.Transactional;
import jakarta.persistence.EntityManager;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final donhangrepository donHangRepository;
    private final chitietdonhangreponsitory chiTietDonHangRepository;
    private final sanphamrepository sanPhamRepository;
    private final nhanvienrepository nhanVienRepository;

    @Autowired
    private CartServiceImpl cartService;
    @PersistenceContext
    private EntityManager entityManager;

    public CartController(donhangrepository donHangRepository,
                          chitietdonhangreponsitory chiTietDonHangRepository,
                          sanphamrepository sanPhamRepository,
                          nhanvienrepository nhanVienRepository) {
        this.donHangRepository = donHangRepository;
        this.chiTietDonHangRepository = chiTietDonHangRepository;
        this.sanPhamRepository = sanPhamRepository;
        this.nhanVienRepository = nhanVienRepository;
    }
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestParam Long sanPhamId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        // 🛑 Kiểm tra nếu người dùng chưa đăng nhập
        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        if (currentUser == null) {
            response.put("status", "error");
            response.put("message", "Người dùng chưa đăng nhập.");
            return ResponseEntity.badRequest().body(response);
        }

        // 🛑 Kiểm tra sản phẩm có tồn tại không
        Optional<sanpham> optionalSanPham = sanPhamRepository.findById(sanPhamId);
        if (optionalSanPham.isEmpty()) {
            response.put("status", "error");
            response.put("message", "Sản phẩm không tồn tại.");
            return ResponseEntity.badRequest().body(response);
        }
        sanpham sanPham = optionalSanPham.get();

        // 🟢 Kiểm tra đơn hàng chưa thanh toán của nhân viên
        Optional<donhang> optionalDonHang = donHangRepository.findByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        donhang donHang = optionalDonHang.orElseGet(() -> {
            donhang newDonHang = new donhang();
            newDonHang.setNhanVien(currentUser);
            newDonHang.setTrangThai("Chưa thanh toán");
            newDonHang.setTongTien(BigDecimal.ZERO);
            return donHangRepository.save(newDonHang);
        });

        // 🟢 Kiểm tra sản phẩm trong đơn hàng
        Optional<chitietdonhang> optionalChiTiet = chiTietDonHangRepository.findByDonHangAndSanPham(donHang, sanPham);
        chitietdonhang chiTiet;

        if (optionalChiTiet.isPresent()) {
            chiTiet = optionalChiTiet.get();
            chiTiet.setSoLuong(chiTiet.getSoLuong() + 1);
        } else {
            chiTiet = new chitietdonhang();
            chiTiet.setDonHang(donHang);
            chiTiet.setSanPham(sanPham);
            chiTiet.setSoLuong(1);
            chiTiet.setGia(sanPham.getGia());
        }

        // ✅ Lưu vào DB
        chiTietDonHangRepository.save(chiTiet);

        // ✅ Cập nhật tổng tiền đơn hàng
        BigDecimal newTotal = donHang.getTongTien().add(sanPham.getGia());
        donHang.setTongTien(newTotal);
        donHangRepository.save(donHang);

        // ✅ Lấy tổng số sản phẩm trong giỏ hàng (đếm theo số lượng)
//        int cartCount = chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(currentUser.getId(), "Chưa thanh toán")
//                .stream()
//                .mapToInt(chitietdonhang::getSoLuong)
//                .sum();


        //Lấy theo tổng số sản sản phẩm
        int cartCount = (int) chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(currentUser.getId(), "Chưa thanh toán")
                .stream()
                .map(chitietdonhang::getSanPham)
                .distinct()
                .count();
        session.setAttribute("cartCount", cartCount);

        // ✅ Trả về JSON để cập nhật ngay trên UI
        response.put("status", "success");
        response.put("cartCount", cartCount);
        response.put("totalAmount", donHang.getTongTien());

        return ResponseEntity.ok(response);
    }




    /**
     * ✅ **Hiển thị giỏ hàng**
     */
    @GetMapping("/view")
    public String viewCart(Model model, HttpSession session) {
        System.out.println("🟢 Yêu cầu xem giỏ hàng");

        // 🛑 Kiểm tra nếu người dùng chưa đăng nhập
        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        if (currentUser == null) {
            System.out.println("🔴 Lỗi: Người dùng chưa đăng nhập.");
            return "redirect:/user/login";
        }

        System.out.println("✅ Người dùng hiện tại: " + currentUser.getId());

        // 🔍 Lấy danh sách sản phẩm trong giỏ hàng của nhân viên đang đăng nhập
        List<chitietdonhang> cartItems = chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(
                currentUser.getId(), "Chưa thanh toán"
        );

        // 🛒 Lưu giỏ hàng vào Model để hiển thị trong Thymeleaf
        model.addAttribute("chiTietDonHang", cartItems);

        // ❌ Sai: Tính tổng số lượng sản phẩm
        // int cartCount = cartItems.stream().mapToInt(chitietdonhang::getSoLuong).sum();

        // ✅ Đếm số loại sản phẩm trong giỏ hàng
        int cartCount = (int) cartItems.stream().map(chitietdonhang::getSanPham).distinct().count();
        session.setAttribute("cartCount", cartCount);

        System.out.println("✅ Tổng số loại sản phẩm trong giỏ hàng: " + cartCount);

        // 🔢 Tính tổng tiền đơn hàng
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (chitietdonhang item : cartItems) {
            if (item.getGia() != null) {
                totalAmount = totalAmount.add(item.getGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
            }
        }

        // 🏷️ Giảm giá cố định (nếu có)
        BigDecimal discount = new BigDecimal(2000000);
        BigDecimal finalTotal = totalAmount.subtract(discount).max(BigDecimal.ZERO); // Tránh giá trị âm

        // ✅ Truyền tổng tiền vào model
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("finalTotal", finalTotal);

        System.out.println("🟢 Tạm tính: " + totalAmount + "₫");
        System.out.println("🟢 Tổng cộng sau giảm giá: " + finalTotal + "₫");

        return "/Java5/cart"; // Chuyển đến trang giỏ hàng
    }

    private nhanvien getCurrentUser(HttpSession session) {
        return (nhanvien) session.getAttribute("currentUser");
    }
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateQuantity(
            @RequestParam Long sanPhamId,
            @RequestParam int quantity,
            HttpSession session) {

        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        Optional<donhang> optionalDonHang = donHangRepository.findByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        if (optionalDonHang.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }
        donhang donHang = optionalDonHang.get();

        Optional<chitietdonhang> optionalChiTiet = chiTietDonHangRepository.findByDonHangAndSanPham(donHang, sanPhamRepository.findById(sanPhamId).orElse(null));
        if (optionalChiTiet.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        chitietdonhang chiTiet = optionalChiTiet.get();
        sanpham sanPham = chiTiet.getSanPham();

        // 🛑 Kiểm tra số lượng tồn kho trước khi cập nhật
        if (quantity > sanPham.getSoLuong()) {
            quantity = sanPham.getSoLuong(); // Giới hạn số lượng theo tồn kho
        }

        chiTiet.setSoLuong(quantity);
        chiTietDonHangRepository.save(chiTiet);

        // 🟢 Cập nhật tổng tiền đơn hàng
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (chitietdonhang item : chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(currentUser.getId(), "Chưa thanh toán")) {
            totalAmount = totalAmount.add(item.getGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
        }
        donHang.setTongTien(totalAmount);
        donHangRepository.save(donHang);

        // 🟢 Trả về tổng tiền mới & số lượng tồn kho để cập nhật trên giao diện
        Map<String, Object> response = new HashMap<>();
        response.put("totalAmount", totalAmount);
        response.put("soLuongTonKho", sanPham.getSoLuong()); // 🛑 Gửi số lượng tồn kho về frontend

        return ResponseEntity.ok(response);
    }


    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long sanPhamId, HttpSession session) {
        System.out.println("🛑 Yêu cầu xóa sản phẩm khỏi giỏ hàng: sanPhamId = " + sanPhamId);

        // 🛑 Kiểm tra nếu người dùng chưa đăng nhập
        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        if (currentUser == null) {
            System.out.println("🔴 Lỗi: Người dùng chưa đăng nhập.");
            return "redirect:/login";
        }
        System.out.println("✅ Người dùng hiện tại: " + currentUser.getId());

        // 🔍 Kiểm tra sản phẩm có tồn tại không
        Optional<sanpham> optionalSanPham = sanPhamRepository.findById(sanPhamId);
        if (optionalSanPham.isEmpty()) {
            System.out.println("🔴 Lỗi: Sản phẩm không tồn tại.");
            return "redirect:/cart/view";
        }
        sanpham sanPham = optionalSanPham.get();
        System.out.println("✅ Sản phẩm cần xóa: " + sanPham.getId() + " - " + sanPham.getTensanpham());

        // 🔍 Lấy đơn hàng chưa thanh toán của nhân viên hiện tại
        Optional<donhang> optionalDonHang = donHangRepository.findByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        if (optionalDonHang.isEmpty()) {
            System.out.println("🔴 Lỗi: Không tìm thấy đơn hàng chưa thanh toán.");
            return "redirect:/cart/view";
        }

        donhang donHang = optionalDonHang.get();
        System.out.println("✅ Đơn hàng hiện tại: " + donHang.getId());

        // 🔍 Tìm sản phẩm cần xóa trong giỏ hàng (SỬA LỖI Ở ĐÂY)
        Optional<chitietdonhang> optionalChiTiet = chiTietDonHangRepository.findByDonHangAndSanPham(donHang, sanPham);
        if (optionalChiTiet.isEmpty()) {
            System.out.println("🔴 Lỗi: Không tìm thấy sản phẩm này trong giỏ hàng.");
            return "redirect:/cart/view";
        }

        chitietdonhang chiTiet = optionalChiTiet.get();
        System.out.println("✅ Xóa sản phẩm: " + chiTiet.getSanPham().getTensanpham());

        // 🔥 Xóa sản phẩm khỏi giỏ hàng
        chiTietDonHangRepository.delete(chiTiet);

        // 🟢 Cập nhật tổng tiền đơn hàng
        donHang.setTongTien(donHang.getTongTien().subtract(chiTiet.getGia().multiply(BigDecimal.valueOf(chiTiet.getSoLuong()))));
        donHangRepository.save(donHang);
        System.out.println("✅ Cập nhật tổng tiền đơn hàng: " + donHang.getTongTien());

        // 🛒 Cập nhật session giỏ hàng
        int cartCount = chiTietDonHangRepository.countByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        session.setAttribute("cartCount", cartCount);
        System.out.println("✅ Cập nhật số lượng sản phẩm trong giỏ hàng: " + cartCount);

        return "redirect:/cart/view";
    }






    @GetMapping("/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        if (currentUser == null) {
            return 0;
        }

        // ✅ Đếm số loại sản phẩm trong giỏ hàng (không tính tổng số lượng)
        return (int) chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(
                currentUser.getId(), "Chưa thanh toán"
        ).stream().map(chitietdonhang::getSanPham).distinct().count();
    }

    @GetMapping("/checkout")
    public String checkoutPage(Model model, @RequestParam Long nhanVienId) {
        BigDecimal totalAmount = cartService.calculateTotalPrice(nhanVienId); // Tính tổng tiền
        BigDecimal discount = new BigDecimal(2000000); // Giảm giá cố định
        BigDecimal finalTotal = totalAmount.subtract(discount).max(BigDecimal.ZERO); // Tránh giá trị âm

        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("finalTotal", finalTotal);
        return "/Java5/checkout";
    }



}
