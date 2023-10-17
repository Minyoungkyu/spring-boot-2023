package com.sbs.demo5.domain.document.standard;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface DocumentHavingSortableTags extends DocumentHavingTags {
    String TAGS_STR_SORT_REGEX = "\\[-?\\d*\\/?-?\\d*\\]";

    Set<? extends DocumentSortableTag> getTags();

    default String getTagsWithSortNoStr() {
        if (getTags().isEmpty()) return "";

        return "#" + getTags()
                .stream()
                .map(tag -> tag.getContent() + "[" + tag.getSortNo() + "/" + tag.getKeyword().getTotal() + "]")
                .sorted()
                .collect(Collectors.joining(" #"));
    }

    default void modifyTags(String newTagsStr, Map<String, ? extends DocumentSortableKeyword> keywordsMap) {
        String inputedNewTagsStr = newTagsStr;
        newTagsStr = newTagsStr.replaceAll(DocumentHavingSortableTags.TAGS_STR_SORT_REGEX, "");

        Set<String> newTags = Arrays.stream(newTagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tagContent -> !tagContent.isEmpty())
                .collect(Collectors.toSet());

        // getTags() 에서 newTagsStr 에 없는 것들은 삭제
        getTags().removeIf(tag -> {
            boolean remove = !newTags.contains(tag.getContent());

            if (remove) tag.getKeyword().removeTag(tag);

            return remove;
        });

        addTags(inputedNewTagsStr, keywordsMap);
    }

    default void addTag(String tagContent, Map<String, ? extends DocumentSortableKeyword> keywordsMap) {
        DocumentSortableTag tag = (DocumentSortableTag) addTag(tagContent);

        DocumentSortableKeyword keyword = keywordsMap.get(tagContent);

        keyword.addTag(tag);
    }

    default void addTags(String tagsStr, Map<String, ? extends DocumentSortableKeyword> keywordsMap) {
        String inputedTagsStr = tagsStr;
        tagsStr = tagsStr.replaceAll(DocumentHavingSortableTags.TAGS_STR_SORT_REGEX, "");

        Arrays.stream(tagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tagContent -> !tagContent.isEmpty())
                .distinct()
                .forEach(tagContent -> addTag(tagContent, keywordsMap));

        Arrays.stream(inputedTagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tagContent -> !tagContent.isEmpty())
                .distinct()
                .forEach(tagContent -> {
                    String[] tagContentBits = tagContent.split("\\[", 2);

                    if (tagContentBits.length == 1) return;

                    tagContent = tagContentBits[0];

                    tagContentBits = tagContentBits[1].split("/", 2);

                    long newSortNo;

                    try {
                        newSortNo = Long.parseLong(tagContentBits[0].replace("]", "").trim());
                    } catch (Exception ignored) {
                        return;
                    }

                    if (newSortNo < 1) newSortNo = 1;
                    if (newSortNo > keywordsMap.get(tagContent).getTotal())
                        newSortNo = keywordsMap.get(tagContent).getTotal();

                    final long _newSortNo = newSortNo;
                    final String _tagContent = tagContent;

                    getTags()
                            .stream()
                            .filter(tag -> tag.getContent().equals(_tagContent))
                            .findFirst()
                            .ifPresent(tag -> tag.applySortNo(_newSortNo));
                });
    }
}
