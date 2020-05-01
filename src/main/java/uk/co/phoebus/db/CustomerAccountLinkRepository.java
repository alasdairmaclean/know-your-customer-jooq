package uk.co.phoebus.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.exception.KycRepositoryException;
import uk.co.phoebus.model.CustomerAccountLink;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class CustomerAccountLinkRepository {

    public static final String COLUMNS = "CUSTOMER_ID, ACCOUNT_ID";
    public static final String CUSTOMER_EXISTS_SQL = "SELECT 1 FROM CUSTOMER WHERE CUSTOMER_ID = :customerId";
    public static final String ACCOUNT_EXISTS_SQL = "SELECT 1 FROM ACCOUNT WHERE ACCOUNT_ID = :accountId";
    public static final String SAVE_SQL = "INSERT INTO CUSTOMER_ACCOUNT (" + COLUMNS + ") SELECT :customerId, :accountId WHERE EXISTS(" + CUSTOMER_EXISTS_SQL + ") AND EXISTS(" + ACCOUNT_EXISTS_SQL + ")";
    public static final String SELECT_SQL = "SELECT " + COLUMNS + " FROM CUSTOMER_ACCOUNT";

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public CustomerAccountLink save(CustomerAccountLink customerAccountLink) {
        Map<String, Object> params = paramsMap(customerAccountLink);
        int update = jdbcTemplate.update(SAVE_SQL, params);
        if (update != 1) {
            throw new KycRepositoryException("customerId or accountId does not exist");
        }
        return customerAccountLink;
    }

    public List<CustomerAccountLink> findByCustomerId(String customerId) {
        return findByExample(CustomerAccountLink.builder().customerId(customerId).build());
    }

    public List<CustomerAccountLink> findByAccountId(String accountId) {
        return findByExample(CustomerAccountLink.builder().accountId(accountId).build());
    }

    private List<CustomerAccountLink> findByExample(CustomerAccountLink searchModel) {
        Map<String, Object> params = paramsMap(searchModel);
        List<String> predicates = Lists.newArrayList(

        );
        if (params.get("customerId") != null) {
            predicates.add("CUSTOMER_ID = :customerId");
        }
        if (params.get("accountId") != null) {
            predicates.add("ACCOUNT_ID = :accountId");
        }
        String sql = SELECT_SQL + " WHERE " + Joiner.on(" AND ").join(predicates);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM CUSTOMER_ACCOUNT", Collections.emptyMap());
    }

    private Map<String, Object> paramsMap(CustomerAccountLink customer) {
        Map<String, Object> params = new HashMap<>();
        params.put("customerId", customer.getCustomerId());
        params.put("accountId", customer.getAccountId());
        return params;
    }

    @PostConstruct
    public void initTemplate() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public static final RowMapper<CustomerAccountLink> ROW_MAPPER = (rs, rowNum) -> CustomerAccountLink.builder()
            .customerId(rs.getString("CUSTOMER_ID"))
            .accountId(rs.getString("ACCOUNT_ID"))
            .build();

}
