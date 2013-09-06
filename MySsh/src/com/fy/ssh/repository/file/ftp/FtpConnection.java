package com.fy.ssh.repository.file.ftp;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;

import com.fy.ssh.repository.file.FileConnection;
import com.fy.ssh.repository.file.LoginBean;

/**
 * @author yinbin
 */
public class FtpConnection implements FileConnection {

	protected Log logger = LogFactory.getLog(getClass());

	public static final String SEP = "/";

	private FTPClient ftpClient;

	public FTPClient getFtpClient() {
		return ftpClient;
	}

	public void setFtpClient(FTPClient ftpClient) {
		this.ftpClient = ftpClient;
	}

	public FtpConnection() {
	}

	private boolean autoCommit = true;

	public boolean isAutoCommit() {
		return autoCommit;
	}

	public void setAutoCommit(boolean autoCommit) {
		logger.info("ftp setAutoCommit(" + autoCommit+")");
		this.autoCommit = autoCommit;
	}

	private List<HistoryBean> history = new ArrayList<HistoryBean>();

	public FtpConnection(String server, int port, String username, String password) throws Exception {
		if (!login(server, port, username, password)) {
			logger.error("无法登录ftp服务器");
			throw new Exception("无法登录ftp服务器");
		}
	}

	public boolean login(String server, int port, String username, String password) throws Exception {
		ftpClient = FtpUtil.login(server, port, username, password);
		return ftpClient == null ? false : true;
	}

	public boolean logout() throws Exception {
		logger.debug("ftp logout");
		return FtpUtil.logout(ftpClient);
	}

	public boolean upload(String local, String remote) throws Exception {
		return upload(null, local, remote);
	}

	public boolean upload(InputStream local, String remote) throws Exception {
		return upload(local, null, remote);
	}

	private boolean upload(InputStream localInputStream, String local, String remote) throws Exception {
		logger.debug("upload(inputStream:" + localInputStream + ",localString:" + local + ",remote:" + remote + ")");
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		String folder = remote.substring(0, remote.lastIndexOf(SEP));
		boolean b = FtpUtil.mkdir(ftpClient, folder);
		if (b) {
			history.add(new HistoryBean("mkdir", folder, null));
		} else {
			throw new Exception("在ftp上创建文件夹失败");
		}
		if (isOpenTranscation()) {
			if (FtpUtil.isFileExist(ftpClient, remote)) {
				boolean rb = FtpUtil.rename(ftpClient, remote, getBackRemote(remote));
				if (rb) {
					history.add(new HistoryBean("update", remote, getBackRemote(remote)));
				}
			}
		}
		boolean ub = false;
		if (null != local) {
			ub = FtpUtil.upload(ftpClient, local, remote);
		} else if (null != localInputStream) {
			ub = FtpUtil.upload(ftpClient, localInputStream, remote);
		}
		if (ub && isOpenTranscation()) {
			history.add(new HistoryBean("upload", local, remote));
		}
		return ub;
	}

	public boolean download(String remote, String local) throws Exception {
		logger.debug("download(remote:" + remote + ",local:" + local + ")");
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		boolean ib = FtpUtil.isFileExist(ftpClient, remote);
		if (!ib) {
			return ib;
		}
		boolean b = FtpUtil.download(ftpClient, remote, local);
		if (b && isOpenTranscation()) {
			history.add(new HistoryBean("download", remote, local));
		}
		return b;
	}

	public boolean download(String remote, OutputStream os) throws Exception {
		logger.debug("download(remote:" + remote + ")");
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		boolean ib = FtpUtil.isFileExist(ftpClient, remote);
		if (!ib) {
			return ib;
		}
		boolean b = FtpUtil.download(ftpClient, remote, os);
		if (b && isOpenTranscation()) {
			history.add(new HistoryBean("download", remote, ""));
		}
		return b;
	}

	public InputStream download(String remote) throws Exception {
		logger.debug("download(remote:" + remote + ")");
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		boolean ib = FtpUtil.isFileExist(ftpClient, remote);
		if (!ib) {
			throw new Exception("无法下载，在FTP服务器上没有此文件:" + remote);
		}
		InputStream inputStream = FtpUtil.download(ftpClient, remote);
		// if (inputStream.available() == 0) {
		// return null;
		// }
		return inputStream;
	}

	public boolean copy(String sourceRemote, String targetRemote) throws Exception {
		logger.debug("copy(sourceRemote:" + sourceRemote + ",targetRemote:" + targetRemote + ")");
		sourceRemote = new String(sourceRemote.getBytes("UTF-8"), "ISO-8859-1");
		targetRemote = new String(targetRemote.getBytes("UTF-8"), "ISO-8859-1");
		boolean b = FtpUtil.copy(ftpClient, sourceRemote, targetRemote);
		if (b && isOpenTranscation()) {
			history.add(new HistoryBean("copy", sourceRemote, targetRemote));
		}
		return b;
	}

