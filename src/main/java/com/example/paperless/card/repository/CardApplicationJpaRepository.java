package com.example.paperless.card.repository;

import com.example.paperless.card.domain.CardApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardApplicationJpaRepository extends JpaRepository<CardApplication, String> {

    List<CardApplication> findAllByOrderByCreatedAtDesc();
}