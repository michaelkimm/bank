package com.ms.bank.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void addMember() {
        // create
        Member member = new Member("beforeName");
        memberRepository.save(member);

        // read
        Member savedMember = memberRepository.findById(member.getId()).get();
        log.info("Same Entity ? : {}", member == savedMember);
        System.out.println("member: " + member.getClass());
        System.out.println("savedMember: " + savedMember.getClass());

        // update
        String newUsername = "kitty";
        member.setName(newUsername);
//        savedMember.setName(newUsername);
        System.out.println(savedMember);
        memberRepository.save(member);

        // update verify
        savedMember = memberRepository.findById(member.getId()).get();
        log.info("member.name: {}\t savedMember.name: {}", member.getName(), savedMember.getName());
        log.info("Same Username ? : {}", member.getName().equals(savedMember.getName()));

        // delete
        memberRepository.delete(savedMember);
        savedMember = memberRepository.findById(member.getId()).orElse(null);
        log.info("Entity is null ? : {}", savedMember);
    }
}

