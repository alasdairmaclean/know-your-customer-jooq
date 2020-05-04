package uk.co.phoebus.db;

import nu.studer.sample.public_.tables.CustomerAccount;
import nu.studer.sample.public_.tables.records.CustomerRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.model.Customer;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.condition;
import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
public class CustomerRepository {

    public static final nu.studer.sample.public_.tables.Customer TABLE = nu.studer.sample.public_.tables.Customer.CUSTOMER;
    public static final CustomerAccount JOIN_TABLE = CustomerAccount.CUSTOMER_ACCOUNT;

    @Autowired
    private DSLContext dsl;

    public Customer save(Customer account) {
        dsl.insertInto(TABLE)
                .set(toDb(account))
                .execute();
        return account;
    }

    public Optional<Customer> findById(String customerId) {
        List<Customer> map = dsl.selectFrom(TABLE)
                .where(TABLE.CUSTOMER_ID.eq(customerId))
                .fetch()
                .map(fromDb());
        return map.stream().findFirst();
    }

    public List<Customer> findByExample(Customer searchModel) {
        if (isEmpty(searchModel.getAccountIds())) {
            return dsl
                    .select()
                    .from(TABLE)
                    .where(condition(toDb(searchModel)))
                    .fetch()
                    .map(a -> fromDb().map(a.into(TABLE)));
        }
        return dsl
                .select()
                .from(TABLE.join(JOIN_TABLE).on(TABLE.CUSTOMER_ID.eq(JOIN_TABLE.CUSTOMER_ID)))
                .where(JOIN_TABLE.ACCOUNT_ID.in(searchModel.getAccountIds()))
                .and(condition(toDb(searchModel)))
                .fetch()
                .map(a -> fromDb().map(a.into(TABLE)));
    }

    public void deleteAll() {
        dsl.deleteFrom(TABLE).execute();
    }

    private RecordMapper<CustomerRecord, Customer> fromDb() {
        return c -> Customer.builder()
                .customerId(c.getCustomerId())
                .dateOfBirth(c.getDateOfBirth())
                .forename(c.getForename())
                .surname(c.getSurname())
                .build();
    }

    private CustomerRecord toDb(Customer customer) {
        CustomerRecord c = new CustomerRecord();
        c.setCustomerId(customer.getCustomerId());
        c.setDateOfBirth(customer.getDateOfBirth());
        c.setForename(customer.getForename());
        c.setSurname(customer.getSurname());
        return c;
    }

}
