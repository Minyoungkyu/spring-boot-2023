package com.sbs.demo5.domain.article.entity;

import com.sbs.demo5.base.jpa.baseEntity.BaseEntity;
import com.sbs.demo5.domain.articleTag.entity.ArticleTag;
import com.sbs.demo5.domain.board.entity.Board;
import com.sbs.demo5.domain.document.standard.DocumentHavingTags;
import com.sbs.demo5.domain.document.standard.DocumentTag;
import com.sbs.demo5.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Article extends BaseEntity implements DocumentHavingTags {
    @ManyToOne
    private Member author;

    @ManyToOne
    private Board board;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    @OneToMany(mappedBy = "article", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude
    private Set<ArticleTag> articleTags = new HashSet<>();

    @Override
    public Set<? extends DocumentTag> getTags() {
        return articleTags;
    }

    @Override
    public DocumentTag addTag(String tagContent) {
        ArticleTag tag = ArticleTag.builder()
                .author(author)
                .article(this)
                .content(tagContent)
                .build();

        articleTags.add(tag);

        return tag;
    }
}
