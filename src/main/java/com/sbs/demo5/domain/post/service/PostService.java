package com.sbs.demo5.domain.post.service;

import com.sbs.demo5.base.app.AppConfig;
import com.sbs.demo5.base.rsData.RsData;
import com.sbs.demo5.domain.document.service.DocumentService;
import com.sbs.demo5.domain.document.standard.DocumentHavingSortableTags;
import com.sbs.demo5.domain.genFile.entity.GenFile;
import com.sbs.demo5.domain.genFile.service.GenFileService;
import com.sbs.demo5.domain.member.entity.Member;
import com.sbs.demo5.domain.post.entity.Post;
import com.sbs.demo5.domain.post.repository.PostRepository;
import com.sbs.demo5.domain.postKeyword.entity.PostKeyword;
import com.sbs.demo5.domain.postKeyword.repository.PostKeywordRepository;
import com.sbs.demo5.standard.util.Ut;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final PostKeywordRepository postKeywordRepository;
    private final GenFileService genFileService;
    private final DocumentService documentService;

    @Transactional
    public RsData<Post> write(Member author, String subject, String tagsStr, String body) {
        return write(author, subject, tagsStr, body, Ut.markdown.toHtml(body));
    }

    @Transactional
    public RsData<Post> write(Member author, String subject, String tagsStr, String body, String bodyHtml) {
        Post post = Post.builder()
                .author(author)
                .subject(subject)
                .body(body)
                .bodyHtml(bodyHtml)
                .build();

        postRepository.save(post);

        Map<String, PostKeyword> postKeywordsMap = findPostKeywordsMap(author, tagsStr);
        post.addTags(tagsStr, postKeywordsMap);

        documentService.updateTempGenFilesToInBody(post);

        return new RsData<Post>("S-1", post.getId() + "번 글이 생성되었습니다.", post);
    }

    public Page<Post> findByKw(Member author, String kwType, String kw, Pageable pageable) {
        return postRepository.findByKw(author, kwType, kw, pageable);
    }

    public Optional<Post> findById(long id) {
        return postRepository.findById(id);
    }

    public RsData<?> checkActorCanModify(Member actor, Post post) {
        if (actor == null || !actor.equals(post.getAuthor())) {
            return new RsData<>("F-1", "권한이 없습니다.", null);
        }

        return new RsData<>("S-1", "가능합니다.", null);
    }

    public RsData<?> checkActorCanDelete(Member actor, Post post) {
        return checkActorCanModify(actor, post);
    }

    @Transactional
    public RsData<Post> modify(Post post, String subject, String tagsStr, String body, String bodyHtml) {

        Map<String, PostKeyword> postKeywordsMap = findPostKeywordsMap(post.getAuthor(), tagsStr);
        post.modifyTags(tagsStr, postKeywordsMap);

        post.setSubject(subject);
        post.setBody(body);
        post.setBodyHtml(bodyHtml);

        documentService.updateTempGenFilesToInBody(post);

        return new RsData<>("S-1", post.getId() + "번 글이 수정되었습니다.", post);
    }

    private Map<String, PostKeyword> findPostKeywordsMap(Member author, String tagsStr) {
        tagsStr = tagsStr.replaceAll(DocumentHavingSortableTags.TAGS_STR_SORT_REGEX, "");

        return Arrays.stream(tagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tag -> !tag.isEmpty())
                .distinct()
                .map(tag -> postKeywordRepository.findByAuthorAndContent(author, tag)
                        .orElseGet(() -> postKeywordRepository.save(PostKeyword.builder()
                                .author(author)
                                .content(tag)
                                .build())))
                .collect(Collectors.toMap(PostKeyword::getContent, postKeyword -> postKeyword));
    }

    @Transactional
    public RsData<?> remove(Post post) {
        findGenFiles(post).forEach(genFileService::remove);

        postRepository.delete(post);

        return new RsData<>("S-1", post.getId() + "번 게시물이 삭제되었습니다.", null);
    }

    private List<GenFile> findGenFiles(Post post) {
        return genFileService.findByRelId(post.getModelName(), post.getId());
    }

    @Transactional
    public RsData<GenFile> saveAttachmentFile(Post post, MultipartFile attachmentFile, long fileNo) {
        String attachmentFilePath = Ut.file.toFile(attachmentFile, AppConfig.getTempDirPath());
        return saveAttachmentFile(post, attachmentFilePath, fileNo);
    }

    @Transactional
    public RsData<GenFile> saveAttachmentFile(Post post, String attachmentFile, long fileNo) {
        GenFile genFile = genFileService.save(post.getModelName(), post.getId(), "common", "attachment", fileNo, attachmentFile);

        return new RsData<>("S-1", genFile.getId() + "번 파일이 생성되었습니다.", genFile);
    }

    public Map<String, GenFile> findGenFilesMapKeyByFileNo(Post post, String typeCode, String type2Code) {
        return genFileService.findGenFilesMapKeyByFileNo(post.getModelName(), post.getId(), typeCode, type2Code);
    }

    @Transactional
    public void removeAttachmentFile(Post post, long fileNo) {
        genFileService.remove(post.getModelName(), post.getId(), "common", "attachment", fileNo);
    }

    public Page<Post> findByTag(String tagContent, Pageable pageable) {
        return postRepository.findByPostTags_content(tagContent, pageable);
    }

    public Page<Post> findByTag(Member author, String tagContent, Pageable pageable) {
        return postRepository.findByAuthorAndPostTags_content(author, tagContent, pageable);
    }
}
