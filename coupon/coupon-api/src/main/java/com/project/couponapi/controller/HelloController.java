package com.project.couponapi.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() throws InterruptedException {
        // RPS에 적힌 수 만큼 API가 처리하는 건지 확인해보기
        // 요청이 들어왔을 때, 0.5초를 쉰 후에 응답을 보냄
        // 응답에 대한 최소값은 500ms -> 초당 대략 2건 정도를 처리할 수 있게됨
        Thread.sleep(500);
        return "hello!";
    }
    /**
     * RPS가 400쯤에서 왔다 갔다함
     * 초당 2건을 처리 * N(서버에서 동시에 처리할 수 있는 수)
     * 여기서 Tomcat은 ThreadPool을 통해 요청을 처리하는데, 기본적으로 ThreadPool의 Max Thread 수는 200이다.
     *
     * application-api.yml에서 thread 수 조절 가능
     *
     * server:
     *   port: 8080
     *   tomcat:
     *     threads:
     *       max: 400
     */
}