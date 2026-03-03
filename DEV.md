# 개발 노트

## 서버 포트

`gradlew bootRun` 실행 시 접속 경로는 `localhost:8080`로 설정되어 있음.

만약 Windows에서 이 주소에 `:8080` 입력 없이 접속하고 싶다면 포트 프록시를 만들면 됨.

### 포트 프록시 등록

관리자 권한으로 명령 프롬프트를 실행 후, 다음 명령어를 입력한다.

```
netsh interface portproxy add v4tov4 listenaddress=127.0.0.1 listenport=80 connectaddress=127.0.0.1 connectport=8080
```

'IP Helper'라는 서비스에 의존하기 때문에, 해당 서비스가 꺼져 있으면 포트 프록시가 작동하지 않음. 일반적인 상황에서 해당 서비스가 비활성화되는 경우는 매우 적음.

### 포트 프록시 등록 해제

관리자 권한으로 명령 프롬프트를 실행 후, 다음 명령어를 입력한다.

```
netsh interface portproxy delete v4tov4 listenaddress=127.0.0.1 listenport=80
```
