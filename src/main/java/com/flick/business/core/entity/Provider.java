package com.flick.business.core.entity;

import com.flick.business.core.entity.security.User;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "providers")
public class Provider {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = true, length = 255)
  private String cnpj;

  @Column(nullable = true, length = 255)
  private String notes;

  @Column(nullable = true, length = 20)
  private String phone;

  @Column(nullable = true, length = 255)
  private String email;

  @Column(nullable = true, length = 255)
  private String address;

}
