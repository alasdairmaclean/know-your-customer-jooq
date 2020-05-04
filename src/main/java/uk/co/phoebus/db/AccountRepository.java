package uk.co.phoebus.db;

import nu.studer.sample.public_.tables.CustomerAccount;
import nu.studer.sample.public_.tables.records.AccountRecord;
import org.jooq.DSLContext;
import org.jooq.RecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.co.phoebus.model.Account;

import java.util.List;
import java.util.Optional;

import static org.jooq.impl.DSL.condition;
import static org.springframework.util.CollectionUtils.isEmpty;

@Repository
public class AccountRepository {

    public static final nu.studer.sample.public_.tables.Account TABLE = nu.studer.sample.public_.tables.Account.ACCOUNT;
    public static final nu.studer.sample.public_.tables.CustomerAccount JOIN_TABLE = CustomerAccount.CUSTOMER_ACCOUNT;

    @Autowired
    private DSLContext dsl;

    public Account save(Account account) {
        dsl.insertInto(TABLE)
                .set(TABLE.ACCOUNT_ID, account.getAccountId())
                .set(TABLE.ACCOUNT_NUMBER, account.getAccountNumber())
                .execute();
        return account;
    }

    public Optional<Account> findById(String accountId) {
        List<Account> map = dsl.selectFrom(TABLE)
                .where(TABLE.ACCOUNT_ID.eq(accountId))
                .fetch()
                .map(fromDb());
        return map.stream().findFirst();
    }

    public List<Account> findByExample(Account searchModel) {
        if (isEmpty(searchModel.getCustomerIds())) {
            return dsl
                    .select()
                    .from(TABLE)
                    .where(condition(toDb(searchModel)))
                    .fetch()
                    .map(a -> fromDb().map(a.into(TABLE)));
        }
        return dsl
                .select()
                .from(TABLE.join(JOIN_TABLE).on(TABLE.ACCOUNT_ID.eq(JOIN_TABLE.ACCOUNT_ID)))
                .where(JOIN_TABLE.CUSTOMER_ID.in(searchModel.getCustomerIds()))
                .and(condition(toDb(searchModel)))
                .fetch()
                .map(a -> fromDb().map(a.into(TABLE)));
    }

    public void deleteAll() {
        dsl.deleteFrom(TABLE).execute();
    }

    private RecordMapper<AccountRecord, Account> fromDb() {
        return a -> Account.builder()
                .accountId(a.getAccountId())
                .accountNumber(a.getAccountNumber()).build();
    }

    private AccountRecord toDb(Account account) {
        AccountRecord a = new AccountRecord();
        a.setAccountId(account.getAccountId());
        a.setAccountNumber(account.getAccountNumber());
        return a;
    }

}
