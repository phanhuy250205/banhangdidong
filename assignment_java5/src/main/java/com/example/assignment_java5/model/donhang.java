package com.example.assignment_java5.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "don_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class donhang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "khach_hang_id")
    private khachhang khachHang;

    private LocalDateTime ngayDat = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal tongTien;
}
