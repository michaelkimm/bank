# **bank**
**개요**

은행의 돈통(계좌) 및 이체 시스템 구현. 기능 구현 시, 사용자의 편의와 성능에 중점.

**목표**

- 계좌 간에 이체가 가능해야 함.
- 같은 은행간 이체, 다른 은행간 이체 모두 가능해야 함.
- 계좌 잔액 조회가 가능해야 함.
- 이체가 요청될 시, 이체 내역을 적재해야 함.
- 모든 이체는 고유한 식별 번호에 의해 추적 가능해야 함.


# **🕐성능 개선**

![이체 완료 소요 시간 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307410-4bdad169-7e3d-447d-90f7-d126ab149f50.png)

![TPS 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307465-d8bdaa23-fc93-4446-965d-0739ba7f68bc.png)

![Mean Test Time 성능 개선 그래프](https://user-images.githubusercontent.com/50356726/230307459-b5503b8f-b5d0-426f-a0b5-dc47f51e1157.png)

# **🌐아키텍처**

![image](https://user-images.githubusercontent.com/50356726/234209917-4c9f0466-c827-4f37-8741-0dd782993bfa.png)

# **🧾ERD**

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