	public boolean delete(String remote) throws Exception {
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		if (isOpenTranscation()) {
			boolean db = FtpUtil.rename(ftpClient, remote, getBackRemote(remote));
			if (db) {
				history.add(new HistoryBean("delete", remote, getBackRemote(remote)));
			}
			return db;
		}
		return FtpUtil.delete(ftpClient, remote);
	}

	public boolean rename(String sourceRemote, String targetRemote) throws Exception {
		sourceRemote = new String(sourceRemote.getBytes("UTF-8"), "ISO-8859-1");
		targetRemote = new String(targetRemote.getBytes("UTF-8"), "ISO-8859-1");
		boolean b = FtpUtil.rename(ftpClient, sourceRemote, targetRemote);
		if (b && isOpenTranscation()) {
			history.add(new HistoryBean("rename", sourceRemote, targetRemote));
		}
		return b;
	}

	public boolean mkdir(String remote) throws Exception {
		remote = new String(remote.getBytes("UTF-8"), "ISO-8859-1");
		boolean b = FtpUtil.mkdir(ftpClient, remote);
		if (b && isOpenTranscation()) {
			history.add(new HistoryBean("mkdir", remote, null));
		}
		return b;
	}

	private boolean isOpenTranscation() {
		return !autoCommit;
	}

	private String getBackRemote(String remote) {
		// 防止多个用户同时请求这个文件的情况
		Long randomL = Math.round(Math.random() * 9999);
		return remote + ".back" + randomL;
	}

	public boolean isFileExist(String fileRemote) throws Exception {
		return ftpClient.getStatus(fileRemote) == null ? false : true;
	}

	public void commit() throws Exception {
		if (isOpenTranscation()) {
			logger.info("commit:");
			for (HistoryBean data : history) {
				String type = data.getType();
				String from = data.getFrom();
				String to = data.getTo();
				String info = type + ":" + from + "--->" + to;
				info = new String(info.getBytes("ISO-8859-1"), "UTF-8");
				logger.info(info);
				if ("update".equals(type)) {
					FtpUtil.delete(ftpClient, to);
				} else if ("upload".equals(type)) {
					// 针对电子签章系统的定制，删除临时文件
					if (null != from && !"".equalsIgnoreCase(from))
						FtpUtil.delete(ftpClient, from);
				} else if ("rename".equals(type)) {
				} else if ("delete".equals(type)) {
					FtpUtil.delete(ftpClient, to);
				} else if ("copy".equals(type)) {
				} else if ("download".equals(type)) {
				} else if ("mkdir".equals(type)) {
				}
			}
		}
	}

	public void rollback() throws Exception {
		if (isOpenTranscation()) {
			logger.info("roolback:");
			Collections.reverse(history);
			for (HistoryBean data : history) {
				String type = data.getType();
				String from = data.getFrom();
				String to = data.getTo();
				String info = type + ":" + from + "<---" + to;
				info = new String(info.getBytes("ISO-8859-1"), "UTF-8");
				logger.info(info);
				if ("update".equals(type)) {
					FtpUtil.rename(ftpClient, to, from);
				} else if ("upload".equals(type)) {
					FtpUtil.delete(ftpClient, to);
					// 针对电子签章系统的定制，删除临时文件
					if (null != from && !"".equalsIgnoreCase(from))
						FtpUtil.delete(ftpClient, from);
				} else if ("rename".equals(type)) {
					FtpUtil.rename(ftpClient, to, from);
				} else if ("delete".equals(type)) {
					FtpUtil.rename(ftpClient, to, from);
				} else if ("copy".equals(type)) {
					FtpUtil.delete(ftpClient, to);
				} else if ("download".equals(type)) {
					File local = new File(to);
					if (local.exists()) {
						local.delete();
					}
				} else if ("mkdir".equals(type)) {
					FtpUtil.rmdir(ftpClient, from);
				}
			}
		}
	}

	@Override
	public boolean login(LoginBean bean) throws Exception {
		return login(bean.getIp(), bean.getPort(), bean.getUsername(), bean.getPassword());
	}

	@Override
	public boolean changeWD2Root() throws Exception {
		return ftpClient.changeWorkingDirectory("/");
	}

	@Override
	public void isOpen(LoginBean loginBean) throws Exception {
		boolean b = ftpClient.isConnected();
		if (!b) {
			login(loginBean);
		}
	}

	@Override
	public boolean isClosed() throws Exception {
		return !(ftpClient.isAvailable() || ftpClient.isConnected());
	}

}

class HistoryBean {

	private String type;
	private String desc;
	private String from;
	private String to;

	public HistoryBean() {
		super();
	}

	public HistoryBean(String type, String from, String to) {
		super();
		this.type = type;
		this.from = from;
		this.to = to;
	}

	public HistoryBean(String type, String desc, String from, String to) {
		super();
		this.type = type;
		this.desc = desc;
		this.from = from;
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

}
