package com.sbs.demo5.domain.document.standard;

import java.util.Set;

public interface DocumentSortableKeyword {

    long getTotal();

    void setTotal(long total);

    Set<? extends DocumentSortableTag> getTags();

    // 특정 postTag 의 순서를 바꾸고, 그것과 관련된 후 처리도 한다.
    default void applySortNo(DocumentSortableTag tag, long newSortNo) {
        // 기존 순서를 저장
        long oldSortNo = tag.getSortNo();

        // 새 순서와 기존 순서가 같으면 패스
        if (oldSortNo == newSortNo) return;

        if (newSortNo < oldSortNo)
            // 새 순서가 기존 순서보다 작으면 기존 순서보다 크거나 같은 순서들의 순서번호를 1씩 증가
            getTags().stream().filter(t -> t.getSortNo() >= newSortNo && t.getSortNo() < oldSortNo).forEach(t -> t.setSortNo(t.getSortNo() + 1));
        else
            // 새 순서가 기존 순서보다 크면 기존 순서보다 작거나 같은 순서들의 순서번호를 1씩 감소
            getTags().stream().filter(t -> t.getSortNo() <= newSortNo && t.getSortNo() > oldSortNo).forEach(t -> t.setSortNo(t.getSortNo() - 1));

        // 새 순서를 적용
        tag.setSortNo(newSortNo);
    }

    default void addTag(DocumentSortableTag tag) { // 태그 추가 부가
        tag.setKeyword(this); // 키워드 세팅
        boolean added = _addTag(tag);

        if (added) {
            tag.setSortNo(getTotal() + 1); // 토탈 +1
            setTotal(getTotal() + 1); // 솥넘 +1
        }
    }

    boolean _addTag(DocumentSortableTag tag);

    default void removeTag(DocumentSortableTag tag) { // 태그 제거
        getTags().stream().filter(t -> t.getSortNo() > tag.getSortNo()).forEach(t -> t.setSortNo(t.getSortNo() - 1)); // 제거 할 태그의 솥 넘 보다 큰 애들의 솥넘 -1

        getTags().remove(tag); // 태그 제거
        setTotal(getTotal() - 1); // 토탈도 -1
    }
}
