package com.sbs.demo5.domain.document.standard;

import com.sbs.demo5.standard.util.Ut;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface DocumentHavingTags extends Document {
    String TAGS_STR_DIVISOR_REGEX = "#|,";

    Set<? extends DocumentTag> getTags(); // Set(extends DocumentTag) return

    default String getTagsStr() { // getTags empty 면 "" , 아니면 stream. DocumentTag 에 getContent 후 # 붙여서 합침
        if (getTags().isEmpty()) return "";

        return "#" + getTags()
                .stream()
                .map(DocumentTag::getContent)
                .collect(Collectors.joining(" #"));
    }

    DocumentTag addTag(String tagContent);

    default void addTags(String tagsStr) { // #, 으로 자름 트림 !isEmpty 면 임시 Set 에 저장 (중복 제거) 이후 Set 순환 하여 addTag
        Arrays.stream(tagsStr.split(TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet())
                .forEach(this::addTag);
    }

    default void modifyTags(String newTagsStr) { // #, 잘라서 Set 저장, 기존 DocumentTags 에서 newTags 에 없는 거 삭제 newTagsStr addTags
        Set<String> newTags = Arrays.stream(newTagsStr.split(TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        // getTags() 에서 newTagsStr 에 없는 것들은 삭제
        getTags().removeIf(tag -> !newTags.contains(tag.getContent()));

        addTags(newTagsStr);
    }


    // <div th:utext="${post.getTagLinks('<a class=`link` href=`%s`>#%s</a>', '/usr/post/listByTag/%s')}"></div>
    // <div><a class="link" href="/usr/post/listByTag/getContent">#getContent</a></div>
    default String getTagLinks(String linkTemplate, String urlTemplate) {
        if (getTags().isEmpty()) return "-"; // 태그 없으면

        final String _linkTemplate = linkTemplate.replace("`", "\""); // 백틱 \ 로

        return getTags()
                .stream()
                .map(tag -> _linkTemplate
                        .formatted(urlTemplate.formatted(Ut.url.encode(tag.getContent())), tag.getContent())) // formatting
                .sorted() // .sorted(Comparator.comparingInt(String::length)) - 정렬 커스텀 가능
                .collect(Collectors.joining(" ")); // 공백 더하고 합침
    }
}
