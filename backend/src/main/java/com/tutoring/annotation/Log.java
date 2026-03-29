package com.tutoring.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    String value() default "";

    String module() default "";

    String operation() default "";

    boolean saveRequestData() default true;

    boolean saveResponseData() default true;
}
