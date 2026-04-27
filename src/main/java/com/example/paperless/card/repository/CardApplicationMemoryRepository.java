package com.example.paperless.card.repository;

import com.example.paperless.card.domain.CardApplication;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
//데이터 저장/조회 역할을 하는 Spring Bean이라는 뜻
//데이터 저장소 접근 담당

//ConcurrentHashMap : 메모리에 데이터를 저장하는 공간
//key   = applicationId
//value = CardApplication 객체
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