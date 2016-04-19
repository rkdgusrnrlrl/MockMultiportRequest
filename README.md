# MockMultiportRequest #
파일 업로드 테스트시 multipart request가 필요한데 이것은 `getParameter()` 기능만 추가한다고 되는 일이 아니다.
우선 `SeveletInputStream`을 구현하는것이 핵심 골짜이기에 거의 `SeveletInputStream` 구현하고 테스트 하는데 초점을 두고 있다

## 동기 ##
- 파일 업로드 단위 테스트에 사용한 Mock request 를 위해
- `SeveletInputStream`을 이해도를 높이기 위해

## 기능 ##
- `SeveletInputStream` 을 구현해 `MultipartRequest` 라이브러리에 대응하게 만든다

## 사용 예시 ##
- 미구현