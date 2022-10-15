
# MySQL Server Architecture


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

## Memory

1. Global memory

    글로벌 영역의 메모리 공간은 서버가 시작되면서 운영체제로부터 할당된다. 

## Storage Engine
![my sql architecture](/individual/younghch/images/mysql-architecture.png)
1. What is storage engine and why do we need it?
    
    스토리지 엔진을은 실제 데이터를 디스크에 쓰거나 읽는 역할을 담당한다. 스토리지 엔진을 분리함으로써 필요에 따라 어플리케이션 환경에 특화된 스토리지 엔진을 선택할 수 있다. 
    
    kernel I/O의 버퍼링과 캐싱만으로 대량의 디스크 작업을 효율적으로 처리하기에 충분하지 않기에 스토리지 엔진이 필요하다. 스토리지 엔진은 다음과 같은 기능을 제공한다.
    - 요청된 디스크 작업을 모아서 최적화해 I/O호출을 줄이거나 순차적인 데이터 처리를 가능하게한다.
    - 갑작스러운 장애 발생에도 일관성이 유지되고 복구가 가능하도록 만든다.
    - B-tree와 같은 효율적인 자료구조로 데이터를 저장할 수 있다.
    
    써놓고 보니 스토리지 엔진이 제공하는 기능이 DBMS를 사용하는 이유랑 같은 것 같다. MySql의 핵심은 스토리지 엔진이라고 해도 되지 않을까.

## Plugins & Component

    스토리지 엔진만 필요에따라 바꿀 수 있는게 아니다. 인증이나 전문 검색, 커넥션 제어 등과 관련된 기능을 플러그인이나 컴포넌트로 적용할 수 있다. 컴포넌트는 플러그인의 단점을 극복하기위해 MySQL 8.0에 추가된 기능이다.





### References
- RealMySQL 8.0 4단원
- [Overview of MySQL Storage Engine Architecture
](https://dev.mysql.com/doc/refman/8.0/en/pluggable-storage-overview.html)
- [Alternative Storage Engines
](https://dev.mysql.com/doc/refman/8.0/en/storage-engines.html)