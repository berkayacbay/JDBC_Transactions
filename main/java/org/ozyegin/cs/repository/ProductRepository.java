package org.ozyegin.cs.repository;

import java.sql.Array;
import java.util.*;
import javax.sql.DataSource;
import org.ozyegin.cs.entity.Product;
import org.ozyegin.cs.entity.Sample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository

public class ProductRepository extends JdbcDaoSupport {

  final int batchSize = 10;
  final String createPS = "INSERT INTO product (name, description, brandName) VALUES(?,?,?)";
  final String updatePS = "UPDATE product SET name=?, description=?, brandName=? WHERE id=?";
  final String getPS = "SELECT * FROM product WHERE id IN (:ids)";
  final String getAllPS = "SELECT * FROM product";
  final String getSinglePS = "SELECT * FROM product WHERE id=?";
  final String deleteAllPS = "DELETE FROM product";
  final String deletePS = "DELETE FROM product WHERE id=?";
  final String getBrandNamePS= "SELECT * FROM product WHERE brandName=?";
  final String selectIdPS="SELECT id FROM product";


  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }

  private final RowMapper <Integer> intRowMapper=((resultSet, i) -> resultSet.getInt(1));

  private final RowMapper<Product> productRowMapper = (resultSet, i) -> new Product()
          .id(resultSet.getInt("id"))
          .name(resultSet.getString("name"))
          .description(resultSet.getString("description"))
          .brandName(resultSet.getString("brandName"));


  public Product find(int id) {
    return Objects.requireNonNull(getJdbcTemplate()).queryForObject
            (getSinglePS, new Object[]{id}, productRowMapper);

  }

  public List<Product> findMultiple(List<Integer> ids) {
    if (ids == null || ids.isEmpty()) {
      return new ArrayList<>();
    } else {

      Map<String, List<Integer>> params = new HashMap<>() {
        {
          this.put("ids", new ArrayList<>(ids));
        }
      };

      return new NamedParameterJdbcTemplate(Objects.requireNonNull(getJdbcTemplate())).query(getPS, params, productRowMapper );
    }


  }





  public List<Product> findByBrandName(String brandName)
  {
    return Objects.requireNonNull(getJdbcTemplate()).query(getBrandNamePS, new Object[]{brandName}, productRowMapper);
  }


  public List<Integer> create(List<Product> products) {
    List<Integer> productId= Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM product",intRowMapper);

    Objects.requireNonNull(getJdbcTemplate()).batchUpdate(createPS,products,batchSize,
            ((preparedStatement, product) -> {
              preparedStatement.setString(1,product.getName());
              preparedStatement.setString(2,product.getDescription());
              preparedStatement.setString(3,product.getBrandName());
            }));
    List<Integer>  newList= Objects.requireNonNull(getJdbcTemplate()).query("SELECT id FROM product",intRowMapper);
    newList.removeAll(productId);
    return newList;
  }

  public void update(List<Product> products) {
    Objects.requireNonNull(getJdbcTemplate()).batchUpdate(
            updatePS, products, batchSize,
            (ps, p) -> {
              ps.setString(1, p.getName());
              ps.setString(2, p.getDescription());
              ps.setString(3, p.getBrandName());
              ps.setInt(4, p.getId());
            });


  }


  public void delete(List<Integer> ids) {
    for(int i=0;i<ids.size();i++){
      Objects.requireNonNull(getJdbcTemplate()).update("Delete from product where id=?",ids.get(i));
    }
  }

  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteAllPS);

  }

}