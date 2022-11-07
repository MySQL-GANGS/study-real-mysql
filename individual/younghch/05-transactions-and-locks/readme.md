# Locks

MySql에서 사용되는 잠금은 스토리지 레벨의 잠금과 MySQL 엔진 레벨로 나뉜다. MySQL엔진 레벨의 잠금은 모든 스토리지 엔진에 영향을 끼치지만 스토리지 엔진 레벨의 잠금은 스토리지 엔진간 영향을 끼치지 않는다.

## MySQL Engine Locks

1. Global Locks

    MySQL 서버의 모든 변경 작업을 멈춘다. `FLUSH TABLES WITH READ LOCK` 명령으로 획득한다. 글로벌 락과, 테이블 락의 해제는 `UNLOCK TABLES`명령을 통해 현재 세션의 모든 잠금을 해제한다. autocommit이 해제되어있는 경우 commit을 완료한 이후 해제 명령을 호출하는 것이 올바른 방법이다.

1. Table Locks

    테이블 단위로 잠금을 설정한다.`LOCK TABLES table_name [READ | WRITE]` 명령으로 획득한다. MyISAM이나  MEMORY 엔진의 테이블을 변경할 경우 MySQL서버가 묵시적으로 잠금을 설정한다. 레코드 기반의 잠금을 제공하는 InnoDB에서는 해당 잠금이 묵시적으로 설정되지 않는다.

1. Named Locks

    임의의 문자열에 대해 잠금을 설정한다. `GET_LOCK('str')` 명령으로 획득, `IS_FREE_LOCK('str')`로 잠금 확인, `RELEASE_LOCK(str)`로 해제한다.. 여러 클라이언트가 상호 동기화 처리를 하는데, 많은 레코드에 대해서 복잡한 변경을 하는 트랜젝션을 제어하는데 유용하게 사용된다.

1. Metadata Locks

    데이터베이스의 객체의 이름이나 구조를 변경하는 경우 획득되는 잠금이다. 명시적으로 획득/해제가 불가능하다.

## InnoDB Storage Engine Locks

 1. Record Locks

    레코드의 인덱스에 잠금을 건다.

1. Gap Locks

    레코드 사이의 공간에 잠금을 건다. 예로 `SELECT c1 FROM t WHERE c1 BETWEEN 10 and 20 FOR UPDATE`를 실행했을 때 `c1=15`인 값을 추가하는 것을 방지한다.

1. Next-Key Locks

    레코드 락과 갭 락을 합쳐놓은 형태의 잠금이다. REPETABLE_READ 격리 수준을 보장하기 위해 한 트랜젝션에서 데이터를 읽고 있을 시 다른 세션에서 데이터를 변경/추가하는 것을 방지한다.

1. Insert Intenon Locks

    데이터 추가시 빈 공간에 대해 획득하는 잠금이다. 테이블 단위가 아니라 gap단위로 잠금을 걸어 추가 작업의 동시성을 높힌다.

1. Auto-Inc Locks

    AUTO_INCREMENT 속성을 사용한 경우 자동 증가하는 값의 유일성을 보장하기 위해 사용되는 잠금이다. `innodb_autoinc_lock_mode=1`을 사용하면 추가하는 데이터의 수를 정확히 예측할 수 있을 떄 auto-inc lock을 사용하는 대신 가볍고 빠른 래치(뮤텍스)를 이용해 처리한다.

## How InnoDB locks index on update

변경해야 할 레코드를 찾기 위해 검색한 인덱스에 모두 락을 건다. 테이블에 인덱스가 없는 경우 테이블을 풀 스캔하면서 업데이트 해 전체 레코드를 잠그게 된다. 적절한 인덱스 없이 업데이트를 수행하면 동시성이 떨어진다.

# Isolation Levels

1. READ UNCOMMITTED

    한 트렌젝션에서 commit하지 않은 내용을 다른 트렌젝션에서 볼 수 있는 dirty read가 허용되는 격리 수준이다. 데이터 정합성에 문제가 많아 잘 사용되지 않는다.

1. READ COMMITED

    commit이 완료된 내용만 다른 트렌젝션에서 조회 가능하다. 한 트렌젝션에서 같은 내용을 다시 읽었을 때 그 사이 다른 트랜젝션에서 데이터가 변경된 경우 같은 결과를 얻지 못하는 NOON-REPPEATABLE READ 부정합 문제가 있다. 오라클 DBMS의 default 격리 수준이다.

1. REPEATABLE READ

    InnoDB의 트랜젝션내에서 보여지는 데이터는 트랜젝션에서 처음으로 테이블을 읽었을때의 snapshop으로 일관되게 유지된다. 트랜젝션이 테이블을 처음으로 읽을 때 그 시점을 기록하고, 해당 시점 이후에 변경된 데이터는 보이지 않는다. 다만 이 snapshot은 SELECT 문에만 적용된다. DELETE나 UPDATE문은 다른 트랜젝션에서 변경된 내용에 영향을 끼친다.
    ```
    SELECT COUNT(c1) FROM t1 WHERE c1 = 'xyz';
    -- Returns 0: no rows match.
    DELETE FROM t1 WHERE c1 = 'xyz';
    -- Deletes several rows recently committed by other transaction.

    SELECT COUNT(c2) FROM t1 WHERE c2 = 'abc';
    -- Returns 0: no rows match.
    UPDATE t1 SET c2 = 'cba' WHERE c2 = 'abc';
    -- Affects 10 rows: another txn just committed 10 rows with 'abc' values.
    SELECT COUNT(c2) FROM t1 WHERE c2 = 'cba';
    -- Returns 10: this txn can now see the rows it just updated.
    ```

1. SERIALIZABLE

    읽기 작업도 테이블에 잠금을 걸게된다. PHANTOM READ 문제가 발생하지 않지만 그만큼 동시성이 떨어진다.