package uk.co.phoebus.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.model.Customer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Date;
import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
public class CustomerRepository {

    public static final String SAVE_SQL = "INSERT INTO CUSTOMER (CUSTOMER_ID, DATE_OF_BIRTH, FORENAME, SURNAME) VALUES (:customerId, :dateOfBirth, :forename, :surname)";
    public static final String SELECT_SQL = "SELECT c.CUSTOMER_ID, c.DATE_OF_BIRTH, c.FORENAME, c.SURNAME FROM CUSTOMER c";

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public Customer save(Customer customer) {
        Map<String, Object> params = paramsMap(customer);
        jdbcTemplate.update(SAVE_SQL, params);
        return customer;
    }

    public Optional<Customer> findById(String customerId) {
        List<Customer> results = findByExample(Customer.builder().customerId(customerId).build());
        return results.stream().findFirst();
    }

    public List<Customer> findByExample(Customer searchModel) {
        Map<String, Object> params = paramsMap(searchModel);
        List<String> predicates = Lists.newArrayList();
        String optionalJoin = "";
        if (params.get("customerId") != null) {
            predicates.add("CUSTOMER_ID = :customerId");
        }
        if (params.get("dateOfBirth") != null) {
            predicates.add("DATE_OF_BIRTH = :dateOfBirth");
        }
        if (params.get("forename") != null) {
            predicates.add("FORENAME = :forename");
        }
        if (params.get("surname") != null) {
            predicates.add("SURNAME = :surname");
        }
        if (params.get("accountIds") != null) {
            optionalJoin = " INNER JOIN CUSTOMER_ACCOUNT ca ON c.CUSTOMER_ID = ca.CUSTOMER_ID";
            predicates.add("ca.ACCOUNT_ID IN (:accountIds)");
        }

        String sql = SELECT_SQL + optionalJoin + " WHERE " + Joiner.on(" AND ").join(predicates);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM CUSTOMER", Collections.emptyMap());
    }

    private Map<String, Object> paramsMap(Customer customer) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customer.getCustomerId());
        params.put("dateOfBirth", customer.getDateOfBirth() == null ? null : Date.valueOf(customer.getDateOfBirth()));
        params.put("forename", customer.getForename());
        params.put("surname", customer.getSurname());
        params.put("accountIds", isEmpty(customer.getAccountIds()) ? null : customer.getAccountIds());
        return params;
    }

    @PostConstruct
    public void initTemplate() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public static final RowMapper<Customer> ROW_MAPPER = (rs, rowNum) -> Customer.builder()
            .customerId(rs.getString("CUSTOMER_ID"))
            .forename(rs.getString("FORENAME"))
            .surname(rs.getString("SURNAME"))
            .dateOfBirth(rs.getDate("DATE_OF_BIRTH").toLocalDate())
            .build();

}
