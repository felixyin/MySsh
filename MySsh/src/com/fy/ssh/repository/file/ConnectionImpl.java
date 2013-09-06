package com.fy.ssh.repository.file;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fy.ssh.repository.file.ftp.FtpConnection;

/**
 * @author yinbin 隔离
 */
public class ConnectionImpl {

	protected Log logger = LogFactory.getLog(getClass());

	private LoginBean loginBean = new LoginBean();

	public ConnectionImpl() {
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	/**
	 * 读取配置文件，获取连接对象
	 * 
	 * @return
	 * @throws Exception
	 */
	public FileConnection get() throws Exception {
		FileConnection fileConnection = null;
		//todo 发挥你的想象力，自己修改吧。
		loginBean.setIp("ip");
		loginBean.setPort(1111);
		loginBean.setUsername("username");
		loginBean.setPassword("password");
		fileConnection = new FtpConnection();
		return fileConnection;
	}
}
