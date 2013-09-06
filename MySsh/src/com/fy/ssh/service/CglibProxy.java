package com.fy.ssh.service;

import java.lang.reflect.Method;
import java.sql.Connection;

import javax.sql.DataSource;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import com.fy.ssh.repository.file.ConnectionImpl;
import com.fy.ssh.repository.file.FileConnection;

/**
 * @author yinbin
 * 
 */
public class CglibProxy implements MethodInterceptor {
	private Enhancer enhancer = new Enhancer();

	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> clazz) {
		enhancer.setSuperclass(clazz);
		enhancer.setCallback(this);
		return (T) enhancer.create();
	}

	// 实现MethodInterceptor接口方法
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		Object result = null;

		Transaction transactionAnno = method.getAnnotation(Transaction.class);
		if (null == transactionAnno)
			transactionAnno = method.getDeclaringClass().getAnnotation(Transaction.class);

		if (null != transactionAnno) {
			boolean canCloseJdbcConnection = false;// 继承jdbc事务标志
			boolean canCloseFileConnection = false;// 继承jdbc事务标志

			String dataSourceName = transactionAnno.catalog();
			TrancationType jdbcTrancationType = transactionAnno.jdbc();
			TrancationType fileTrancationType = transactionAnno.file();

			Connection jdbcConn = Context.getJdbcConnection();
			FileConnection fileConn = Context.getX();

			// 通过代理类调用父类中的方法
			try {

				// jdbc 开启事务时的处理
				if (jdbcTrancationType != TrancationType.NONE) {
					if (null == jdbcConn || jdbcConn.isClosed()) {
						canCloseJdbcConnection = true;// 继承事务，关闭连接
						// todo 自己修改ds的来源
						DataSource ds = null;
						jdbcConn = ds.getConnection();
						Context.setJdbcConnection(jdbcConn);
					}
					if (jdbcTrancationType == TrancationType.OPEN && canCloseJdbcConnection) {
						jdbcConn.setAutoCommit(false);
					}
				}

				// file 开启事务时的处理
				if (fileTrancationType != TrancationType.NONE) {
					if (null == fileConn || fileConn.isClosed()) {
						canCloseFileConnection = true;
						ConnectionImpl getConnection = new ConnectionImpl();
						fileConn = getConnection.get();
						fileConn.login(getConnection.getLoginBean());
						Context.setFileConnection(fileConn);
					}
					if (fileTrancationType == TrancationType.OPEN && canCloseFileConnection) {
						fileConn.setAutoCommit(false);
					}
				}

				// invoke 被代理方法
				result = proxy.invokeSuper(obj, args);

				// 提交
				if (jdbcTrancationType == TrancationType.OPEN) {
					if (null != jdbcConn && canCloseJdbcConnection) {
						jdbcConn.commit();
					}
				}

				if (fileTrancationType == TrancationType.OPEN)
					if (null != fileConn && canCloseFileConnection)
						fileConn.commit();

			} catch (Exception e) {

				if (jdbcTrancationType == TrancationType.OPEN)
					if (null != jdbcConn && canCloseJdbcConnection)
						jdbcConn.rollback();

				if (fileTrancationType == TrancationType.OPEN)
					if (null != fileConn && canCloseFileConnection)
						fileConn.rollback();

				throw e;
			} finally {

				if (null != jdbcConn && canCloseJdbcConnection) {// 继承jdbc事务支持
					jdbcConn.close();
					jdbcConn = null;
					Context.setJdbcConnection(jdbcConn);
				}

				if (null != fileConn && canCloseFileConnection) {// 继承file事务支持
					fileConn.logout();
					fileConn = null;
					Context.setFileConnection(fileConn);
				}
			}// finally end

		}// if end

		else {
			// 没有注解便不能使用Context，便也不错任何处理，此类将作为一个普通的service
			result = proxy.invokeSuper(obj, args);
		}

		return result;
	}
}