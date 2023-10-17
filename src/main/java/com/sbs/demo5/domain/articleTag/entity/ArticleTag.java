package com.sbs.demo5.domain.articleTag.entity;

import com.sbs.demo5.base.jpa.baseEntity.BaseEntity;
import com.sbs.demo5.domain.article.entity.Article;
import com.sbs.demo5.domain.document.standard.DocumentTag;
import com.sbs.demo5.domain.member.entity.Member;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Objects;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class ArticleTag extends BaseEntity implements DocumentTag {
    @ManyToOne
    private Member author;

    @ManyToOne
    private Article article;

    private String content;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArticleTag that = (ArticleTag) o;

        return Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + (author != null ? author.hashCode() : 0);
        result = 31 * result + (article != null ? article.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
