# MockMultiportRequest #
파일 업로드 테스트시 multipart request가 필요한데 이것은 `getParameter()` 기능만 추가한다고 되는 일이 아니다.
우선 `SeveletInputStream`을 구현하는것이 핵심 골짜이기에 거의 `SeveletInputStream` 구현하고 테스트 하는데 초점을 두고 있다

## 동기 ##
- 파일 업로드 단위 테스트에 사용한 Mock request 를 위해
- `SeveletInputStream`을 이해도를 높이기 위해

## 기능 ##
- `SeveletInputStream` 을 구현해 `MultipartRequest` 라이브러리에 대응하게 만든다
- 현재 `MultipartRequest` Commons `FileUpload` 로 파일 업로드 테스트 완료

## 구조 ##
- `ServletInputStream` 의 구현체인 `MockServletInputStream` 이 있음
- `MockServletInputStream` 생성자 호출시 인자로 byte[] 값을 넘겨줘야한다.
- 해당 byte[] 값을 생성하기 위해서 `Part` 클래스를 활용해 작성한다.

## 사용 예시 ##
- `MultipartRequest`가 들어간 로직 테스트 시 예제
```java
//데이터 세팅
String[] paramNames = new String[] {"record_no","title","contents"};
String[] values = new String[] {"00001","제목입니다.","안녕하세요\n내용부입니다."};

//파일 정보 세팅
String paramNameForFile = "up_file";
String filePath = ROOT_PATH + File.separator +"img" + File.separator + "1.jpg";

//ServletInput 스트림을 만들기 위한 Byte 코드 만들어 주기
byte[] b = null;
try {
    Parts parts = new Parts();
    for (int i = 0; i < PARAM_NAMES.length; i++) {
        parts.addPart(PARAM_NAMES[i], VALUES[i]);
    }
    b = parts.makeByte();
} catch (IOException e) {
    e.printStackTrace();
}

ServletInputStream in = new MockServletInputStream(b);
try {
    when(mockReq.getInputStream()).thenReturn(in);
    when(mockReq.getContentLength()).thenReturn(b.length);
    MultipartRequest multipartRequest = new MultipartRequest(mockReq, ROOT_PATH + File.separator + "img_copy", "utf-8");

} catch (IOException e) {
    e.printStackTrace();
}

 ```
