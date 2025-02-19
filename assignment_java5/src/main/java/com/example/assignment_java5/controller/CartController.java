package com.example.assignment_java5.controller;

import com.example.assignment_java5.model.chitietdonhang;
import com.example.assignment_java5.model.donhang;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.repository.chitietdonhangreponsitory;
import com.example.assignment_java5.repository.donhangrepository;
import com.example.assignment_java5.repository.nhanvienrepository;
import com.example.assignment_java5.repository.sanphamrepository;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
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
    @Transactional // Đảm bảo giao dịch được commit
    public String addToCart(@RequestParam Long sanPhamId, HttpSession session) {
        System.out.println("🟢 Nhận request thêm sản phẩm vào giỏ hàng: sanPhamId = " + sanPhamId);

        // 🛑 Kiểm tra nếu người dùng chưa đăng nhập
        Object currentUserObj = session.getAttribute("currentUser");
        if (!(currentUserObj instanceof nhanvien)) {
            System.out.println("🔴 Lỗi: Người dùng chưa đăng nhập.");
            return "redirect:/login";
        }
        nhanvien currentUser = (nhanvien) currentUserObj;
        System.out.println("✅ Người dùng hiện tại: " + currentUser.getId());

        // 🟢 Kiểm tra sản phẩm có tồn tại không
        Optional<sanpham> optionalSanPham = sanPhamRepository.findById(sanPhamId);
        if (optionalSanPham.isEmpty()) {
            System.out.println("🔴 Lỗi: Sản phẩm không tồn tại.");
            return "redirect:/api/sanpham/list";
        }
        sanpham sanPham = optionalSanPham.get();
        System.out.println("✅ Sản phẩm được thêm: " + sanPham.getId() + " - " + sanPham.getTensanpham());

        // 🟢 Kiểm tra đơn hàng chưa thanh toán của nhân viên
        Optional<donhang> optionalDonHang = donHangRepository.findByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        donhang donHang = optionalDonHang.orElseGet(() -> {
            donhang newDonHang = new donhang();
            newDonHang.setNhanVien(currentUser);
            newDonHang.setTrangThai("Chưa thanh toán");
            newDonHang.setTongTien(BigDecimal.ZERO);
            return donHangRepository.save(newDonHang); // Lưu đơn hàng mới
        });

        System.out.println("✅ Đơn hàng hiện tại: " + donHang.getId());

        // 🟢 Kiểm tra sản phẩm trong đơn hàng
        Optional<chitietdonhang> optionalChiTiet = chiTietDonHangRepository.findByDonHangAndSanPham(donHang, sanPham);
        chitietdonhang chiTiet;

        if (optionalChiTiet.isPresent()) {
            chiTiet = optionalChiTiet.get();
            chiTiet.setSoLuong(chiTiet.getSoLuong() + 1);
            System.out.println("🔄 Cập nhật số lượng sản phẩm: " + chiTiet.getSoLuong());
        } else {
            chiTiet = new chitietdonhang();
            chiTiet.setDonHang(donHang);
            chiTiet.setSanPham(sanPham);
            chiTiet.setSoLuong(1);
            chiTiet.setGia(sanPham.getGia());
            System.out.println("🟢 Thêm sản phẩm mới vào giỏ hàng.");
        }

        // 🛑 **LƯU CHI TIẾT ĐƠN HÀNG**
        chiTietDonHangRepository.save(chiTiet);
        System.out.println("✅ Đã lưu sản phẩm vào đơn hàng.");

        // 🟢 Cập nhật tổng tiền đơn hàng
        BigDecimal newTotal = donHang.getTongTien().add(sanPham.getGia());
        donHang.setTongTien(newTotal);
        donHangRepository.save(donHang);
        System.out.println("✅ Cập nhật tổng tiền đơn hàng: " + donHang.getTongTien());

        // 🛒 Cập nhật session giỏ hàng
        int cartCount = chiTietDonHangRepository.countByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
        session.setAttribute("cartCount", cartCount);
        System.out.println("✅ Tổng số sản phẩm trong giỏ hàng (cập nhật session): " + cartCount);

        return "redirect:/api/sanpham/list";
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

        // 🔍 Lấy danh sách sản phẩm CHỈ của nhân viên đang đăng nhập
        List<chitietdonhang> cartItems = chiTietDonHangRepository.findByDonHang_NhanVien_IdAndDonHang_TrangThai(
                currentUser.getId(), "Chưa thanh toán"
        );

        // 🛒 Lưu giỏ hàng vào Model để hiển thị trong Thymeleaf
        model.addAttribute("chiTietDonHang", cartItems);

        // 🔄 Cập nhật số lượng sản phẩm trong session
        int cartCount = cartItems.stream().mapToInt(chitietdonhang::getSoLuong).sum();
        session.setAttribute("cartCount", cartCount);

        System.out.println("✅ Tổng số sản phẩm trong giỏ hàng: " + cartCount);

        return "/Java5/cart"; // Chuyển đến trang giỏ hàng
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





    @GetMapping("/cart/count")
    @ResponseBody
    public int getCartCount(HttpSession session) {
        Object currentUserObj = session.getAttribute("currentUser");
        if (!(currentUserObj instanceof nhanvien)) {
            return 0;
        }
        nhanvien currentUser = (nhanvien) currentUserObj;
        return chiTietDonHangRepository.countByNhanVienAndTrangThai(currentUser, "Chưa thanh toán");
    }


}
