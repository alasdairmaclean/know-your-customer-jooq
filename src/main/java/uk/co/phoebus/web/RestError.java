package uk.co.phoebus.web;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = RestError.RestErrorBuilder.class)
public class RestError {

    private String field;
    private String message;

    @JsonPOJOBuilder(withPrefix = "")
    public static class RestErrorBuilder {

    }

}
