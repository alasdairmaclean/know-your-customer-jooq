package uk.co.phoebus.model;

import java.time.LocalDate;

public abstract class TestObjects {

    public static Customer aDefaultCustomer() {
        return Customer.builder()
                .forename("Peter")
                .surname("Parker")
                .dateOfBirth(LocalDate.parse("1980-07-06"))
                .build();
    }

    public static Account aDefaultAccount() {
        return Account.builder()
                .accountNumber(11112222)
                .build();
    }

}
