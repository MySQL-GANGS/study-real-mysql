# 5장 : 트랜잭션

## 다음 2주 간은 6-7장 학습하기

### 스프링의 @Transactional 기본값들은?

- Propgataion.REQUIRED
- Iso.defult

---

### 트랜잭션의 범위를 최소화 해야하는 이유는...?

아래는 본문에서 나온 내용들...
> 메일 전송, FTP파일 전송 작업, 네트워크를 통해 원격 서버와 통신 작업 -> DBMS의 트랜잭션 내에서 제거하는 것이 좋음.
>
> 트랜잭션은 외부 I/O를 들고있으면 안된다.

- 용훈 > 외부와 통신을 한다는 것은 하드웨어에 무거운 작업이기 때문에
- 주혁 > 용훈님과 동의. 트랜잭션이 길어지면 다른 커넥션에서 대기를 해야되고 대기시간도 비용이며 성능 저하로 이어진다.
- 치종 > 요청과 응답하는 시간이 오래걸린다.

---

### mysqldump

- mysqldump을 이용해서 백업을 수행한다면 옵션에 따라 MySQL 서버에서 어떤 잠금을 걸게 되는지 확인할 필요가 있다고 한다.

---

### MySQL의 이원화된 잠금 : MySQL 에는 잠금이 두 지점에서 발생한다.

- Innodb의 잠금 == 레코드 잠금
- MySQL의 잠금 == 레코드락, 넥스트키락, 글로벌 락 등...\(p.160)
- p.166 참고

--- 

### 레벨업 락 lock_escalation p.167

- 락을 특정 범위를 넘어서면 상위 단계\(넓은 범위)로 엔진이 자동으로 승격시켜준다.

---

### 갭락과 넥스트 키락?

- 갭락 > 렉코드와 레코드 싱의 간격에 새로운…

- 넥스트 키 락

```
REPEATABLE-READ
Tx start
Select *from users where age >= 100;
1명 조회

(다른 tx. Insert into age=103) >> 성공시 >> REPEATABLE-READ이 깨짐

Select *from users where age >100;
1명 조회

Txt finish;
```

--- 

### REPEATABLE-READ

- REPEATABLE-READ 격리 수준에서 binlog 와 관련한 트랜잭션의 대기가 있다고 하는데... next key lock, binlog 와 관련해서 데드락이 발생할 수 있다고함?

--- 

### 데이터 페이지의 단위는 무엇인가?

--- 
