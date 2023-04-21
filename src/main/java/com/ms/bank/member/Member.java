package com.ms.bank.member;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@Entity
public class Member {
    @Id
    @Column(name ="member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long memberId;

    @Column(name = "name")
    private String name;

    protected Member() {}

    public Member(String name) {
        this.name = name;
    }
}

