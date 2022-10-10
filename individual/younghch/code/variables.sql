# SESSION, GLOBAL scope test
SHOW GLOBAL VARIABLES LIKE 'join_buffer_size'; #262144
SHOW VARIABLES LIKE 'join_buffer_size'; #262144
SET GLOBAL join_buffer_size = 524288;
SHOW GLOBAL VARIABLES LIKE 'join_buffer_size'; #524288
SHOW VARIABLES LIKE 'join_buffer_size'; #262144

# save data permanently on config file
SET PERSIST max_connections = 5000; 
SHOW GLOBAL VARIABLES LIKE 'max_connections';
/* 
cat /local/var/mysql/mysqld-auto.cnf
mysql_dynamic_parse_early_variables": {"max_connections": {"Value": "5000"
*/


# Change the config but not applied on current server
SET PERSIST_ONLY max_connections = 2000;
SHOW GLOBAL VARIABLES LIKE 'max_connections';

# PERSIST_ONLY can change non dynamic variable
SET PERSIST innodb_doublewrite = ON; # error
SET PERSIST_ONLY innodb_doublewrite = ON; # OK

# persisted variables
SELECT
	a.variable_name
    ,b.variable_name
    ,a.set_time
    ,a.set_user
    ,a.set_host
FROM
	performance_schema.variables_info a
INNER JOIN
	performance_schema.persisted_variables b
	ON
		a.variable_name = b.variable_name
WHERE
	b.variable_name LIKE 'max_connections';
    
# reset
RESET PERSIST max_connections;
RESET PERSIST;

