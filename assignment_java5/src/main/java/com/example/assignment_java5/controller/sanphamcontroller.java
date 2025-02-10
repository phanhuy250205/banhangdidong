package com.example.assignment_java5.controller;

import com.example.assignment_java5.Dto.sanphamdto;
import com.example.assignment_java5.model.sanpham;
import com.example.assignment_java5.service.sanphamservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/sanpham")
public class sanphamcontroller {
    @Autowired
    private sanphamservice Sanphamservice;

    //lấy tất cả sản phẩm
    @GetMapping("/")
    public  String getallSanpham(Model model){
        List<sanpham> sanphamList = Sanphamservice.getallSanpham();
        model.addAttribute("sanPhams",sanphamList);
        return "/Java5/products";
    }

    //Lấy tất cả sản phẩm theo id
    @GetMapping("/{id}")
    public  String getsanphamid(@PathVariable int id , Model model){
        sanpham Sanpham = Sanphamservice.getSanPhamById(Long.valueOf(id)).orElse(null);
        model.addAttribute("sanPham",Sanpham);
        return "sanpham";
    }
    @PostMapping("create")
    public  String createSanpham(@ModelAttribute("sanPham") sanpham Sanpham){
        Sanphamservice.createSanPham(Sanpham);
        return "redirect:/sanpham";
    }

    // Xóa sản phẩm
    @GetMapping("/delete/{id}")
    public String deleteSanPham(@PathVariable Long id) {
        Sanphamservice.deleteSanPhamById(id);
        return "redirect:/sanpham/";  // Redirect về trang danh sách sản phẩm
    }

   // Tìm kiếm sản phẩm
        @GetMapping("/search")
        public String searchSanPham(@RequestParam("searchTerm") String searchTerm, Model model) {
            // Gọi phương thức tìm kiếm từ service layer
            List<sanpham> foundProducts = Sanphamservice.searchSanPham(searchTerm);

            // Thêm danh sách sản phẩm tìm thấy vào model để hiển thị trong template
            model.addAttribute("sanPhams", foundProducts);
            return "sanpham"; // Trả về trang danh sách sản phẩm với kết quả tìm kiếm
        }
        @GetMapping("/detail/{id}")
        public  String getsanphamdetail(@PathVariable long id , Model model){
        sanpham Sanpham = Sanphamservice.getSanPhamById(Long.valueOf(id)).orElse(null);
        model.addAttribute("sanPham",Sanpham);
        return "/Java5/product-detail";
    }
}
