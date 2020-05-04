package uk.co.phoebus.web;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.co.phoebus.db.AccountRepository;
import uk.co.phoebus.db.CustomerAccountLinkRepository;
import uk.co.phoebus.db.CustomerRepository;
import uk.co.phoebus.exception.KycRequestValidationException;
import uk.co.phoebus.model.Account;
import uk.co.phoebus.model.Customer;
import uk.co.phoebus.model.CustomerAccountLink;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
public class KycController {

    private CustomerRepository customerRepository;
    private CustomerAccountLinkRepository linkRepository;
    private AccountRepository accountRepository;

    @PostMapping("/customers")
    public Customer createCustomer(@Valid @RequestBody Customer customer,
                                   BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            throw new KycRequestValidationException(bindingResult.getFieldErrors());
        }
        Customer withId = customer.toBuilder()
                .customerId(UUID.randomUUID().toString())
                .build();
        return customerRepository.save(withId).toBuilder().accountIds(customer.getAccountIds()).build();
    }

    @GetMapping("/customers/{customerId}")
    public Customer getCustomerById(@PathVariable("customerId") String customerId) {
        Optional<Customer> customer = customerRepository.findById(customerId)
                .map(this::enrichWithAccountIds);
        return customer.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Customer enrichWithAccountIds(Customer customer) {
        List<CustomerAccountLink> links = linkRepository.findByCustomerId(customer.getCustomerId());
        List<String> accountIds = links.stream()
                .map(CustomerAccountLink::getAccountId)
                .collect(Collectors.toList());
        return customer.toBuilder()
                .accountIds(accountIds)
                .build();
    }

    @PostMapping("/customer-searches")
    public List<Customer> searchCustomers(@RequestBody Customer customer) {
        return customerRepository.findByExample(customer);
    }

    @PostMapping("/accounts")
    public Account createAccount(@Valid @RequestBody Account account,
                                 BindingResult bindingResult) {
        if (bindingResult.hasFieldErrors()) {
            throw new KycRequestValidationException(bindingResult.getFieldErrors());
        }
        Account withId = account.toBuilder()
                .accountId(UUID.randomUUID().toString())
                .build();
        return accountRepository.save(withId);
    }


    @GetMapping("/accounts/{accountId}")
    public Account getAccountById(@PathVariable("accountId") String accountId) {
        List<CustomerAccountLink> links = linkRepository.findByAccountId(accountId);
        Optional<Account> byId = accountRepository.findById(accountId).map(this::enrichWithCustomerIds);
        return byId.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private Account enrichWithCustomerIds(Account account) {
        List<CustomerAccountLink> links = linkRepository.findByAccountId(account.getAccountId());
        List<String> customerIds = links.stream()
                .map(CustomerAccountLink::getCustomerId)
                .collect(Collectors.toList());
        return account.toBuilder()
                .customerIds(customerIds)
                .build();
    }

    @PostMapping("/account-searches")
    public List<Account> searchAccounts(@RequestBody Account account) {
        return accountRepository.findByExample(account);
    }

    @PostMapping("/customer-account-links")
    public CustomerAccountLink createCustomerAccountLink(@RequestBody CustomerAccountLink customerAccountLink) {
        return linkRepository.save(customerAccountLink);
    }

}
