package com.sbs.demo5.domain.document.standard;

// 배경지식 시작
// 이 인터페이스가 standard 패키지에 있는 이유 : 이 프로젝트에서 standard 는 일종의 규약이라는 뜻 입니다.
// 이 인터페이스는 텍스트에디터를 통해 작성되는 글들이 지켜야하는 규약을 담았습니다.
// 이 인터페이스가 적용 대상 : Post, Article
// 이를 통해서 TextEditorService 에서 Article 과 Post 를 구분해서 처리할 필요가 없어졌습니다.
// 배경지식 끝
public interface Document {
    String getBody();

    String getBodyHtml();

    void setBody(String body);

    void setBodyHtml(String bodyHtml);

    default String getBodyForEditor() {
        return getBody()
                .replaceAll("(?i)(</?)script", "$1t-script");
    }

    default String getBodyHtmlForPrint() {
        return getBodyHtml()
                .replace("toastui-editor-ww-code-block-highlighting", "");
    }
}
