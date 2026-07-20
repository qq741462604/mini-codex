package com.study.service;

import com.study.entity.Member;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    public Member getMemberInfo(String userId) {
        Member member = new Member();
        member.setId("1");
        member.setUserId(userId);
        member.setLevel(1);
        return member;
    }
}