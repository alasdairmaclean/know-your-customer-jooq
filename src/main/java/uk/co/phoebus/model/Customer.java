package uk.co.phoebus.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Serializable {

    public static final int MAX_FORENAME_LENGTH = 100;
    public static final int MAX_SURNAME_LENGTH = 100;

    private String customerId;

    @Null
    private List<String> accountIds;

    @NotNull
    @Length(min = 1, max = MAX_FORENAME_LENGTH)
    private String forename;

    @NotNull
    @Length(min = 1, max = MAX_SURNAME_LENGTH)
    private String surname;

    @NotNull
    private LocalDate dateOfBirth;

}