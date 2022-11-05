# Locks

MySql에서 사용되는 잠금은 스토리지 레벨의 잠금과 MySQL 엔진 레벨로 나뉜다. MySQL엔진 레벨의 잠금은 모든 스토리지 엔진에 영향을 끼치지만 스토리지 엔진 레벨의 잠금은 스토리지 엔진간 영향을 끼치지 않는다.

## MySQL Engine locks

1. Global Lock

    MySQL 서버의 모든 변경 작업을 멈춘다. `FLUSH TABLES WITH READ LOCK` 명령으로 획득한다. 글로벌 락과, 테이블 락의 해제는 `UNLOCK TABLES`명령을 통해 현재 세션의 모든 잠금을 해제한다. autocommit이 해제되어있는 경우 commit을 완료한 이후 해제 명령을 호출하는 것이 올바른 방법이다.

1. Table Lock

    테이블 단위로 잠금을 설정한다.`LOCK TABLES table_name [READ | WRITE]` 명령으로 획득한다. MyISAM이나  MEMORY 엔진의 테이블을 변경할 경우 MySQL서버가 묵시적으로 잠금을 설정한다. 레코드 기반의 잠금을 제공하는 InnoDB에서는 해당 잠금이 묵시적으로 설정되지 않는다.

1. Named Lock

    임의의 문자열에 대해 잠금을 설정한다. `GET_LOCK('str')` 명령으로 획득, `IS_FREE_LOCK('str')`로 잠금 확인, `RELEASE_LOCK(str)`로 해제한다.. 여러 클라이언트가 상호 동기화 처리를 하는데, 많은 레코드에 대해서 복잡한 변경을 하는 트랜젝션을 제어하는데 유용하게 사용된다.

1. Metadata Lock

    데이터베이스의 객체의 이름이나 구조를 변경하는 경우 획득되는 잠금이다. 명시적으로 획득/해제가 불가능하다.

## InnoDB Storage Engine Locks