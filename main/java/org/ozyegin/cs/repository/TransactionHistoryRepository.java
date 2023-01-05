package org.ozyegin.cs.repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.util.*;
import javax.sql.DataSource;

import com.google.common.hash.HashCode;
import org.ozyegin.cs.entity.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository

public class TransactionHistoryRepository extends JdbcDaoSupport {

  final String Query1="SELECT h1.key,h1.value FROM transaction_history h1,product_order p1 WHERE h1.key=p1.company AND h1.value=p1.pid AND p1.amount=(SELECT MAX(p2.amount) FROM product_order p2 WHERE h1.key=p2.company )";
  final String Query11=" SELECT h.key, h.value FROM transaction_history h GROUP BY h.key, h.value  HAVING SUM(h.amount)>(SELECT SUM(h1.amount)  FROM transaction_history h1 WHERE h.key=h1.key AND h.value<>h1.value)";
  final String Query2="SELECT  c.name  FROM transaction_history h ,company c  GROUP BY  c.name EXCEPT SELECT  h1.key FROM transaction_history h1, company c1   WHERE  c1.name=h1.key AND h1.order_date >= ? and h1.order_date <= ?  GROUP BY  h1.key";
  private final RowMapper<Pair> pairMapper = (resultSet, i) -> new Pair(
          resultSet.getString(1),
          resultSet.getInt(2)
  );

  private final RowMapper<String> stringMapper = (resultSet, i) -> resultSet.getString(1);

  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public List<Pair> query1() {

    List<Pair> pairs =  Objects.requireNonNull(getJdbcTemplate()).query(Query11,pairMapper);



    return pairs;
  }

  public List<String> query2(Date start, Date end) {
    List<String> companies = Objects.requireNonNull(getJdbcTemplate().query(Query2, new Object[]{start, end}, stringMapper));
    return companies;
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update("DELETE FROM transaction_history");
  }
}
