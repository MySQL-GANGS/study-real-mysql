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

# Query Hints
## Index Hints
**인덱스 힌트는 ANSI표준이 아닌 MySQL에서만 사용가능하므로 되도록이면 사용하지 말자.**

**최적 실행 계획은 데이터의 성격에 따라 시시각각 변하므로 지금 인덱스를 사용하는게 좋은 계획이라도 나중에는 달라질 수 있다. 섣부른 최적화를 하지 말자**

**가장 훌륭한 최적화는 힌트가 필요한 쿼리를 서비스에서 없애거나 튜닝할 필요가 없게 데이터를 최소화 하거나 데이터 모델의 단순화로 힌트가 필요하지 않게 하는 것이다.** 
### STRAIGHT_JOIN
쿼리의 조인 순서를 `FROM`절에 사용된 순서대로 설정한다. 

*필요한 상황*
- 임시테이블과 일반 테이블의 조인
    
    옵티마이저가 적절한 조인 순서를 선택하므로 성능 저하가 있는 경우에만 사용한다. 일반적으로 임시 테이블을 드라이빙 테이블로 선정하는 것이 좋다. 조인 컬럼에 인덱스가 없는 경우에는 두 테이블 중 레코드 건수가 작은 쪽을 드라이빙 테이블로 선택하는 것이 좋다.

- 임시 테이블끼리 조인

    임시테이블은 항상 인덱스가 없기 때문에 크기가 작은 테이블을 드라이빙 테이블로 선택하는것이 좋다.

- 일반 테이블끼리 조인

    인덱스가 한쪽에만 있는 경우 인덱스가 없는 테이블을 드라이빙으로 이외의 경우에는 레코드 건수가 작은 쪽을 드라이빙 테이블로 선택하는 것이 좋다.

### USE / FORCE / IGNORE INDEX

*종류*
- USE INDEX

    특정 테이블의 인덱스를 사용하도록 권장한다. 대부분의 경우 옵티마이저는 사용자의 힌트를 채택하지만 사용을 보장하지는 않는다.

- FORCE INDEX

    USE INDEX와 동일한 기능을 하지만 옵티마이저에 미치는 영향이 더 강하다. 사용을 보장하지 않는것은 동일하다.

- IGNORE INDEX

    인덱스를 사용하지 못하도록 한다.

*용도*
- USE INDEX FOR JOIN
- USE INDEX FOR ORDER BY
- USE INDEX FOR GROUP BY

### SQL_CALC_FOUND_ROWS vs COUNT(*)
SQL_CALC_FOUND_ROWS힌트가 있는 경우 LIMIT에 명시된 만큼의 데이터를 찾아도 끝까지 검색을 수행한다. `SELECT FOUND_ROWS()`쿼리를 통해 전체 데이터 수를 알 수 있다. COUNT(*)는 커버링 인덱스 만으로 처리될 수 있는 반면 FOUND_ROWS는 모든 레코드를 읽어야한다. 페이징에 사용하지 말자.

## Optimizer Hints

### scope
- 인덱스
- 테이블
- 쿼리블록
- 글로벌

### hints
힌트|설명|영향 범위
---|--|-------
MAX_EXECUTION_TIME | 쿼리의 실행 시간 제한 | 글로벌
RESOURCE_GROUP | 쿼리 실행의 리소스 그룹 설정 | 글로벌
SET_VAR | 쿼리 실행을 위한 시스템 변수 제어 | 글로벌
SUBQUERY | 서브쿼리의 세미 조인 최적화 전략 제어 | 쿼리 블룩
BKA, NO_BKA | Batched Key Access 조인 사용 여부 제어 | 쿼리 블록, 테이블
BNL, NO_BNL | Block Nasted Loop 조인 사용 여부 제어 |쿼리 블록, 테이블
DERIVED_CONDITION_PUSHDOWN <br>NO_DERIVED_CONDITION_PUSHDOWN | 외부 쿼리의 조건을 서브쿼리로 옮기는 최적화 사용여부 제어 | 쿼리 블록, 테이블
HASH_JOIN,<br>NO_HASH_JOIN | 해시조인 사용여부 제어 | 쿼리블록, 테이블
JOIN_FIXED_ORDER | FROM 절에 명시된 테이블 순서대로 조인 | 쿼리블록
JOIN_ORDER | 힌트에 명시된 순서대로 조인 | 쿼리블록
JOIN_PREFIX | 힌트에 명시된 테이블을 드라이빙 테이블로 선택 | 쿼리블록
JOIN_SUFFIX | 힌트에 명시된 테이블을 드리븐 테비을로 선택 | 쿼리블록
QB_NAME | 쿼리블록의 이름을 설정
SEMIJOIN,<br>NO_SEMIJOIN | 서브쿼리의 세미 조인 최적화 전략 제어<br>(DUPSWEEDOUT, FIRSTMATCH, LOOSESCAN, MATERIALIZATION) | 쿼리블록
MERFE, NO_MERGE | FROM절의 서브쿼리나 뷰를 외부 쿼리 블록으로 병합하는 최적화 수행 여부 선택 | 테이블
INDEX_MERGE, NO_INDEX_MERGE | 인덱스 병합 실행 계획 사용 여부 선택 | 테이블, 인덱스
MRR, NO_MRR | Multi-Range Read 사용 여부 제어 | 테이블, 인덱스
NO_ICP | index condition pushdown 사용 여부 제어 | 테이블, 인덱스
NO_RANGE_OPTIMIZATION | 특정 인덱스를 사용하지 못하도록 하거나 풀테이블 스캔 방식으로 쿼리 처리 | 테이블, 인덱스
SKIP_SCAN,<br> NO_SKIP_SCAN  | 인덱스 스킵 스캔 사용 여부 선택 | 테이블, 인덱스
INDEX, NO_INDEX | GROUP BY, ORDER BY, WHERE 절의 처리를 위한 인덱스 사용 여부 제어 | 인덱스
