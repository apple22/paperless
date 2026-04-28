package com.example.paperless.card.repository;

import com.example.paperless.card.domain.CardApplication;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CardApplicationMemoryRepository {

    private final Map<String, CardApplication> store = new ConcurrentHashMap<>();

    public CardApplication save(CardApplication cardApplication) {
        store.put(cardApplication.getApplicationId(), cardApplication);
        return cardApplication;
    }

    public Optional<CardApplication> findById(String applicationId) {
        return Optional.ofNullable(store.get(applicationId));
    }

    public int count() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }
}