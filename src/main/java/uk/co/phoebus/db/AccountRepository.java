package uk.co.phoebus.db;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.model.Account;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
public class AccountRepository {

    public static final String SAVE_SQL = "INSERT INTO ACCOUNT (ACCOUNT_ID, ACCOUNT_NUMBER) VALUES (:accountId, :accountNumber)";
    public static final String SELECT_SQL = "SELECT a.ACCOUNT_ID, a.ACCOUNT_NUMBER FROM ACCOUNT a";

    @Autowired
    private DataSource dataSource;

    private NamedParameterJdbcTemplate jdbcTemplate;

    public Account save(Account account) {
        Map<String, Object> params = paramsMap(account);
        jdbcTemplate.update(SAVE_SQL, params);
        return account;
    }

    public Optional<Account> findById(String accountId) {
        List<Account> results = findByExample(Account.builder().accountId(accountId).build());
        return results.stream().findFirst();
    }

    public List<Account> findByExample(Account searchModel) {
        Map<String, Object> params = paramsMap(searchModel);
        List<String> predicates = Lists.newArrayList();
        String optionalJoin = "";
        if (params.get("accountId") != null) {
            predicates.add("ACCOUNT_ID = :accountId");
        }
        if (params.get("accountNumber") != null) {
            predicates.add("ACCOUNT_NUMBER = :accountNumber");
        }
        if (params.get("customerIds") != null) {
            optionalJoin = " INNER JOIN CUSTOMER_ACCOUNT ca ON a.ACCOUNT_ID = ca.ACCOUNT_ID";
            predicates.add("ca.CUSTOMER_ID IN (:customerIds)");
        }

        String sql = SELECT_SQL + optionalJoin + " WHERE " + Joiner.on(" AND ").join(predicates);
        return jdbcTemplate.query(sql, params, ROW_MAPPER);
    }

    public void deleteAll() {
        jdbcTemplate.update("DELETE FROM ACCOUNT", Collections.emptyMap());
    }

    private Map<String, Object> paramsMap(Account account) {
        Map<String, Object> params = new HashMap<>();
        params.put("accountId", account.getAccountId());
        params.put("accountNumber", account.getAccountNumber());
        params.put("customerIds", isEmpty(account.getCustomerIds()) ? null : account.getCustomerIds());
        return params;
    }

    @PostConstruct
    public void initTemplate() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public static final RowMapper<Account> ROW_MAPPER = (rs, rowNum) -> Account.builder()
            .accountId(rs.getString("ACCOUNT_ID"))
            .accountNumber(rs.getInt("ACCOUNT_NUMBER"))
            .build();

}
