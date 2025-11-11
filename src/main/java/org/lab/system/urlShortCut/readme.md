## 단축 URL 설계
### 요구사항
- 단축 url의 길이 및 표현 방식(문자, 숫자)
   - url 고정된 길이(ex) 7자리) & url 형식(문자, 영어 혼용)
   - 단축 url의 구성 : `http://{domain=우리 서비스의 도메인}/{hashValue}`

### api spec
> POST(새로운 리소스 생성) : {domain}/short-urls
> 
* request body : url string(원본 url)
```json
  {
    "originUrl": "https://example.com/original-link-long-string"
  }
```
* response body : url string(단축 url)
```json
  {
    "shortenUrl": "https://ourserver.com/CD1B2C"
  }
```
* response code : 201

> GET(단축 url을 기존의 url으로 리다이렉션 하는 엔드포인트) : {domain}/{hashValue}
* response body : 원래 목적지가 될 url string
* response code : 302 -> 해당 url이 영구적으로(301)가 아닌 임시적으로(302) 새로운 Url로 변경되었음을 나타낸다.

### 구현 방식

Q. 해시를 사용한 이유? : 입력이 같으면 항상 같은 결과가 나오고 + 일정한 길이로 줄일 수 있기 때문
1. 같은 원본 url에 대해서는 항상 <고정된 길이의 임의의 문자열>이 나와야 함 -> 해시 함수를 적용하면 됌(특정 key에 대해서 항상 같은 값이 출력)
2. 데이터베이스 연동이 안되는 지금 상황에서 BEST & DB 가 있어도 DB I/O 없이 빠르게 O(1)에 사용자에게 찾아서 원본 url 리다이렉트 가능
3. 보안성 강화 -> 해시값을 보고 원본 url을 유추할 수 없음(일정한 규칙이 있는게 X)

#### URL 저장소
- 해시의 특성은 원본 복구가 불가능 -> 원본 저장소와의 매핑을 위해 DB에 저장이 필요
- DB : 트래픽이 많고 데이터 간의 관계가 필요 없으므로 확장이 쉬운 NoSQL 고려 가능
- 어플리케이션 단에서 처리 : 조회 / 삽입 / 삭제가 O(1)인 해시 맵 사용

#### 로직 - 단축 url 생성
```
ex) https://google.com/abcdefg/dfksjdlf -> https://ourserver.com/{hashValue}
```

> 방법 1 : 해시 함수 이용
- 기존 해시 함수(sha-256)를 사용하고, 원하는 문자열 길이(N)만큼 잘라서 사용
   - 문제점 : N이 작을수록 충돌 가능성이 높아진다. 
   - 충돌 가능성 산정 : 문자열 8자리 -> sha-256은 16진법으로 표현되므로 한 글자당 4bit -> 총 32bit -> 경우의 수 2^32 = 43억 
   - 충돌 해결 방안 : 충돌이 발생했다면(DB 질의) 충돌이 발생하지 않을때 까지 원본 문자열에 + 알파(사전에 정한 문자열)해서 고유 해시값을 만들어낸다.

> 방법 2 : 해시 함수 작성
- 직접 해시 함수 작성 -> url을 바이트 형태로 변환 -> bit 배열 추출
   - 해당 비트 데이터를 문자열로 인코딩한다 -> base64인코딩(only 문자열, 8bit씩 자름) or 16진법(문자(10이상)+숫자, 4bit씩 자름)로 변환해서 줄인다.
   - 나온 문자열을 앞에서부터 고정된 길이만큼 자른다.
