package com.fy.ssh.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yinbin
 *
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Transaction {

	String catalog() default "ds_oracle";

	TrancationType jdbc() default TrancationType.OPEN;

	TrancationType file()default TrancationType.NONE;

}
