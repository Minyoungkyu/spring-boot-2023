package com.sbs.demo5.domain.post.entity;


import com.sbs.demo5.base.jpa.baseEntity.BaseEntity;
import com.sbs.demo5.domain.document.standard.DocumentHavingSortableTags;
import com.sbs.demo5.domain.document.standard.DocumentSortableTag;
import com.sbs.demo5.domain.document.standard.DocumentTag;
import com.sbs.demo5.domain.member.entity.Member;
import com.sbs.demo5.domain.postTag.entity.PostTag;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Setter
@Getter
@AllArgsConstructor(access = PROTECTED)
@NoArgsConstructor(access = PROTECTED)
@SuperBuilder
@ToString(callSuper = true)
public class Post extends BaseEntity implements DocumentHavingSortableTags {
    @ManyToOne
    private Member author;

    private String subject;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(columnDefinition = "TEXT")
    private String bodyHtml;

    @OneToMany(mappedBy = "post", orphanRemoval = true, cascade = {CascadeType.ALL})
    @Builder.Default
    @ToString.Exclude

    @OrderBy("id ASC")
    private Set<PostTag> postTags = new LinkedHashSet<>();

    @Override
    public Set<? extends DocumentSortableTag> getTags() {
        return postTags;
    }

    @Override
    public DocumentTag addTag(String tagContent) {
        PostTag tag = PostTag.builder()
                .author(author)
                .post(this)
                .content(tagContent)
                .build();

        postTags.add(tag);

        return tag;
    }
}

