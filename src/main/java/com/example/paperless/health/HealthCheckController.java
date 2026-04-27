package com.example.paperless.health;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

//Http요청을 받는 Api contorller
@RestController
public class HealthCheckController {
    //접속주소
   @GetMapping("/health")
    public Map<String , Object> health(){
       return Map.of(
         "status" , "up" ,
         "application" , "paper-less-card-application",
               "javaVersion" , System.getProperty("java.version"),
                "serverTime" , LocalDateTime.now()

       );
   }
}
