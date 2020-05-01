package uk.co.phoebus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.co.phoebus.db.AccountRepository;
import uk.co.phoebus.db.CustomerAccountLinkRepository;
import uk.co.phoebus.db.CustomerRepository;
import uk.co.phoebus.model.Account;
import uk.co.phoebus.model.Customer;
import uk.co.phoebus.model.CustomerAccountLink;
import uk.co.phoebus.web.RestError;

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.co.phoebus.model.TestObjects.aDefaultAccount;
import static uk.co.phoebus.model.TestObjects.aDefaultCustomer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ApplicationIntTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private CustomerAccountLinkRepository linkRepository;

    private String customersUrl;
    private String customerSearchesUrl;
    private String accountsUrl;
    private String accountSearchesUrl;
    private String customerAccountLinksUrl;



    @BeforeEach
    public void beforeEach() {
        String baseUrlPrefix = String.format("http://localhost:%s/", port);
        customersUrl = baseUrlPrefix + "customers";
        customerSearchesUrl = baseUrlPrefix + "customer-searches";
        accountsUrl = baseUrlPrefix + "accounts";
        accountSearchesUrl = baseUrlPrefix + "account-searches";
        customerAccountLinksUrl = baseUrlPrefix+"customer-account-links";

        customerRepository.deleteAll();
        accountRepository.deleteAll();
        linkRepository.deleteAll();
    }

    @Test
    public void customer_invalidCreateReturns400() {
        Customer customer = aDefaultCustomer().toBuilder()
                .forename(null)
                .build();
        ResponseEntity<List<RestError>> createResponse = createCustomerExpectingError(customer);
        assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
        RestError expectedError = RestError.builder()
                .field("forename")
                .message("must not be null")
                .build();
        assertEquals(newArrayList(expectedError), createResponse.getBody());
    }

    @Test
    public void customer_saveAndGetById() {
        Customer createResponse = createCustomer(aDefaultCustomer());
        String customerId = createResponse.getCustomerId();

        ResponseEntity<Customer> response = getCustomerById(customerId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Customer actual = response.getBody();

        assertCustomerAttributes(aDefaultCustomer(), actual);
    }

    @Test
    public void customer_getByInvalidId() {
        createCustomer(aDefaultCustomer());

        ResponseEntity<Customer> response = getCustomerById("not-found");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void customer_searchByForename() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        createCustomer(aDefaultCustomer().toBuilder().forename("NotPeter").build());

        Customer searchModel = Customer.builder()
                .forename(createdCustomer.getForename())
                .build();
        List<Customer> searchResults = searchCustomers(searchModel);
        assertEquals(1, searchResults.size());
        assertEquals(createdCustomer.getCustomerId(), searchResults.get(0).getCustomerId());
    }

    @Test
    public void customer_searchBySurname() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        createCustomer(aDefaultCustomer().toBuilder().surname("NotParker").build());

        Customer searchModel = Customer.builder()
                .surname(createdCustomer.getSurname())
                .build();
        List<Customer> searchResults = searchCustomers(searchModel);
        assertEquals(1, searchResults.size());
        assertEquals(createdCustomer.getCustomerId(), searchResults.get(0).getCustomerId());
    }

    @Test
    public void customer_searchByDateOfBirth() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        createCustomer(aDefaultCustomer().toBuilder().dateOfBirth(LocalDate.parse("2020-01-01")).build());

        Customer searchModel = Customer.builder()
                .dateOfBirth(createdCustomer.getDateOfBirth())
                .build();
        List<Customer> searchResults = searchCustomers(searchModel);
        assertEquals(1, searchResults.size());
        assertEquals(createdCustomer.getCustomerId(), searchResults.get(0).getCustomerId());
    }

    @Test
    public void customer_searchByAccountId() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        Account createdAccount = createAccount(aDefaultAccount());

        CustomerAccountLink customerAccountLink = CustomerAccountLink.builder()
                .customerId(createdCustomer.getCustomerId())
                .accountId(createdAccount.getAccountId())
                .build();
        CustomerAccountLink createdLink = createCustomerAccountLink(customerAccountLink);
        assertEquals(customerAccountLink, createdLink);

        Customer otherCustomer = createCustomer(aDefaultCustomer());
        Account otherAccount = createAccount(aDefaultAccount().toBuilder().accountNumber(22222222).build());

        CustomerAccountLink otherCustomerAccountLink = CustomerAccountLink.builder()
                .customerId(otherCustomer.getCustomerId())
                .accountId(otherAccount.getAccountId())
                .build();
        createCustomerAccountLink(otherCustomerAccountLink);

        Customer searchModel = Customer.builder()
                .accountIds(newArrayList(createdAccount.getAccountId()))
                .build();
        List<Customer> searchResults = searchCustomers(searchModel);
        assertEquals(1, searchResults.size());
        assertEquals(createdCustomer.getCustomerId(), searchResults.get(0).getCustomerId());
    }

    @Test
    public void account_invalidCreateReturns400() {
        Account account = aDefaultAccount().toBuilder()
                .accountNumber(null)
                .build();
        ResponseEntity<List<RestError>> createResponse = createAccountExpectingError(account);
        assertEquals(HttpStatus.BAD_REQUEST, createResponse.getStatusCode());
        RestError expectedError = RestError.builder()
                .field("accountNumber")
                .message("must not be null")
                .build();
        assertEquals(newArrayList(expectedError), createResponse.getBody());
    }

    @Test
    public void account_saveAndGetById() {
        Account createdAccount = createAccount(aDefaultAccount());
        String accountId = createdAccount.getAccountId();

        ResponseEntity<Account> response = getAccountById(accountId);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertAccountAttributes(aDefaultAccount(), response.getBody());
    }

    @Test
    public void account_searchByAccountNumber() {
        Account createdAccount = createAccount(aDefaultAccount());
        createAccount(aDefaultAccount().toBuilder().accountNumber(99999999).build());

        List<Account> searchResults = searchAccounts(Account.builder().accountNumber(createdAccount.getAccountNumber()).build());
        assertEquals(1, searchResults.size());
        assertEquals(createdAccount.getAccountId(), searchResults.get(0).getAccountId());
    }

    @Test
    public void account_getByInvalidId() {
        createCustomer(aDefaultCustomer());

        ResponseEntity<Account> response = getAccountById("not-found");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void account_searchByCustomerId() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        Account createdAccount = createAccount(aDefaultAccount());

        CustomerAccountLink customerAccountLink = CustomerAccountLink.builder()
                .customerId(createdCustomer.getCustomerId())
                .accountId(createdAccount.getAccountId())
                .build();
        CustomerAccountLink createdLink = createCustomerAccountLink(customerAccountLink);
        assertEquals(customerAccountLink, createdLink);

        Customer otherCustomer = createCustomer(aDefaultCustomer());
        Account otherAccount = createAccount(aDefaultAccount().toBuilder().accountNumber(22222222).build());

        CustomerAccountLink otherCustomerAccountLink = CustomerAccountLink.builder()
                .customerId(otherCustomer.getCustomerId())
                .accountId(otherAccount.getAccountId())
                .build();
        createCustomerAccountLink(otherCustomerAccountLink);

        Account searchModel = Account.builder()
                .customerIds(newArrayList(createdCustomer.getCustomerId()))
                .build();
        List<Account> searchResults = searchAccounts(searchModel);
        assertEquals(1, searchResults.size());
        assertEquals(createdAccount.getAccountId(), searchResults.get(0).getAccountId());
    }

    @Test
    public void account_duplicateAccountReturnsBadRequest() {
        createAccount(aDefaultAccount());
        ResponseEntity<List<RestError>> accountExpectingError = createAccountExpectingError(aDefaultAccount());
        assertEquals(HttpStatus.BAD_REQUEST, accountExpectingError.getStatusCode());
        List<RestError> expectedErrors = newArrayList(RestError.builder().message("Resource already exists").build());
        assertEquals(expectedErrors, accountExpectingError.getBody());
    }

    @Test
    public void customerAccountLink_associateAccountWithCustomer() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        Account createdAccount = createAccount(aDefaultAccount());

        CustomerAccountLink customerAccountLink = CustomerAccountLink.builder()
                .customerId(createdCustomer.getCustomerId())
                .accountId(createdAccount.getAccountId())
                .build();
        CustomerAccountLink createdLink = createCustomerAccountLink(customerAccountLink);
        assertEquals(customerAccountLink, createdLink);

        Customer customerWithAccountId = createdCustomer.toBuilder()
                .accountIds(newArrayList(createdAccount.getAccountId()))
                .build();
        Account accountWithCustomerId = createdAccount.toBuilder()
                .customerIds(newArrayList(createdCustomer.getCustomerId()))
                .build();

        ResponseEntity<Customer> customerById = getCustomerById(createdCustomer.getCustomerId());
        assertEquals(customerWithAccountId, customerById.getBody());

        ResponseEntity<Account> accountById = getAccountById(createdAccount.getAccountId());
        assertEquals(accountWithCustomerId, accountById.getBody());
    }

    @Test
    public void customerAccountLink_invalidAccountIdReturnsError() {
        Customer createdCustomer = createCustomer(aDefaultCustomer());
        createAccount(aDefaultAccount());

        CustomerAccountLink customerAccountLink = CustomerAccountLink.builder()
                .customerId(createdCustomer.getCustomerId())
                .accountId("does-not-exist")
                .build();

        ResponseEntity<List<RestError>> errorResponse = createCustomerAccountLinkExpectingError(customerAccountLink);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        List<RestError> expectedErrors = newArrayList(RestError.builder().message("customerId or accountId does not exist").build());
        assertEquals(expectedErrors, errorResponse.getBody());
    }

    @Test
    public void customerAccountLink_invalidCustomerIdReturnsError() {
        createCustomer(aDefaultCustomer());
        Account createdAccount = createAccount(aDefaultAccount());

        CustomerAccountLink customerAccountLink = CustomerAccountLink.builder()
                .customerId("does-not-exist")
                .accountId(createdAccount.getAccountId())
                .build();

        ResponseEntity<List<RestError>> errorResponse = createCustomerAccountLinkExpectingError(customerAccountLink);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        List<RestError> expectedErrors = newArrayList(RestError.builder().message("customerId or accountId does not exist").build());
        assertEquals(expectedErrors, errorResponse.getBody());
    }

    private Customer createCustomer(Customer customer) {
        ResponseEntity<Customer> response = testRestTemplate.exchange(customersUrl,
                HttpMethod.POST, new HttpEntity<>(customer), Customer.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<List<RestError>> createCustomerExpectingError(Customer customer) {
        return testRestTemplate.exchange(customersUrl,
                HttpMethod.POST, new HttpEntity<>(customer), new ParameterizedTypeReference<List<RestError>>() {
                });
    }

    private List<Customer> searchCustomers(Customer customer) {
        ResponseEntity<List<Customer>> response = testRestTemplate.exchange(customerSearchesUrl,
                HttpMethod.POST, new HttpEntity<>(customer), new ParameterizedTypeReference<List<Customer>>() {
                });

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<Customer> getCustomerById(String customerId) {
        return testRestTemplate.exchange(customersUrl + "/" + customerId,
                HttpMethod.GET, null, Customer.class);
    }

    private void assertCustomerAttributes(Customer expected, Customer actual) {
        assertNotNull(actual.getCustomerId());
        assertEquals(expected.getForename(), actual.getForename());
        assertEquals(expected.getSurname(), actual.getSurname());
        assertEquals(expected.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(expected.getAccountIds(), expected.getAccountIds());
    }

    private Account createAccount(Account account) {
        ResponseEntity<Account> response = testRestTemplate.exchange(accountsUrl,
                HttpMethod.POST, new HttpEntity<>(account), Account.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private List<Account> searchAccounts(Account account) {
        ResponseEntity<List<Account>> response = testRestTemplate.exchange(accountSearchesUrl,
                HttpMethod.POST, new HttpEntity<>(account), new ParameterizedTypeReference<List<Account>>() {
                });
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<List<RestError>> createAccountExpectingError(Account account) {
        return testRestTemplate.exchange(accountsUrl,
                HttpMethod.POST, new HttpEntity<>(account), new ParameterizedTypeReference<List<RestError>>() {
                });
    }

    private ResponseEntity<Account> getAccountById(String accountId) {
        return testRestTemplate.exchange(accountsUrl + "/" + accountId,
                HttpMethod.GET, null, Account.class);
    }

    private void assertAccountAttributes(Account expected, Account actual) {
        assertNotNull(actual.getAccountId());
        assertEquals(expected.getAccountNumber(), actual.getAccountNumber());
        assertEquals(expected.getCustomerIds(), expected.getCustomerIds());
    }

    private CustomerAccountLink createCustomerAccountLink(CustomerAccountLink link) {
        ResponseEntity<CustomerAccountLink> response = testRestTemplate.exchange(customerAccountLinksUrl,
                HttpMethod.POST, new HttpEntity<>(link), CustomerAccountLink.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return response.getBody();
    }

    private ResponseEntity<List<RestError>> createCustomerAccountLinkExpectingError(CustomerAccountLink link) {
        return testRestTemplate.exchange(customerAccountLinksUrl,
                HttpMethod.POST, new HttpEntity<>(link),new ParameterizedTypeReference<List<RestError>>() {
                });
    }


}
