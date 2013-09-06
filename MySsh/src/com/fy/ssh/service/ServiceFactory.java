package com.fy.ssh.service;


/**
 * @author yinbin
 *
 */
public class ServiceFactory {
	
	public static <T> T getInstance(Class<T> clazz){
		return new CglibProxy().get(clazz);
	}

}
