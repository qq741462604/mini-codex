package com.study.controller;

import com.study.entity.Member;
import com.study.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/info")
    public Member getMemberInfo(@RequestParam String userId) {
        return memberService.getMemberInfo(userId);
    }
}