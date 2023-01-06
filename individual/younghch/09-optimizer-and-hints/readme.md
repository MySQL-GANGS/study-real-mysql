# Optimizer
옵티마이저는 파스트리를 분석해 최적화 및 실행 계획을 수행한다.
## role
- 불필요한 조건 제거 및 복잡한 연산의 단순화
- 테이블의 조인이 있을시 읽기 순서 결정
- 사용할 인덱스 결정
- 임시 테이블 사용여부 결정

## rule-based optimizer vs cost-based optimizer
- 비용 기반 최적화(RBO)는 여러 경우의 쿼리 처리 방법을 만들고 각 작업의 비용과 테이블의 통계정보를 이용해 실행 계획별 비용을 산출하고 최소 비용을 선택한다.
- 규칙 기반 최적화(CBO)는 내장된 규칙에 따라 실행계획을 수립한다. 같은 쿼리에 대해 항상 같은 실행 계획을 얻을 수 있다. 비용 기반 최적화의 cpu 연산이 부담스러웠던 과거에 사용되었고 현재는 거의 사용되지 않는다.


# Data Processing
## ORDER BY
### file sort
ORDER BY 절을 사용할 때 인덱스를 사용할 수 있는 경우에는 순서대로 읽기만 하면된다. 인덱스가 없는경우 filesort를 이용해 별도의 정렬 처리를 해야한다.

*sort buffer*

정렬시 sort_buffer_size 시스템 변수로 정한 크기의 sort buffer를 할당받는다. 데이터의 크기가 버퍼보다 크면 디스크 공간을 사용해 임시 파일을 생성하고 병합하며 정렬을 수행해야해 성능이 떨어진다. sort buffer는 공유되지 않으며 트렌젝션마다 할당받는다. sort_buffer_size가 너무 큰경우(10MB이상) 메모리 여유 부족으로 서버가 강제종료될 수 있다.

*sort mode*

MySQL서버의 정렬 방식은 세가지가 있다.(INFORMATION_SCHEMA.OPTIMIZER_TRACE에서 확인 가능)
1. <sort_key, rowid>

    정렬키와 로우 아이디만 가져와서 정렬한다.

2. <sort_key, additional_fields>

    소트 버퍼에 정렬 키와 레코드 전체를 가져와서 정렬한다. 정렬이 완료되면 버퍼의 내용을 그대로 클라이언트에 넘겨준다. 정렬 대상 레코드의 크기나 건수가 작은 경우 효율적이다.

3. <sort_key, packed_additional_fields>

    정렬 키와 정렬 대상 컬럼과 프라이머리키만 가져와서 정렬한다. 정렬이 완료되면 프라이머리 키를 이용해 테이블을 한번 더 읽은 결과를 클라이언트에 반환한다. 대상 레코드의 크기나 건수가 상당한 경우 효율적이다.

select문에서 필요한 컬럼만 조회하지 않는 경우 sort buffer를 비효율적으로 사용하게 된다.

### EXPLAIN query result ORDER BY

정렬 처리 방법 | 실행 계획 extra 컬럼  | 조건|
------------|-------------------| ---|
인덱스 사용 | 별도 표기 없음 | 인덱스가 걸린 컬럼이 정렬 조건이며 드라이빙 테이블에 있다.<br>정렬 조건이 where절에 포함되면 order by와 where 조건 컬럼은 같은 인덱스를 사용할 수 있어야한다. 
조인에서 드라이빙 테이블만 정렬 | Using filesort | 드라이빙 테이블의 컬럼만으로 order by절이 작성되어있어 첫번째 정렬 후 조인하는 것이 빠르다. 
조인에서 조인 결과를 임시 테이블에 저장 후 정렬 | Using temprorary; Using file sort | order by 절이 드리븐 테이블의 컬럼에 걸려있다.

## GROUP BY

**GROUP BY 에 사용된 HAVING 조건은 인덱스를 사용하지 못한다. HAVING절을 튜닝하려 하지말자**

### EXPLAIN query result of GROUP BY

정렬 처리 방법 | 실행 계획 extra 컬럼  | 조건|
------------|-------------------| ---|
tight index scan | 별도 표기 없음 | 조인의 드라이빙 테이블에 있는 컬럼으로만 그루핑이 이뤄지고 인덱스가 걸려있다.
loose index scan | Using index for group by | 드라이빙 테이블의 인덱스 전체로 그루핑이 이뤄지지 않지만 인덱스를 일부 사용할 수 있다.(앞쪽)
temporary table | Using temporary | 인덱스를 전혀 사용하지 못해 테이블을 풀스캔한다.

### DISTINCT
집계함수가 없는 경우 GROUP BY와 동일하게 동작한다.
**집계함수가 사용되지 않은 경오 특정 컬럼에만 DISTINCT를 사용할 수 없고 조회된 컬럼의 유니크한 조합을 가져온다. `SELECT DISTINCT(col1), col2 FROM table`은 `SELECT DISTINCT col1, col2 FROM table`과 같다.**
집계함수가 있는 경우 경우 임시 테이블을 이용해 처리된다.  

## Temporary Table