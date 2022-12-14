# 04. 아키텍쳐

1. MySQL Replication을 이용해 다른 서버에 db를 실시간으로 복사한다면 RAID-10대신 RAID-0나 RAID-5를 사용해도될까요?

> 추후 Real_MySQL 복제 관련 챕터에서 자세히 보기로

2. MySQL 8.0은 이전 버전에서 query cache를 사용했을때의 버그로 인해 query cache를 사용하지 않는다고 합니다. 성능 향상을 위해 서버 메모리에 개발자가 자체적으로 db의 내용을 캐싱하고자
	 하면 어떤 점을 주의해야하고 db데이터 변경이 있을 때 어떤 조치를 취해야할까요?

> 용훈님이 제시한 관련 면접 질문 사례 분석하기 참고하기

3. InnoDB가 데이터 InnoDB가 데이터를 프라이머리 키로 정렬해서 저장한다면 데이터가 추가될 때마다 물리데이터의 정렬이 일어나는걸까요?

> 그렇지 않는다면 InnoDB 의 프라이머리 키 기준의 동작은 있는지? 있으면 어떻게 동작하는지를 설명하시오
> p.99 에 언급한 “쿼리의 실행계획에서 다른 보조 인덱스보다 프라이머리 키로 처리될 확률이 높다.” 부분에서 이야기하는 확률은 통계적으로 옵티파이저가 프라이머리 키 사용을 더 자주 한다는 것을 의미한다.

4. 프라이머리 키에 대한 클러스터링 추가 공부하기. \(장,단점은? 이루고자 하는 것?)

> - 클러스터링 이란 여러 개를 하나로 묶는 것
> - MySQL 에서 클러스터링은 테이블의 레코드를 비슷한 것들\(프라이머리 키) 끼리 묶어서 저장하는 형태
		>
- 이는 비슷한 값들을 \(range 조회 등..) 동시에 조회하는 것과 같은 원리라서
> - MySQL 에서 클러스터링 인덱스
		>
- InnoDB만 지원
>   - 프라이머리 키에만 적용
>   - 프라이머리 키 값에 의해 레코드 저장 위치가 결정된다. 그래서 PK 변경시 저장 위치도 변경이 필요함
>   - 인덱스 알고리즘이라기 보다는 테이블 레코드의 저장 방식이라고 볼 수 있음
> - InnoDB와 클러스터링 인덱스
		>
- PK 기반의 클러스터링 인덱스는 리프노드에 실제 레코드 값들을 가지고 있다.
>   - 세컨더리 인덱스는 리프노드에 프라이머리 키 값을 저장하도록 구현되어 있다.
>   - InnoDB에서 PK 로 검색시 매우 빠르다.
> - p. 270 ~p.275

6. 어댑티브 해시 인덱스는 스토리지 엔진 단위로 존재한다고 하고 스토리지 엔진은 테이블 단위로 스토리지 엔진 선택이 가능한데… 그러면 어댑티브 해시 인덱스는 테이블 마다 있는 건지?

7. MySQL 8.0은 이전 버전에서 query cache를 사용했을때의 버그로 인해 query cache를 사용하지 않는다고 합니다. 성능 향상을 위해 서버 메모리에 개발자가 자체적으로 db의 내용을 캐싱하고자
	 하면 어떤 점을 주의해야하고 db데이터 변경이 있을 때 어떤 조치를 취해야할까요?

8. 데이터베이스의 Random I/O 와 Sequential I/O 는 어떻게 다르고 HDD 와 SSD에 따라 어떤 차이가 있을지?
