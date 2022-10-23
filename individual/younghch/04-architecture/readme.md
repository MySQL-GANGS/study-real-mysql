# MySQL Server Architecture

![MySQL Server Architecture](/individual/younghch/images/mysql-architecture.png)

## Parser

쿼리 파서는 사용자 요청으로 들어온 문장을 토큰으로 분리해 트리 형태의 구조로 만든다. 쿼리의 기본 문법 오류를 검증한다.

## Query Cache

SQL의 실행결과를 메모리에 캐시하고 동일 쿼리가 실행되면 즉시 결과를 반환한다. MySQL 8.0부터는 예전 버전에서 이 기능이 많은 버그를 발생시켰기에 삭제되었다.

## InnoDB Storage Engine
### What is a storage engine?
    
스토리지 엔진을은 실제 데이터를 디스크에 쓰거나 읽는 역할을 담당한다. 스토리지 엔진을 분리함으로써 필요에 따라 어플리케이션 환경에 특화된 스토리지 엔진을 선택할 수 있다. 
### Features of InnoDB

1. Clustered indexes

    InnoDB는 데이터를 프라이머리 키를 기준으로 클러스터링하고 정렬해 저장한다. 프라이머리 키가 없다면 유니크 키를 기준으로, 이마저도 없으면 GEN_CLUST_INDEX를 생성해 사용한다. 세컨더리 인덱스는 물리주소가 아닌 프라이머리 키 인덱스를 논리 주소로 사용한다. 그렇기에 프라이머리 키의 길이가 길면 세컨더리 인덱스의 길이도 길어져 더 많은 데이터를 사용하게 될 수 있다.
    
    Clustered index를 이용한 검색이 물리데이터 주소를 가진 페이지로 이어지기에 검색이 빠르다. 클러스터드 인덱스를 사용하지 않을 경우 검색조건은 물리데이터 주소가 여러 페이지에 나뉘어져 더 많은 디스크 I/O를 발생시킬 것이다. 

1. Multi Version Concurrency Control

    트랜젝션에서 일관된 읽기를 위해 MVCC를 지원한다. 한 트랜젝션에서 데이터를 변경한 경우 버퍼 풀에 해당 내용을 반영하고 변경전 내용을 언두로그에 기록한다. 트렌젝션이 커밋되기 전까지 다른 트렌젝션에서는 언두로그의 데이터를 읽는다. 롤벡을 실행하면 언두영역에 있는 데이터를 버퍼풀로 다시 복구한다. 트렌젝션이 길어지면 언두 로그에 데이터가 계속 쌓이는 문제가 발생할 수 있다. 

1. Automated Deadlock monitoring

    데드락 감지 스레드가 주기적으로 Wait-for List를 검사해 교착 상태에 빠진 트렌젝션을 찾아 종료한다. 트렌젝션의 언두 로그가 더 적은 스레드를 강제 종료 후 롤벡한다. 

1. Other features
    - can use foreign key
    - force recovery with `innocb_force_recovery` option
### In-Memory Structures

![innodb-memory-architecture](/individual/younghch/images/innodb-memory-architecture.png)

1. Buffer pool

    디스크의 데이터나 인덱스 정보를 메모리에 캐시해주거나 쓰기 작업을 모아주는 버퍼 역할을 한다. `innodb_buffer_pool_size` 시스템 변수로 크기를 설정할 수 있으며, 동적으로 확장이 가능하다. 데이터 접근시 락을 사용하며 `innodb_buffer_pool_instances` 시스템 변수를 이용해 버퍼풀을 여러개로 분할해 동시성을 늘릴 수 있다.

    버퍼풀은 `innodb_page_size`크기의 페이지로 이루어진 linked list 구조를 가진다. MySQL tuning을 위해서는 버퍼풀이 어떻게 자주 사용되는 데이터를 관리하는지 이해해야한다. 버퍼풀은 new sublist와 old sublist로 리스트를 나누고 LRU 알고리즘을 사용한다. 

    ![innodb-buffer-pool-list](/individual/younghch/images/innodb-buffer-pool-list.png)

    **작동 방식**
    - 새로운 페이지는 old sublist의 헤드인 midpoint에 삽입된다.
    - old sublist에 있는 데이터를 읽으면 해당 데이터는 new sublist의 헤드로 옮겨진다.
    - 새로운 페이지를 추가해야하는데 메모리에 공간이 없으면 old sublist의 끝에 있는 데이터를 삭제한다.

1. Change Buffer

    ![innodb-change-buffer.png](/individual/younghch/images/innodb-change-buffer.png)

    버퍼풀에 존재하지 않는 secondary index 페이지의 변경이 있을 시 이를 즉시 실행하지 않고 사용자에게 결과를 바로 반환 후 이 변경사항을 저장하는 공간을 change buffer라 한다. 체인지 버퍼에 임시로 저장된 데이터는 머지 스레드에 의해 백그라운드에서 반영된다.

