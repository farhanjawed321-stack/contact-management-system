package com.contactapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "email_addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    private String label; // work, personal, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id")
    private Contact contact;
}