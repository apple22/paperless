package com.example.paperless;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
/*
Repository
데이터 저장/조회 담당 계층

Memory Repository
DB 대신 메모리에 데이터를 저장하는 학습용 저장소

Map
key-value 형태로 데이터를 저장하는 자료구조

ConcurrentHashMap
동시 요청 환경에서 HashMap보다 안전한 Map

Optional
값이 있을 수도 없을 수도 있음을 표현하는 Java 타입

@PathVariable
URL 경로의 값을 메서드 파라미터로 받는 어노테이션

Domain Object
업무 데이터를 표현하는 내부 객체

클래스 : 설계도
객체 : 실제 만들어진 물건
메서드 : 객체가 할수 있는 행동

 */
@SpringBootApplication
public class PaperlessApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaperlessApplication.class, args);
    }

}