1. Adaptive Hash Index

    어댑티브 해시 인덱스는 InnoDB 스토리지 엔진에서 사용자가 자주 요청하는 데이터에 대해 자동으로 생성하는 인덱스이다. 자주 사용되는 인덱스를 해시로 저장해 B-Tree 인덱스의 불필요한 검색시간을 줄이는 역할을 한다.

    **성능 향상에 도움이 되는 경우**
    - 동등 조건 검색(in, =)이 많은 경우
    - 쿼리가 데이터 일부에 집중되는 경우
    - 디스크 데이터와 버퍼 풀 크기가 비슷한 경우

    **성능 향상에 도움이 되지 않는 경우**
    - 디스크 읽기가 많은 경우
    - 특정 패턴의 쿼리가 많은 경우(JOIN, LIKE)
    - 매우 큰 데이터를 가진 테이블의 레코드를 폭넓게 읽는 경우

1. Log Buffer

    로그 버퍼는 로그 파일에 기록될 데이터를 저장하는 메모리이다. 주기적으로 버퍼의 데이터를 디스크로 기록한다. 로그 퍼버의 크기가 크다면 트렌젝션이 커밋하기전에 일어나는 로그 파일의 디스크 기록을 일일이 기다릴 필요가 없어 disk I/O를 줄일 수 있다.
### InnoDB On-Disk Structures

1. Redo Log

    리두 로그는 데이터의 변경을 기록해 비정상 상황에서 데이터 파일에 기록되지 못한 데이터를 잃지 않게 해주는 안전 장치이다. 데이터베이스 서버는 데이터 변경 내용을 쓰기에 최적화된 리두 로그로 먼저 기록한다. 버퍼풀의 쓰기 버퍼링 공간과 리두 로그는 밀접한 관계가 있다. 리두 로그 파일의 크기가 작다면 버퍼를 아무리 크게해도 쓰기 버퍼링에서 성능을 기대할 수 없다.
    
    디스크와 동기화 되지 않은 로그를 활성 리두 로그(active redo log)라고 한다. 데이터 변경이 일어나면 LSN(log sequence number)을 증가시키며 활성 리두 로그가 아닌 공간에 데이터를 기록한다. 데이터 베이서는 주기적으로 데이터를 동기화할때 마지막에 동기화한 LSN부터 마지막 LSN까지 동기화 해야한다. 이 차이를 체크포인트 에이지라고하며 크기의 최댓값은 리두 로그 파일의 크기이다. 그렇기에 리두로그 파일을 적절하게 설정하지 않으면 허용 가능한 더티 페이지의 수가 설정한 버퍼의 크기에서 기대한 수 보다 작을 수 있다.

1. Undo Log

    언두 로그는 트렌젝션에서 데이터를 변경한 내용을 되돌릴 수 있는 정보를 기록한다. 데이터가 한 트렌젝션에서 변경되었을 때 다른 트렌젝션에서는 언두로그를 통해 변경되기 전의 데이터를 읽을 수 있다.

1. Double Write Buffer

    버퍼 풀에서 데이터를 디스크로 플러시할 떄 발생하는 장애를 복구 할수 있도록 디스크에 쓰기전에 데이터를 double wirte 버퍼에 기록한다. 

    
## Threads

1. Foreground thread(Client thread)

    포어그라운드 스레드는 서버에 접속된 클라이언트마다 적어도 하나씩 존재하며 클라이언트가 요청하는 문장을 처리한다. 스레드 캐시(thread cache)를 사용해 스레드를 캐싱한다.

2. Background thread

    InnoDB의 백그라운드 스레드는 다음과 같은 작업을 담당한다.
    - 인서트 버퍼 병합
    - 로그를 디스크로 기록
    - 버퍼 풀 데이터를 디스크에 기록
    - 데이터를 버퍼로 읽음
    - 잠금이나 데드락 모니터링

## Plugins & Component

스토리지 엔진만 필요에따라 바꿀 수 있는게 아니다. 인증이나 전문 검색, 커넥션 제어 등과 관련된 기능을 플러그인이나 컴포넌트로 적용할 수 있다. 컴포넌트는 플러그인의 단점을 극복하기위해 MySQL 8.0에 추가된 기능이다.


### References
- RealMySQL 8.0 4단원
- [Overview of MySQL Storage Engine Architecture
](https://dev.mysql.com/doc/refman/8.0/en/pluggable-storage-overview.html)
- [Alternative Storage Engines
](https://dev.mysql.com/doc/refman/8.0/en/storage-engines.html)
- [The InnoDB Storage Engine](https://dev.mysql.com/doc/refman/8.0/en/innodb-storage-engine.html)