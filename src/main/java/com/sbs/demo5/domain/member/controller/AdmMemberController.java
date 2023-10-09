package com.sbs.demo5.domain.member.controller;

import com.sbs.demo5.base.rq.Rq;
import com.sbs.demo5.domain.member.entity.Member;
import com.sbs.demo5.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/adm/member")
@RequiredArgsConstructor
public class AdmMemberController {
    private final MemberService memberService;
    private final Rq rq;

    @GetMapping("/list")
    public String showList(Model model, @RequestParam(defaultValue = "1") int page) {
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("id"));
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by(sorts));
        Page<Member> memberPage = memberService.findAll(pageable);
        model.addAttribute("memberPage", memberPage);

        return "adm/member/list";
    }
}
