package com.example.assignment_java5.Dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class chiitiethoadondto {
    private Long id;
    private Long hoaDonId;
    private Long sanPhamId;
    private int soLuong;
    private BigDecimal gia;
}
