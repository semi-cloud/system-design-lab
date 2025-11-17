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

- `해싱(Hashing)`을 활용해 구현한다.
  - 같은 원본 url에 대해서는 항상 <고정된 길이의 임의의 문자열>이 나와야 함
  - 해시값을 보고 원본 url을 유추할 수 없기에(일정한 규칙이 있는게 X) 보안성 강화

#### 전체 흐름
 1. 전달받은 url이 유효한지 검사한다.
 2. 유효하다면 해당 url의 단축 url이 이미 생성되었는지 검사하고, 있다면 반환한다.
 3. 새로운 원본 url이라면 해시 함수를 적용해 중복 없는 단축 url을 만들어낸다.
 4. 추후 리다이렉션을 위해 원본과 단축 url 매핑 정보를 hashmap에 저장한다.

#### URL 저장소
- 해시의 특성은 원본 데이터 복구가 불가능 -> 원본 저장소와의 매핑을 위해 값 저장이 필요
  - DB : 트래픽이 많고 데이터 간의 관계가 필요 없으므로 확장이 쉬운 NoSQL 고려 가능
  - 메모리 : 조회 / 삽입 / 삭제가 O(1)인 해시 맵 사용

#### 해시값 생성 방식
```
ex) https://google.com/abcdefg/dfksjdlf -> https://ourserver.com/{hashValue}
```

- 기존 해시 함수(sha-256)를 사용하고, 결과를 문자열로 인코딩한 뒤, 원하는 문자열 길이(N)만큼 잘라서 사용
   - 문제점 : N이 작을수록 충돌 가능성이 높아진다. 
   - 충돌 가능성 산정 : 문자열 8자리 -> sha-256은 16진법으로 표현되므로 한 글자당 4bit -> 총 32bit -> 경우의 수 2^32 = 43억 
   - 충돌 해결 방안 : 충돌이 발생했다면(DB 질의) 충돌이 발생하지 않을때 까지 원본 문자열에 알파(사전에 정한 문자열)를 더해서 고유 해시값을 만들어낸다.

#### 문자열 인코딩 방식 선정

1. URL-safe base64 인코딩 적용(택 O)
   - 문자(URL에 안전한 문자) + 숫자, 8bit씩 자름
   - N글자 -> 가능한 경우의 수는 64^N 가지 : 충돌 가능성 낮음
   
2. 16진법 적용(택 X)
   - 문자(10이상) + 숫자, 4bit씩 자름
   - base64보다 같은 비트 수 기준으로 문자열 길이가 길어짐
   - N글자 -> 가능한 경우의 수는 16^N 가지 : 충돌 가능성 base64 보다 높음
