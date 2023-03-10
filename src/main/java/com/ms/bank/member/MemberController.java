package com.ms.bank.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    @GetMapping("/test")
    @Transactional
    void addMember() {
        memberService.addMember();
    }


    @GetMapping("/test1")
    @Transactional
    void addMember1(HttpServletResponse httpServletResponse) throws IOException {
        System.out.println("1111");
        System.out.println("222222222");
    }
}

