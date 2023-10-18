package com.sbs.demo5.domain.document.standard;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface DocumentHavingSortableTags extends DocumentHavingTags {
    String TAGS_STR_SORT_REGEX = "\\[-?\\d*\\/?-?\\d*\\]";

    Set<? extends DocumentSortableTag> getTags();

    default String getTagsWithSortNoStr() { // #tag[sortNo/total]
        if (getTags().isEmpty()) return "";

        return "#" + getTags()
                .stream()
                .map(tag -> tag.getContent() + "[" + tag.getSortNo() + "/" + tag.getKeyword().getTotal() + "]")
                .sorted()
                .collect(Collectors.joining(" #"));
    }

    default void modifyTags(String newTagsStr, Map<String, ? extends DocumentSortableKeyword> keywordsMap) {
        String inputedNewTagsStr = newTagsStr; //#tag[1/1]
        newTagsStr = newTagsStr.replaceAll(DocumentHavingSortableTags.TAGS_STR_SORT_REGEX, ""); // #tag

        Set<String> newTags = Arrays.stream(newTagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX)) // tag
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
        DocumentSortableTag tag = (DocumentSortableTag) addTag(tagContent); // 태그 추가 하고

        DocumentSortableKeyword keyword = keywordsMap.get(tagContent); // 이 태그의 키워드 찾고

        keyword.addTag(tag); // 키워드 add 호출 ( total, sortNo 재정렬 요청)
    }

    default void addTags(String tagsStr, Map<String, ? extends DocumentSortableKeyword> keywordsMap) {
        String inputedTagsStr = tagsStr;
        tagsStr = tagsStr.replaceAll(DocumentHavingSortableTags.TAGS_STR_SORT_REGEX, "");

        Arrays.stream(tagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tagContent -> !tagContent.isEmpty())
                .distinct() // 중복시 stream 에서 제거
                .forEach(tagContent -> addTag(tagContent, keywordsMap));

        Arrays.stream(inputedTagsStr.split(DocumentHavingSortableTags.TAGS_STR_DIVISOR_REGEX))
                .map(String::trim)
                .map(String::toUpperCase)
                .filter(tagContent -> !tagContent.isEmpty())
                .distinct()
                .forEach(tagContent -> {
                    String[] tagContentBits = tagContent.split("\\[", 2); // [ i/i ]

                    if (tagContentBits.length == 1) return; // [] 없으면 return

                    tagContent = tagContentBits[0];

                    tagContentBits = tagContentBits[1].split("/", 2);

                    long newSortNo;

                    try {  // 앞의 숫자 long 으로
                        newSortNo = Long.parseLong(tagContentBits[0].replace("]", "").trim());
                    } catch (Exception ignored) {
                        return;
                    }

                    if (newSortNo < 1) newSortNo = 1; // 1보다 작으면 1
                    if (newSortNo > keywordsMap.get(tagContent).getTotal()) // total 보다 크면 total
                        newSortNo = keywordsMap.get(tagContent).getTotal();

                    final long _newSortNo = newSortNo;
                    final String _tagContent = tagContent;

                    getTags()
                            .stream()
                            .filter(tag -> tag.getContent().equals(_tagContent)) // 기존 태그에 이미 있으면 걸림
                            .findFirst()
                            .ifPresent(tag -> tag.applySortNo(_newSortNo)); // filter 에서 걸린게 있으면 applySortNo(newSortNo) 호출
                });
    }
}
