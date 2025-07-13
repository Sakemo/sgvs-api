package com.flick.business.core.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "providers")
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;
}