package uk.co.phoebus.db;

import nu.studer.sample.public_.tables.Account;
import nu.studer.sample.public_.tables.Customer;
import nu.studer.sample.public_.tables.records.CustomerAccountRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.exception.KycRepositoryException;
import uk.co.phoebus.model.CustomerAccountLink;

import java.util.List;

import static org.jooq.impl.DSL.*;

@Repository
public class CustomerAccountLinkRepository {

    public static final nu.studer.sample.public_.tables.CustomerAccount TABLE = nu.studer.sample.public_.tables.CustomerAccount.CUSTOMER_ACCOUNT;
    public static final Customer CUSTOMER = Customer.CUSTOMER;
    public static final Account ACCOUNT = Account.ACCOUNT;

    @Autowired
    private DSLContext dsl;

    public CustomerAccountLink save(CustomerAccountLink customerAccount) {
        int numInserted = dsl.insertInto(TABLE)
                .columns(TABLE.CUSTOMER_ID, TABLE.ACCOUNT_ID)
                .select(select(val(customerAccount.getCustomerId()), val(customerAccount.getAccountId()))
                        .whereExists(selectOne().from(CUSTOMER).where(CUSTOMER.CUSTOMER_ID.eq(customerAccount.getCustomerId())))
                        .and(exists(selectOne().from(ACCOUNT).where(ACCOUNT.ACCOUNT_ID.eq(customerAccount.getAccountId())))))
                .execute();
        if (numInserted != 1) {
            throw new KycRepositoryException("customerId or accountId does not exist");
        }
        return customerAccount;
    }

    public List<CustomerAccountLink> findByCustomerId(String customerId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.CUSTOMER_ID.eq(customerId))
                .fetch()
                .map(fromDb());
    }

    public List<CustomerAccountLink> findByAccountId(String accountId) {
        return dsl.selectFrom(TABLE)
                .where(TABLE.ACCOUNT_ID.eq(accountId))
                .fetch()
                .map(fromDb());
    }

    public void deleteAll() {
        dsl.deleteFrom(TABLE).execute();
    }

    private RecordMapper<CustomerAccountRecord, CustomerAccountLink> fromDb() {
        return ca -> CustomerAccountLink.builder()
                .customerId(ca.getCustomerId())
                .accountId(ca.getAccountId())
                .build();
    }

    private CustomerAccountRecord toDb(CustomerAccountLink customerAccount) {
        CustomerAccountRecord ca = new CustomerAccountRecord();
        ca.setCustomerId(customerAccount.getCustomerId());
        ca.setAccountId(customerAccount.getAccountId());
        return ca;
    }

}
