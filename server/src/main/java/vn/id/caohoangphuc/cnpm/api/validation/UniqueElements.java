package vn.id.caohoangphuc.cnpm.api.validation;

import java.lang.annotation.*;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueElementsValidator.class)
public @interface UniqueElements {
    String message() default "Elements must be unique";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
