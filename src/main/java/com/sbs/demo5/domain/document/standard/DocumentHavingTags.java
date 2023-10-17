package com.sbs.demo5.domain.document.standard;

import com.sbs.demo5.standard.util.Ut;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface DocumentHavingTags extends Document {
    String TAGS_STR_DIVISOR_REGEX = "#|,";

    Set<? extends DocumentTag> getTags();

    default String getTagsStr() {
        if (getTags().isEmpty()) return "";

        return "#" + getTags()
                .stream()
                .map(DocumentTag::getContent)
                .collect(Collectors.joining(" #"));
    }

    DocumentTag addTag(String tagContent);

    default void addTags(String tagsStr) {
        Arrays.stream(tagsStr.split(TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet())
                .forEach(this::addTag);
    }

    default void modifyTags(String newTagsStr) {
        Set<String> newTags = Arrays.stream(newTagsStr.split(TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .collect(Collectors.toSet());

        // getTags() 에서 newTagsStr 에 없는 것들은 삭제
        getTags().removeIf(tag -> !newTags.contains(tag.getContent()));

        addTags(newTagsStr);
    }

    default String getTagLinks(String linkTemplate, String urlTemplate) {
        if (getTags().isEmpty()) return "-";

        final String _linkTemplate = linkTemplate.replace("`", "\"");

        return getTags()
                .stream()
                .map(tag -> _linkTemplate
                        .formatted(urlTemplate.formatted(Ut.url.encode(tag.getContent())), tag.getContent()))
                .sorted()
                .collect(Collectors.joining(" "));
    }
}
