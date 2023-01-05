package org.ozyegin.cs.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import javax.sql.DataSource;
import org.ozyegin.cs.entity.Company;
import org.ozyegin.cs.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

@Repository
public class CompanyRepository extends JdbcDaoSupport {
  final int batchSize = 10;
  final String createPS = "INSERT INTO company (name,country,city,streetInfo,phoneNumber) VALUES (?,?,?)" ;
  final String getPS= "SELECT * FROM company WHERE   IN ()";
  final String getAllPS= "SELECT * FROM company";
  final String getSinglePS= "SELECT * FROM company WHERE =?";
  final String deleteAllPS= "DELETE FROM company";
  final String deleteAllEmailsPS= "DELETE FROM emails";
  final String deleteAllZipCityPS="DELETE FROM zip_city";
  final String deletePS="DELETE FROM company WHERE name=?";
  final String deleteEmailsPS="DELETE FROM emails WHERE name=?";
  final String getNamePS="SELECT * FROM company WHERE name=?";

  final String selectNamePS="SELECT name FROM company WHERE country=?";

  final String getCountryPS= "SELECT * FROM company WHERE country=?";
  private final RowMapper<String> stringRowMapper = (resultSet, i) -> resultSet.getString(1);


  @Autowired
  public void setDatasource(DataSource dataSource) {
    super.setDataSource(dataSource);
  }


  private final RowMapper<Company> CompanyRowMapper = (resultSet, i) -> new Company()
          .Name(resultSet.getString("name"))
          .Phone(resultSet.getString("phone"))
          .Country(resultSet.getString("country"))
          .Street(resultSet.getString("street"))
          .Zip(resultSet.getInt("zip"));






  public Company find(String name) throws Exception {
    Company company = Objects.requireNonNull(getJdbcTemplate()).queryForObject(getNamePS,new Object[] {name}, CompanyRowMapper);
    company.setE_mails(Objects.requireNonNull(getJdbcTemplate()).query("SELECT email FROM emails WHERE name=?",
            new Object[] {company.getName()},stringRowMapper));
    String city = Objects.requireNonNull(getJdbcTemplate()).queryForObject("SELECT city FROM zip_city WHERE zip =?", new Object[] {company.getZip()}, stringRowMapper);
    company.setCity(city);
    return company;
  }


  public String create(Company company) throws Exception {
    List<String> e_mails=company.getE_mails();
    try {
      String city = Objects.requireNonNull(getJdbcTemplate()).queryForObject(
              "SELECT city FROM zip_city WHERE zip=?", new Object[]{company.getZip()}, stringRowMapper);
      if (!city.equals(company.getCity())) {
        throw new Exception("Not found");
      }
    }
    catch(EmptyResultDataAccessException e){
      Objects.requireNonNull(getJdbcTemplate()).update(
              "INSERT INTO zip_city (zip,city) VALUES(?,?)", company.getZip(),company.getCity());
    }
    Objects.requireNonNull(getJdbcTemplate())
            .update("INSERT INTO company (name,country,zip,street,phone) VALUES (?,?,?,?,?)",
                    company.getName(),company.getCountry(),company.getZip(),company.getStreetInfo(),company.getPhoneNumber());

    for(String email:company.getE_mails()){
      Objects.requireNonNull(getJdbcTemplate()).update(
              "INSERT INTO emails (name,email) VALUES (?,?)", company.getName(), email);

    }

    return company.getName();
  }






  public String delete(String name) {
    Objects.requireNonNull(getJdbcTemplate()).update(deleteEmailsPS,name);
    Objects.requireNonNull(getJdbcTemplate()).update(deletePS,name);
    return name;
  }



  public void deleteAll() {
    Objects.requireNonNull(getJdbcTemplate().update(deleteAllEmailsPS));
    Objects.requireNonNull(getJdbcTemplate().update(deleteAllPS));
    Objects.requireNonNull(getJdbcTemplate().update(deleteAllZipCityPS));
  }


  public List<Company> findByCountry(String country) throws Exception {
    List<String> names= Objects.requireNonNull(getJdbcTemplate())
            .query("Select name from company where country=?",new Object[]{country}, stringRowMapper);
    List<Company> result= new ArrayList<Company>();
    for(int i=0;i<names.size();i++){
      result.add(find(names.get(i)));
    }

    return result;
  }
}