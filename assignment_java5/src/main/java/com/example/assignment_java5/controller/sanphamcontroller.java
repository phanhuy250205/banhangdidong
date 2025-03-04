package com.example.assignment_java5.controller;

import com.example.assignment_java5.Dto.sanphamdto;
import com.example.assignment_java5.model.HinhAnhSanPham;
import com.example.assignment_java5.model.nhanvien;
import com.example.assignment_java5.model.phanloaihang;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.service.HinhAnhSanPhamService;
import com.example.assignment_java5.service.PhanLoaiHangService;
import com.example.assignment_java5.service.sanphamservice;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/sanpham")
public class sanphamcontroller {
    @Autowired
    private sanphamservice Sanphamservice;

    @Autowired
    private PhanLoaiHangService phanLoaiHangService;

    @Autowired
    private HinhAnhSanPhamService hinhAnhSanPhamService;

    @GetMapping("/list")
    public String getSanPhamList(@RequestParam(value = "category", required = false) Long categoryId,
                                 @RequestParam(value = "searchTerm", required = false) String searchTerm,
                                 @RequestParam(value = "minGia", required = false) Double minGia,
                                 @RequestParam(value = "maxGia", required = false) Double maxGia,
                                 @RequestParam(value = "thuongHieu", required = false) List<String> thuongHieu,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 @RequestParam(value = "size", defaultValue = "12") int size,
                                 Model model, HttpSession session) {

        Page<sanpham> sanPhamPage;

        // Lấy thông tin người dùng hiện tại từ session
        nhanvien currentUser = (nhanvien) session.getAttribute("currentUser");
        Long currentUserId = (currentUser != null && currentUser.getChucVu() != null) ? currentUser.getChucVu().getId() : null;

        // In ra currentUserId để kiểm tra
        System.out.println("Current User ID: " + currentUserId);

        // Kiểm tra nếu có categoryId, lọc sản phẩm theo danh mục
        if (categoryId != null) {
            sanPhamPage = Sanphamservice.getSanPhamByPhanLoaiHang_Id(categoryId, PageRequest.of(page, size));
        } else {
            // Nếu không có categoryId, thực hiện tìm kiếm và lọc sản phẩm
            sanPhamPage = Sanphamservice.searchAndFilterSanPham(searchTerm, minGia, maxGia, thuongHieu, page, size);
        }

        // Lấy tất cả danh mục phân loại từ service
        List<phanloaihang> danhMucList = phanLoaiHangService.getAllDanhMuc();

        // Truyền các thuộc tính vào model
        model.addAttribute("sanPhamPage", sanPhamPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", sanPhamPage.getTotalPages());
        model.addAttribute("searchTerm", searchTerm);
        model.addAttribute("minGia", minGia);
        model.addAttribute("maxGia", maxGia);
        model.addAttribute("thuongHieu", thuongHieu);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("danhMucList", danhMucList);

        // Kiểm tra user role và điều chỉnh nút hiển thị tùy theo userId
        if (currentUserId != null) {
            // Kiểm tra xem userId có phải là 10002 (Role người dùng là khách hàng)
            if (currentUserId == 10002) {
                model.addAttribute("showAddToCart", true); // Hiển thị nút "Thêm vào giỏ"
            } else {
                model.addAttribute("showAddToCart", false); // Ẩn nút "Thêm vào giỏ", chỉ hiển thị nút "Xem thống kê"
            }
        } else {
            model.addAttribute("showAddToCart", false); // Không có session user, ẩn nút
        }

        return "products";  // Trang sản phẩm hiển thị danh sách
    }

    @GetMapping("/uploadsanpham")
    public String uploadsanpham(Model model) {
        List<sanpham> sanphamList = Sanphamservice.getallSanpham();
        List<phanloaihang> danhMucList = phanLoaiHangService.getAllDanhMuc(); // 🔹 Lấy danh mục

        // ✅ Debug: Kiểm tra dữ liệu danh mục trong console
        System.out.println("📌 Danh sách danh mục: " + danhMucList);

        model.addAttribute("sanPhams", sanphamList);
        model.addAttribute("danhMucList", danhMucList); // ✅ Truyền danh mục vào Thymeleaf

        return "uploadoder";
    }

    @GetMapping("/detail/{id}")
    public String getSanPhamDetail(@PathVariable Long id, Model model) {
        Optional<sanpham> sanPhamOpt = Sanphamservice.getSanPhamById(id);

        if (sanPhamOpt.isEmpty()) {
            return "redirect:/sanpham";
        }

        sanpham sanPham = sanPhamOpt.get();
        List<HinhAnhSanPham> danhSachAnh = hinhAnhSanPhamService.getHinhAnhBySanPhamId(id);

        // Kiểm tra dữ liệu danh sách ảnh
        for (HinhAnhSanPham anh : danhSachAnh) {
            System.out.println("Ảnh URL: " + anh.getUrlHinhAnh());
        }

        model.addAttribute("sanPham", sanPham);
        model.addAttribute("danhSachAnh", danhSachAnh);

        return "product-detail";
    }


    //Lấy tất cả sản phẩm theo id
    @GetMapping("/{id}")
    public String getsanphamid(@PathVariable int id, Model model) {
        sanpham Sanpham = Sanphamservice.getSanPhamById(Long.valueOf(id)).orElse(null);
        model.addAttribute("sanPham", Sanpham);
        return "product-detail";
    }

    @PostMapping("/create")
    public String createSanpham(@ModelAttribute sanpham sanPham,
                                @RequestParam("phanLoaiId") Long phanLoaiId,
                                @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        sanPham.setPhanLoaiHang(phanLoaiHangService.getDanhMucById(phanLoaiId)
                .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy danh mục với ID: " + phanLoaiId)));

        Sanphamservice.createSanPham(sanPham, files);
        return "redirect:/api/sanpham/uploadsanpham";
    }

    // Xóa sản phẩm
    @GetMapping("/edit/{id}")
    public String editSanPham(@PathVariable Long id, Model model) {
        Optional<sanpham> sanPhamOpt = Sanphamservice.getSanPhamById(id);

        if (sanPhamOpt.isPresent()) {
            sanpham sanPham = sanPhamOpt.get();
            model.addAttribute("sanPham", sanPham);  // Truyền sanPham vào model
            List<phanloaihang> danhMucList = phanLoaiHangService.getAllDanhMuc();
            model.addAttribute("danhMucList", danhMucList);
            return "uploadoder"; // Trả về trang với modal sửa sản phẩm
        } else {
            return "redirect:/api/sanpham/uploadsanpham"; // Điều hướng về trang danh sách nếu không tìm thấy sản phẩm
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteSanPham(@PathVariable Long id) {
        Sanphamservice.deleteSanPhamById(id); // Gọi phương thức xóa từ service
        return "redirect:/api/sanpham/uploadsanpham";  // Điều hướng lại trang danh sách sau khi xóa
    }

    @PostMapping("/update")
    public String updateSanPham(@ModelAttribute sanpham sanPham,
                                @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        Sanphamservice.updateSanPham(sanPham.getId(), sanPham, files); // Cập nhật sản phẩm
        return "redirect:/api/sanpham/uploadsanpham";  // Điều hướng lại trang danh sách sau khi sửa
    }





}