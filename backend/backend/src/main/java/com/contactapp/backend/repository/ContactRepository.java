package com.contactapp.backend.repository;

import com.contactapp.backend.entity.Contact;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository
    extends JpaRepository<Contact, Long> {

    // Get all contacts by user
    Page<Contact> findByUserId(
        Long userId, Pageable pageable);

    // Search by name, email AND phone!
    @Query("SELECT DISTINCT c FROM Contact c " +
           "LEFT JOIN c.emailAddresses e " +
           "LEFT JOIN c.phoneNumbers p " +
           "WHERE c.user.id = :userId AND (" +
           "LOWER(c.firstName) LIKE " +
           "LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE " +
           "LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE " +
           "LOWER(CONCAT('%', :search, '%')) OR " +
           "p.number LIKE " +
           "CONCAT('%', :search, '%'))")
    Page<Contact> searchContacts(
        @Param("userId") Long userId,
        @Param("search") String search,
        Pageable pageable);
}