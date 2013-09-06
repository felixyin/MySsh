package com.fy.ssh.repository.file;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA. User: yinbin Date: 12-11-2 Time: 下午2:46
 * 
 * 文件存储连接接口。 类似jdbc的 Connection
 */
public interface FileConnection {

	boolean upload(String local, String remote) throws Exception;

	boolean upload(InputStream local, String remote) throws Exception;

	boolean download(String remote, String local) throws Exception;

	InputStream download(String remote) throws Exception;

	public boolean download(String remote, OutputStream os) throws Exception;

	// boolean copy(String sourceRemote, String targetRemote) throws Exception;
	//
	// boolean delete(String remote) throws Exception;
	//
	// boolean rename(String sourceRemote, String targetRemote) throws
	// Exception;
	//
	// boolean mkdir(String remote) throws Exception;

	boolean login(String server, int port, String username, String password) throws Exception;

	boolean logout() throws Exception;

	void setAutoCommit(boolean b) throws Exception;

	void commit() throws Exception;

	void rollback() throws Exception;

	boolean login(LoginBean loginBean) throws Exception;

	boolean changeWD2Root() throws Exception;

	void isOpen(LoginBean loginBean) throws Exception;

	boolean isClosed()throws Exception;
}
