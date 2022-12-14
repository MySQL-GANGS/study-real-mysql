//JDBC 트랜잭션에 대한 이해 ( https://www.marcobehler.com/guides/spring-transaction-management-transactional-in-depth?fbclid=IwAR1PsHPKHyLGmiKORaTsvXV6EwIwe5f2RTCkz52QLZFnDdI7QzArXLil4PQ )


//1. JDBC가 트랜잭션을 다루는 코드
//----------------------------------------------------------------------------------------------------------------------------
import java.sql.Connection;

Connection connection = dataSource.getConnection(); // (1) dataSource 인스턴스 생성

try (connection) {
    connection.setAutoCommit(false); // (2) setAutoCommit(true)가 기본값이므로 따로 설정을 해주지 않을시에는 트랜잭션 제어 불가능
    // execute some SQL statements...
    connection.commit(); // (3) 문제없으면 commit

} catch (SQLException e) {
    connection.rollback(); // (4) 문제생기면 rollback
}

//----------------------------------------------------------------------------------------------------------------------------


//2. Spring에서의 @Transactional annotaion
//----------------------------------------------------------------------------------------------------------------------------
public class UserService {

    public Long registerUser(User user) {
        Connection connection = dataSource.getConnection(); // (1) Connection을 가져오고
        try (connection) {
            connection.setAutoCommit(false); // (1) setAutoCommit(false)

            // execute some SQL that e.g.
            // inserts the user into the db and retrieves the autogenerated id
            // userDao.save(user);

            connection.commit(); // (1) 문제없으면 commit
        } catch (SQLException e) {
            connection.rollback(); // (1) 문제생기면 rollback
        }
    }
}


public class UserService {

    @Transactional //@Transactional annotaion을 이용하여 작성
    public Long registerUser(User user) {
        // execute some SQL that e.g.
        // inserts the user into the db and retrieves the autogenerated id
        // userDao.save(user);
        return id;
    }
}
//----------------------------------------------------------------------------------------------------------------------------


//3. @Transactional Isolation Level
//----------------------------------------------------------------------------------------------------------------------------
connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ); // 격리수준 설정에 대한 동작방식 예제
@Transactional(isolation = Isolation.REPEATABLE_READ) // @Transactional annotaion에서의 격리수준 설정 예제


//Q. Isolation Level은 DB의 동작방식에 따라야 하는가 ??? 아니면 Sevice의 동작방식을 설정할 수 있을까 ?? 

//----------------------------------------------------------------------------------------------------------------------------


//4. @Transcation Propagation Levels
//----------------------------------------------------------------------------------------------------------------------------
//트랜잭션 사용시에 아래 둘 중 하나 설정
  @Transactional(propagation = Propagation.REQUIRED)
  // or
  @Transactional(propagation = Propagation.REQUIRES_NEW)

//(1) Required (default): My method needs a transaction, either open one for me or use an existing one → getConnection(). setAutocommit(false). commit().

//(2) Require_new: I want my completely own transaction → getConnection(). setAutocommit(false). commit().

//(3) Supports: I don’t really care if a transaction is open or not, i can work either way → nothing to do with JDBC

//(4) Mandatory: I’m not going to open up a transaction myself, but I’m going to cry if no one else opened one up → nothing to do with JDBC

//(5) Not_Supported: I really don’t like transactions, I will even try and suspend a current, running transaction → nothing to do with JDBC

//(6) Never: I’m going to cry if someone else started up a transaction → nothing to do with JDBC

//(7) Nested: It sounds so complicated, but we are just talking savepoints! → connection.setSavepoint()
//----------------------------------------------------------------------------------------------------------------------------