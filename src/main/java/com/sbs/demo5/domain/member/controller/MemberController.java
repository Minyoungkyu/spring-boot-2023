package com.sbs.demo5.domain.member.controller;

import com.sbs.demo5.base.rq.Rq;
import com.sbs.demo5.base.rsData.RsData;
import com.sbs.demo5.domain.member.entity.Member;
import com.sbs.demo5.domain.member.exception.EmailNotVerifiedAccessDeniedException;
import com.sbs.demo5.domain.member.service.MemberService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/usr/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;
    private final Rq rq;

    @PreAuthorize("isAnonymous()")
    @GetMapping("/login")
    public String showLogin() {
        return "usr/member/login";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/join")
    public String showJoin() {
        return "usr/member/join";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/join")
    public String join(@Valid JoinForm joinForm) {
        RsData<Member> joinRs = memberService.join(
                joinForm.getUsername(),
                joinForm.getPassword(),
                joinForm.getNickname(),
                joinForm.getEmail(),
                joinForm.getProfileImg()
        );

        return rq.redirectOrBack("/", joinRs);
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/checkUsernameDup")
    @ResponseBody
    public RsData<String> checkUsernameDup(String username) {
        return memberService.checkUsernameDup(username);
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/checkEmailDup")
    @ResponseBody
    public RsData<String> checkEmailDup(String email) {
        return memberService.checkEmailDup(email);
    }

    public boolean assertCurrentMemberVerified() {
        if (!memberService.isEmailVerified(rq.getMember()))
            throw new EmailNotVerifiedAccessDeniedException("이메일 인증 후 이용해주세요.");

        return true;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/notVerified")
    public String showNotVerified() {
        return "usr/member/notVerified";
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findUsername")
    public String showFindUsername() {
        return "usr/member/findUsername";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/findUsername")
    public String findUsername(String email) {
        return memberService.findByEmail(email)
                .map(member ->
                        rq.redirect(
                                "/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                                "해당 회원의 아이디는 `%s` 입니다.".formatted(member.getUsername())
                        )
                )
                .orElseGet(() -> rq.historyBack("`%s` (은)는 존재하지 않은 회원 이메일 입니다.".formatted(email)));
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/findPassword")
    public String showFindPassword() {
        return "usr/member/findPassword";
    }

    @PreAuthorize("isAnonymous()")
    @PostMapping("/findPassword")
    public String findPassword(String username, String email) {
        return
                memberService.findByUsernameAndEmail(username, email)
                        .map(member -> {
                            memberService.sendTempPasswordToEmail(member);
                            return rq.redirect(
                                    "/usr/member/login?lastUsername=%s".formatted(member.getUsername()),
                                    "해당 회원의 이메일로 임시 비밀번호를 발송하였습니다."
                            );
                        }).orElseGet(() -> rq.historyBack("일치하는 회원이 존재하지 않습니다."));
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class JoinForm {
        @NotBlank
        private String username;
        @NotBlank
        private String nickname;
        @NotBlank
        private String password;
        @NotBlank
        private String email;
        private MultipartFile profileImg;
    }
}
