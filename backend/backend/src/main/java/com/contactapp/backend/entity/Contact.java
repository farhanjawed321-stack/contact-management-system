package com.contactapp.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "contacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    private String lastName;
    private String title;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, 
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<EmailAddress> emailAddresses;

    @OneToMany(mappedBy = "contact", cascade = CascadeType.ALL, 
               orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PhoneNumber> phoneNumbers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}