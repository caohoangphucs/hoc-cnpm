package vn.id.caohoangphuc.cnpm.api.validation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueElementsValidator.class)
public @interface UniqueElements {
    /** Returns the validation message shown when an input contains duplicates. */
    String message() default "Elements must be unique";

    /** Returns validation groups supported by Jakarta Bean Validation. */
    Class<?>[] groups() default {};

    /** Returns payload types supported by Jakarta Bean Validation. */
    Class<? extends Payload>[] payload() default {};
}
