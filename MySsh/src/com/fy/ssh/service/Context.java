package com.fy.ssh.service;

import java.io.Serializable;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.fy.ssh.repository.file.FileConnection;

/**
 * @author yinbin
 *
 */
public class Context implements Serializable {

	private static final long serialVersionUID = 360000226517229428L;
	private static final String JDBC_KEY = "JDBC_CONNECTION";
	private static final String FILE_KEY = "FILE_CONNECTION";
	@SuppressWarnings("rawtypes")
	private static ThreadLocal threadLocal = new ThreadLocal();
	@SuppressWarnings("unused")
	private Map<String, Object> map;

	public static Connection getJdbcConnection() throws Exception {
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		if (null != map) {
			Connection con = (Connection) map.get(JDBC_KEY);
			if (null != con) {
				return con;
			}
		}
		return null;
	}

	public static void setJdbcConnection(Connection connection) {
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		if (null == map) {
			map = new HashMap<String, Object>();
			threadLocal.set(map);
		}
		map.put(JDBC_KEY, connection);
	}

	public static FileConnection getFileConnection() throws Exception {
		FileConnection fileCon = getX();
		if (null == fileCon)
			throw new Exception("您需要在此方法的注解Transcation中配置file属性");
		return fileCon;
	}

	public static FileConnection getX() throws Exception {
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		if (null != map) {
			FileConnection con = (FileConnection) map.get(FILE_KEY);
			if (null != con) {
				return con;
			}
		}
		return null;
	}

	public static void setFileConnection(FileConnection fileConnection) {
		Map<String, Object> map = (Map<String, Object>) threadLocal.get();
		if (null == map) {
			map = new HashMap<String, Object>();
			threadLocal.set(map);
		}
		map.put(FILE_KEY, fileConnection);
	}
}
