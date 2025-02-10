package com.example.assignment_java5.Dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class danhgiadto {
    private Long id;
    private Long sanPhamId;
    private Long khachHangId;
    private String noiDung;
    private int danhGia;
    private LocalDateTime ngayDanhGia;
}
