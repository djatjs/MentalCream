# MentalCream v2 사용자 데이터 전환

v2는 `app_user`를 기준으로 일일 기록, 완료 활동, AI 추천을 사용자별로 분리한다. 애플리케이션의 `ddl-auto`가 `none`이므로 실행 전에 DB 스키마를 준비해야 한다.

## 신규 환경

빈 Oracle 스키마에서 `docs/v2-schema.sql`을 실행한다. 애플리케이션 실행 후 `/register`에서 계정을 만들면 BCrypt로 암호화된 비밀번호가 저장된다.

## 기존 v1 데이터가 있는 환경

v1의 `daily_log.log_date` 기본키와 `done_item.log_date` 외래키가 v2에서는 각각 `daily_log.id`, `done_item.daily_log_id`로 바뀐다. 따라서 기존 테이블에 v2 DDL을 바로 실행하면 안 된다.

1. 기존 `daily_log`, `done_item`, `suggestion`을 백업한다.
2. 별도의 빈 스키마에 v2 테이블을 만든다.
3. `/register`로 이관 대상 계정을 생성한다.
4. 기존 일일 기록을 해당 계정의 `user_id`와 함께 `daily_log`에 넣는다.
5. 새로 생성된 `daily_log.id`를 날짜로 매핑해 기존 완료 활동의 `daily_log_id`로 넣는다.
6. 기존 추천에 같은 `user_id`를 지정해 옮긴다.
7. 사용자별 동일 날짜 데이터가 한 건인지 확인한 뒤 애플리케이션 연결 정보를 v2 스키마로 전환한다.

운영 DB에서는 백업과 검증 없이 테이블 삭제·이름 변경을 하지 않는다. 특히 기존 데이터가 어느 사용자 소유인지 애플리케이션이 자동 판단할 수 없으므로, 이관 계정은 사람이 명시적으로 결정해야 한다.

## 비밀값 설정

DB 비밀번호와 Gemini API 키는 Git에 커밋하지 않는다. 로컬 `application.properties`에서는 환경 변수를 참조하도록 구성하는 것을 권장한다.

```properties
spring.datasource.password=${MENTALCREAM_DB_PASSWORD}
gemini.api.key=${GEMINI_API_KEY}
```
