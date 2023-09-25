package com.sbs.demo5.domain.member.service;

import com.sbs.demo5.base.rsData.RsData;
import com.sbs.demo5.domain.email.service.EmailService;
import com.sbs.demo5.domain.genFile.entity.GenFile;
import com.sbs.demo5.domain.genFile.service.GenFileService;
import com.sbs.demo5.domain.member.entity.Member;
import com.sbs.demo5.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenFileService genFileService;
    private final EmailService emailService;

    @Transactional
    public RsData<Member> join(String username, String password, String nickname, String email, MultipartFile profileImg) {
        if (findByUsername(username).isPresent())
            return RsData.of("F-1", "%s(은)는 사용중인 아이디입니다.".formatted(username));

        if (findByEmail(email).isPresent())
            return RsData.of("F-2", "%s(은)는 사용중인 이메일입니다.".formatted(username));

        Member member = Member
                .builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .nickname(nickname)
                .email(email)
                .build();

        member = memberRepository.save(member);

        if (profileImg != null) {
            genFileService.save(member.getModelName(), member.getId(), "common", "profileImg", 0, profileImg);
        }

        sendJoinCompleteMail(member);

        return RsData.of("S-1", "회원가입이 완료되었습니다.", member);
    }

    private void sendJoinCompleteMail(Member member) {
        CompletableFuture<RsData> sendRsFuture = emailService.send(member.getEmail(), "회원가입이 완료되었습니다.", "회원가입이 완료되었습니다.");

        final String email = member.getEmail();

        sendRsFuture.whenComplete((rs, throwable) -> {
            if (rs.isFail()) {
                log.info("메일 발송 실패 : " + email);
                return;
            }

            log.info("메일 발송 성공 : " + email);
        });
    }

    private Optional<Member> findByEmail(String email) {
        return memberRepository.findByEmail(email);
    }

    public Optional<Member> findByUsername(String username) {
        return memberRepository.findByUsername(username);
    }

    public Optional<Member> findById(long id) {
        return memberRepository.findById(id);
    }

    public RsData<String> checkUsernameDup(String username) {
        if (findByUsername(username).isPresent()) return RsData.of("F-1", "%s(은)는 사용중인 아이디입니다.".formatted(username));

        return RsData.of("S-1", "%s(은)는 사용 가능한 아이디입니다.".formatted(username), username);
    }

    public RsData<String> checkEmailDup(String email) {
        if (findByEmail(email).isPresent()) return RsData.of("F-1", "%s(은)는 사용중인 이메일입니다.".formatted(email));

        return RsData.of("S-1", "%s(은)는 사용 가능한 이메일입니다.".formatted(email), email);
    }

    public Optional<String> findProfileImgUrl(Member member) {
        return genFileService.findGenFileBy(
                        member.getModelName(), member.getId(), "common", "profileImg", 0
                )
                .map(GenFile::getUrl);
    }
}
