# **bank**
### [노션 기술 문서](https://excellent-snowshoe-c4c.notion.site/fbb07af7283b4f818204fce604df570a)
**개요**

은행의 돈통(계좌) 및 이체 시스템 구현. 기능 구현 시, 사용자의 편의와 성능에 중점.

**목표**

- 계좌 간에 이체가 가능해야 함.
- 같은 은행간 이체, 다른 은행간 이체 모두 가능해야 함.
- 계좌 잔액 조회가 가능해야 함.
- 이체가 요청될 시, 이체 내역을 적재해야 함.
- 모든 이체는 고유한 식별 번호에 의해 추적 가능해야 함.


# **🕐성능 개선**
### [문제 & 성능 테스트 기록](https://excellent-snowshoe-c4c.notion.site/Bank-e3d0c72454114e6fa804287ec1a3e4a7?pvs=4)
![이체 완료 소요 시간 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307410-4bdad169-7e3d-447d-90f7-d126ab149f50.png)

![TPS 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307465-d8bdaa23-fc93-4446-965d-0739ba7f68bc.png)

![Mean Test Time 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307459-b5503b8f-b5d0-426f-a0b5-dc47f51e1157.png)

# **🌐아키텍처**

![image](https://user-images.githubusercontent.com/50356726/234209917-4c9f0466-c827-4f37-8741-0dd782993bfa.png)

# **🧾ERD**
### [데이터 모델 도출 과정](https://excellent-snowshoe-c4c.notion.site/fe23d902981e4aee8ae6d14110136875?pvs=4)
![image](https://github.com/michaelkimm/bank/assets/50356726/15697425-026d-4195-9865-129df02dcd08)


# **🔧사용 기술**
## Backend
- Java11
- Spring Boot 2.7.0, Spring MVC
- Spring Data JPA
- Junit5, Mockito
- Gradle 7.x

## DevOps
- Ubuntu 20.04
- MySQL
- Github Action
- NCP server, mysql

# **❓기술적 이슈**
## 이체 성능 개선기
- [#1 타행 이체 기능 성능 개선기, 프로젝트 소개](https://ujkim-game.tistory.com/90)
- [#2 타행 이체 기능 성능 개선기, 데이터 정합성](https://ujkim-game.tistory.com/91)
- [#3 타행 이체 기능 성능 개선기, 속도(아웃박스 스케줄러 전략 선정 및 구현)](https://ujkim-game.tistory.com/92)
- [#4 타행 이체 기능 성능 개선기, 속도(인프라 개선)](https://ujkim-game.tistory.com/93)
- [#5 타행 이체 기능 성능 개선기, 속도(이체 입금 요청 API 처리 전략)](https://ujkim-game.tistory.com/94)
- [#6 타행 이체 기능 성능 개선기, 속도(gap lock으로 인한 insert 병목 해결)](https://ujkim-game.tistory.com/96)
- [#7 비관락 획득 후 외부 API 호출 최소화](https://excellent-snowshoe-c4c.notion.site/API-lock-73fd3720aff546f09401a0d4bb5b2eef)

# ❓QnA
[답변 정리 링크](https://excellent-snowshoe-c4c.notion.site/fbb07af7283b4f818204fce604df570a?pvs=4)
1. 왜 이벤트 데이터를 DB에 저장했는가?
2. 이벤트 데이터를 MQ에 발행하지 않은 이유?
3. 이벤트 처리에 대한 요청에 대한 응답이 200이 아니면, 어떻게 처리할 것인가?
4. 왜 두개의 은행을 띄워서 성능 테스트를 실행했나?
5. 분산 트랜잭션의 최종 일관성을 확인할 수 있는 더 좋은 테스트 환경이 있을까?
6. Double request 문제(따닥 문제) 해결할 방법은?
7. 현재 구조에서 더 개선하고 싶은 점은? 
