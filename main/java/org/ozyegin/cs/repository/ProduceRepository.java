package org.ozyegin.cs.repository;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class ProduceRepository extends JdbcDaoSupport {
  private final RowMapper<Integer> intRowMapper = (resultSet, i) -> resultSet.getInt(1);
  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  public Integer produce(String company, int product, int capacity) {
    int id= Objects.requireNonNull(getJdbcTemplate())
            .queryForObject("INSERT INTO production (company,product_id,capacity) VALUES (?,?,?) returning produceId",
                    new Object[]{company,product, capacity},intRowMapper);

    return id;
  }



  public void delete(int produceId) throws Exception {
    if(Objects.requireNonNull(getJdbcTemplate()).update("Delete from production where produceId=?",
            produceId)!=1){
      throw new Exception("delete failed");
    };
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update("Delete from production");
  }
}