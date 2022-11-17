# Ch.5 반복문

## 내용정리

- 반복계의 한계에 대해 언급하고 있다.
- SQL 문은 내부적으로는 반복문을 사용하고 있는 포장계라고 한다.
- SQL 언어의 의도에 맞게 작성할 수록 더 RDBMS 기술에 더 잘 올라타고 활용할 수 있다는 생각이든다.
- 반북계의 단점
	- 포장계로 재작성 > 현실에서는 적용하기 다소 어려움
	- 각각의 SQL을 튜닝 > 대부분 index range 스캔 index 스캔을 활용하는데 얼마나 더 나아질 수 있는가?
	- 다중 처리 > 가능한지도 확인해봐야되고 한정적임
- 반복계의 장점
	- 실행 계획의 안정성 > 실행 계획이 단순해져서 변경될 확률이 낮다, 포장계는 상대적으로 복잡해서 변경의 여지가 있다.
	- 예상처리 시간의 정밀도 > 예측 가능하다는 것은 정밀하다고도 할 수 있다.
	- 트랜잭션 제어가 편리하다.
- 예제 실행해보기 : p175-176
	- 느낀점 : 윈도우 함수가 좋은 이유...?

> window 함수는 현재 행과 어떤 식으로든 관련이 있는 일련의 테이블 행에 대해 계산을 수행합니다. <br />
> 이는 집계 함수로 수행할 수 있는 계산 유형과 비슷합니다. <br />
> 그러나 일반 집계 함수와 달리 window 함수를 사용하면 행이 단일 출력 행으로 그룹화되지 않습니다.  <br />
> 행은 별도의 ID를 유지합니다. <br />
> 뒤에서 window 함수는 쿼리 결과의 현재 행 이상에 액세스할 수 있습니다.
> REF : https://mode.com/sql-tutorial/sql-window-functions/

## 16강 : SQL 에서는 반복을 어떻게 표현하는가..? p.175

### 1. 포인트는 CASE 식과 WINDOW 함수

- QueryDSL-test 로 작성 시도...
- max over partition by ... 는 SQLExpressions.max() 를 써보려고 했는데 실패
- 예제를 좀 내 방식대로 풀어서 ORDER BY this, that 해서 어플리케이션에서 이전 레코드 조회

### 2. 최대 반복횟수가 정해져 있는 경우 p

- 다시금 CASE 식과 WINDOW 함수
- 우편번호에서 가장 가까운 곳 찾기... RANK와
- 테이블 크기가 크다면 테이블 풀 스캔을 줄이는 것의 효과가 더 크다.

### 3. 반복 횟수가 정해지지 않은 경우

- 예시 주제는 이사 기록 `기존 주소 > 최신 주소`
	- 주소가 NULL 이면 가장 최신 이사기록
	- 재귀적으로 역추적하면서 첫 주소를 확인하려는 것인데...
	- `WITH RECURSIVE`가 표준 SQL의 일부라는 것에 놀랐다.

### `WITH RECURSIVE` 의 대안 p. 194

- 이는 최근에 만들어진 기능이라 구현이 안되어있거나 실행계획이 최적화 되지 않은 DBMS 들이있다.
- 그래서 다음 3가지 방법을 제안하는데

1. 인접 리스트 모델 : DFS 알고리즘
2. 중첩 집합 모델 : 새로운 데이터가 이전의 데이터 안에 포함된다고 보면된다.
3. 경로 열거 모델 : 갱신이 거의 발생하지 않을 경우라서 예시가 없다?

## 17강 : 바이어스의 공죄 p.198

- 저자는 어플리케이션은 절차 지향적으로 수행된다고 한다.
- SQL의 이념은 절차적 계층을 은폐하는 것이 SQL의 이념이다.

## 요약

- ***반복계는 튜닝 가능한 부분이 거의 없음 == 인덱스 활용 여부를 제외하면...
- 예제를 읽어보니 윈도우 함수가 있기 전에는 상관 서브 쿼리가 정석이었다고 한다.