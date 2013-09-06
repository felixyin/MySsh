package com.fy.ssh.controler;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author yinbin
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
	String value();
}