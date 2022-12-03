# 데이터 암호화
---

MySQL 5.7버전부터 지원.

* MySQL 5.7 : 데이터 파일(테이블스페이스) 암호화
* MySQL 8.0 : 리두 로그, 언두 로그, 복제를 위한 바이너리 로그 등도 모두 암호화.

### MySQL 서버의 데이터 암호화
---
데이터베이스 서버와 디스크 사이의 데이터 읽고 쓰기 지점에서 암호화 또는 복호화를 수행.
* 데이터 암호화 기능이 활성화되어 있다고 하더라고 MySQL 내부와 사용자 입장에서는 아무런 차이가 없기 때문에 이러한 암호화 방식을 가리켜 TDE(Transparent Data Encryption)이라고 한다. 

TDE에서 암호화키는 KeyRing 플러그인에 의해 관리됨.
* keyring_file File-Based 플러그인
* keyring_encrypted_file Keyring 플러그인
* keyring_okb KMIP 플러그인
* keyring_aws Amazon Web Services Keyring 플러그인
* 모두 2단계(2-Tier)키 관리 방식을 사용한다. 

데이터를 읽을 때 복호화하여 InnoDB의 버퍼 풀에 적재된다. 적재된 데이터는 암호화되지 않은 테이블과 동일한 성능.

적재되지 않은 데이터를 읽을 때 복호화 과정을 거치므로 당연히 쿼리 처리가 지연된다. 

압축과 암호화가 동시에 진행되면 압축을 먼저 한다. 암호화한 결과는 랜덤한 바이트의 배열이 나오기 때문에 이는 압축률을 상당히 떨어뜨리기 때문.

### keyring_file 플러그인 설치
---
keyring_file 플러그인은 테이블 스페이스 키를 암호화하기 위한 마스터 키를 디스크의 파일로 관리하는데, 이때 마스터 키는 평문으로 디스크에 저장된다. 

설치방법
* TDE 플러그인의 경우 MySQL 서버가 시작되는 단계에서 가장 빨리 초기화 되기 때문에 my.cnf에서 early-plugin-load 시스템 변수에 keyring_file 플러그인을 위한 라이브러리("keyring_file.so")를 명시하면 된다. 
* keyring_file 플러그인이 마스터 키를 저장할 키링 파일의 경로를 keyring_file_data 설정에 명시하면 된다. 
* keyring_file_data설정 경로 : MySQL 서버 = 1:1
* MySQL 서버의 설정 파일이 준비되면 MySQL 서버를 재시작. 
* SHOW PLUGINS;
* 데이터 암호화 기능을 사용하는 테이블을 생성하거나 마스터 로테이션을 실행하면 키링 파일의 마스터 키가 초기화.

### 테이블 암호화
---
TDE를 이용하는 테이블 생성 방법
```MySQL
CREATE TABLE tab_encrypted(
    id INT,
    data VARCHAR(100),
    PRIMARY KEY(id)
) ENCRYPTION='Y';

INSERT INTO tab_encrypted VALUES(1, 'test_data');

SELECT * FROM tab_encrypted;
```

암호화된 테이블만 검색하기
```MySQL
SELECT table_schema, table_name, create_options
  FROM information_schema.tables
 WHERE table_name='tab_encrypted';
 ```

 응용프로그램에서 암호화된 칼럼은 인덱스를 생성하더라도 인덱스의 기능을 100%활용할 수 없다. 

 응용프로그램의 암호화와 MySQL서버의 암호화 기능 중 선택해야 하는 상황이라면 MySQL 서버의 암호화 기능을 선택할 것을 권장.

 MySQL 서버의 암호화 기능과 혼합해서 사용한다면 더 안전한 서비스 구축가능.

 ### 언두 로그 및 리두 로그 암호화
 ---
 테이블 암호화를 적용해도 리두 로그나 언두 로그, 그리고 복제를 위한 바이너리 로그에는 평문으로 저장된다. 

MySQL 8.0.16버전부터 innodb_undo_log_encrypt 시스템 변수와 innodb_redo_log_encrypt시스템 변수를 이용해 InnoDB 스토리지 엔진의 리두 로그와 언두 로그를 암호화된 상태로 저장할 수 있게 개선됐다.

### 바이너리 로그 암호화
---
바이너리 로그는 의도적으로 상당히 긴 시간 동안 보관하는 서비스도 있고 때로는 증분백업(Incremental Backup)을 위해 바이너리 로그를 보관하기도 한다. 
