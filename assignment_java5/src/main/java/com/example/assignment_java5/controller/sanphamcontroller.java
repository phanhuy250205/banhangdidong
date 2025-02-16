package com.example.assignment_java5.controller;

import com.example.assignment_java5.Dto.sanphamdto;
import com.example.assignment_java5.model.phanloaihang;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.service.PhanLoaiHangService;
import com.example.assignment_java5.service.sanphamservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.Banner;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/sanpham")
public class sanphamcontroller {
    @Autowired
    private sanphamservice Sanphamservice;

    @Autowired
    private PhanLoaiHangService phanLoaiHangService;


    //lấy tất cả sản phẩm
    @GetMapping("/")
    public String getAllSanPham(Model model) {
        List<sanpham> sanPhamList = Sanphamservice.getallSanpham();
        List<phanloaihang> danhMucList = phanLoaiHangService.getAllDanhMuc();

        // ✅ Debug: In danh mục ra console
        System.out.println("📌 Danh sách danh mục: " + danhMucList);

        model.addAttribute("sanPhams", sanPhamList);
        model.addAttribute("danhMucList", danhMucList);
        model.addAttribute("sanPham", new sanpham());
        return "/Java5/products";
    }



    @GetMapping("/uploadsanpham")
    public String uploadsanpham(Model model) {
        List<sanpham> sanphamList = Sanphamservice.getallSanpham();
        List<phanloaihang> danhMucList = phanLoaiHangService.getAllDanhMuc(); // 🔹 Lấy danh mục

        // ✅ Debug: Kiểm tra dữ liệu danh mục trong console
        System.out.println("📌 Danh sách danh mục: " + danhMucList);

        model.addAttribute("sanPhams", sanphamList);
        model.addAttribute("danhMucList", danhMucList); // ✅ Truyền danh mục vào Thymeleaf

        return "/Java5/uploadoder";
    }

    @GetMapping("/detail/{id}")
    public String getSanPhamDetail(@PathVariable Long id, Model model) {
        sanpham SanPham = Sanphamservice.getSanPhamById(id).orElse(null);
        if (SanPham == null) {
            return "redirect:/sanpham"; // Nếu không tìm thấy sản phẩm, quay về danh sách
        }
        model.addAttribute("sanPham", SanPham);
        return "/Java5/product-detail"; // Trả về trang chi tiết sản phẩm
    }
    //Lấy tất cả sản phẩm theo id
    @GetMapping("/{id}")
    public  String getsanphamid(@PathVariable int id , Model model){
        sanpham Sanpham = Sanphamservice.getSanPhamById(Long.valueOf(id)).orElse(null);
        model.addAttribute("sanPham",Sanpham);
        return "/Java5/product-detail";
    }
    @PostMapping("/create")
    public String createSanpham(@ModelAttribute sanpham sanPham,
                                @RequestParam("phanLoaiId") Long phanLoaiId,
                                @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        sanPham.setPhanLoaiHang(phanLoaiHangService.getDanhMucById(phanLoaiId)
                .orElseThrow(() -> new RuntimeException("❌ Không tìm thấy danh mục với ID: " + phanLoaiId)));

        Sanphamservice.createSanPham(sanPham, files);
        return "redirect:/sanpham/";
    }
    // Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteSanPham(@PathVariable Long id) {
        Sanphamservice.deleteSanPhamById(id);
        return "redirect:/api/sanpham/uploadsanpham";  // Redirect về trang danh sách sản phẩm
    }

   @GetMapping("/search")
    public String searchSanPham(@RequestParam("searchTerm") String searchTerm, Model model) {
        List<sanpham> foundProducts = Sanphamservice.searchSanPham(searchTerm);
        model.addAttribute("sanPhams", foundProducts);
        return "sanpham"; // Trả về trang danh sách sản phẩm với kết quả tìm kiếm
    }
}
