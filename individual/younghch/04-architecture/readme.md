# 쿼리의 실행 순서

쿼리실행 -> 파서 -> 옵티마이저 -> API -> Storage Engine
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
    - foreign key is available
    - force recovery with `innocb_force_recovery` option
### In-Memory Structures
![innodb-memory-architecture](/individual/younghch/images/innodb-memory-architecture.png)

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