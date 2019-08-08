package com.bquarkz.simplecsv;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target( { ElementType.TYPE } )
@Retention( RetentionPolicy.RUNTIME )
public @interface CSVBean
{
    boolean shouldWriteHeader() default true;
    boolean shouldSkipHeader() default false;
    boolean shouldVerifyHeader() default false;
}
