package com.example.paperless.card.repository;

import com.example.paperless.card.domain.CardApplication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardApplicationJpaRepository extends JpaRepository<CardApplication, String> {
}