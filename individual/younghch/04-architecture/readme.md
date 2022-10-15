# MySQL Server Architecture
![my sql architecture](/individual/younghch/images/mysql-architecture.png)

## MySql Engine

1. What is MySql Engine?

    공식 문서에는 MySql Engine이라는 표현이 없다. 책에서 설명의 편의를 위해 만들어낸 개념인 듯 하다. 커넥션 헨들러, SQL파서, 전처리기, 옵티마이저를 모아 MySql엔진으로 칭한다.

## Storage Engine

1. What is storage engine and why do we need it?
    
    - mysql documentation
    
        스토리지 엔진을은 실제 데이터를 디스크에 쓰거나 읽는 역할을 담당한다. 스토리지 엔진을 분리함으로써 필요에 따라 어플리케이션 환경에 특화된 스토리지 엔진을 선택할 수 있다. 
    
    - what I understood
        
        mysql도 os위에서 돈다. kernel I/O의 버퍼링과 캐싱만으로 대량의 디스크 작업을 효율적으로 처리하기에 충분하지 않기에 스토리지 엔진이 필요하다. 스토리지 엔진은 다음과 같은 기능을 제공한다.
        - 요청된 디스크 작업을 모아서 최적화해 I/O호출을 줄이거나 순차적인 데이터 처리를 가능하게한다.
        - 갑작스러운 장애 발생에도 일관성이 유지되고 복구가 가능하도록 만든다.
        - B-tree와 같은 효율적인 자료구조로 데이터를 저장할 수 있다.





### References
- RealMySQL 8.0 4단원
- [Overview of MySQL Storage Engine Architecture
](https://dev.mysql.com/doc/refman/8.0/en/pluggable-storage-overview.html)
- [Alternative Storage Engines
](https://dev.mysql.com/doc/refman/8.0/en/storage-engines.html)