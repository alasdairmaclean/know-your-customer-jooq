package uk.co.phoebus.model;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static uk.co.phoebus.model.Customer.MAX_FORENAME_LENGTH;
import static uk.co.phoebus.model.Customer.MAX_SURNAME_LENGTH;
import static uk.co.phoebus.model.TestObjects.aDefaultAccount;
import static uk.co.phoebus.model.TestObjects.aDefaultCustomer;


/**
 * Checks model beans' validation annotations
 */
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ModelValidationIntTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void close() {
        validatorFactory.close();
    }

    @Test
    public void customer_valid() {
        Customer customer = aDefaultCustomer();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertEquals(Collections.emptySet(), violations);
    }

    @Test
    public void customer_forenameNull() {
        Customer customer = aDefaultCustomer().toBuilder()
                .forename(null)
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "forename", "must not be null");
    }

    @Test
    public void customer_forenameEmpty() {
        Customer customer = aDefaultCustomer().toBuilder()
                .forename("")
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "forename", "length must be between 1 and 100");
    }

    @Test
    public void customer_forenameTooLong() {
        Customer customer = aDefaultCustomer().toBuilder()
                .forename(StringUtils.repeat("X", MAX_FORENAME_LENGTH + 1))
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "forename", "length must be between 1 and 100");
    }

    @Test
    public void customer_surnameNull() {
        Customer customer = aDefaultCustomer().toBuilder()
                .surname(null)
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "surname", "must not be null");
    }

    @Test
    public void customer_hasAccountIds() {
        Customer customer = aDefaultCustomer().toBuilder()
                .accountIds(newArrayList("test-account-id"))
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "accountIds", "must be null");
    }

    @Test
    public void customer_surnameEmpty() {
        Customer customer = aDefaultCustomer().toBuilder()
                .surname("")
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "surname", "length must be between 1 and 100");
    }

    @Test
    public void customer_surnameTooLong() {
        Customer customer = aDefaultCustomer().toBuilder()
                .surname(StringUtils.repeat("X", MAX_SURNAME_LENGTH + 1))
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "surname", "length must be between 1 and 100");
    }

    @Test
    public void customer_dateOfBirthNull() {
        Customer customer = aDefaultCustomer().toBuilder()
                .dateOfBirth(null)
                .build();
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        expectSingleValidationError(violations, "dateOfBirth", "must not be null");
    }

    @Test
    public void account_valid() {
        Account account = aDefaultAccount();
        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        assertEquals(Collections.emptySet(), violations);
    }

    @Test
    public void account_accountNumberNull() {
        Account account = aDefaultAccount().toBuilder()
                .accountNumber(null)
                .build();
        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        expectSingleValidationError(violations, "accountNumber", "must not be null");
    }

    @Test
    public void customer_hasCustomerIds() {
        Account account = aDefaultAccount().toBuilder()
                .customerIds(newArrayList("test-customer-id"))
                .build();
        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        expectSingleValidationError(violations, "customerIds", "must be null");
    }

    private <T> void expectSingleValidationError(Set<ConstraintViolation<T>> violations, String property, String message) {
        assertEquals(1, violations.size());
        ConstraintViolation<T> violation = violations.iterator().next();
        assertEquals(property, violation.getPropertyPath().toString());
        assertEquals(message, violation.getMessage());
    }

}