package com.fy.ssh.repository.file.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

/**
 * @author yinbin
 */
public class FtpUtil {

	public static final String SEP = "/";

	public static FTPClient login(String server, int port, String username, String password) throws Exception {
		FTPClient ftpClient = new FTPClient();// ftpClient不能共享
		ftpClient.connect(server, port);

		ftpClient.setControlEncoding("GBK");
		FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
		config.setServerLanguageCode("zh");

		boolean lb = ftpClient.login(username, password);
		if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
			ftpClient.disconnect();
			throw new Exception("未连接到FTP，用户名或密码错误。");
		}
		if (lb) {
			return ftpClient;
		}
		throw new Exception("登录ftp失败，请检查连接参数");
	}

	public static boolean upload(FTPClient ftpClient, String local, String remote) throws Exception {
		InputStream input = new FileInputStream(local);
		return upload(ftpClient, input, remote);
	}

	public static boolean upload(FTPClient ftpClient, InputStream input, String remote) throws Exception {
		try {
			boolean b1 = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			boolean b2 = ftpClient.storeFile(remote, input);
			return b1 && b2;
		} finally {
			if (null != input) {
				input.close();
			}
		}
	}

	public static boolean isFileExist(FTPClient ftpClient, String fileRemote) throws Exception {
		return ftpClient.getStatus(fileRemote) == null ? false : true;
	}

	public static boolean download(FTPClient ftpClient, String remote, String local) throws Exception {
		OutputStream output = new FileOutputStream(local);
		try {
			boolean b1 = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			boolean b2 = ftpClient.retrieveFile(remote, output);
			return b1 && b2;
		} finally {
			if (null != output) {
				output.close();
			}
		}
	}

	public static boolean download(FTPClient ftpClient, String remote, OutputStream os) throws Exception {
		boolean b1 = ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
		boolean b2 = ftpClient.retrieveFile(remote, os);
		return b1 && b2;
	}

	public static InputStream download(FTPClient ftpClient, String remote) throws Exception {
		InputStream inputStream = null;
		// try {
		inputStream = ftpClient.retrieveFileStream(remote);
		// } finally {
		// if (null != inputStream) {
		// inputStream.close();
		// }
		// }
		return inputStream;
	}

	public static boolean copy(FTPClient ftpClient, String sourceRemote, String targetRemote) throws Exception {
		// InputStream input = download(ftpClient, sourceRemote);
		// boolean ub = upload(ftpClient, input, targetRemote);
		// return ub;
		String tempPath = FtpUtil.class.getResource("").getPath() + sourceRemote.replace("/", "");
		boolean db = download(ftpClient, sourceRemote, tempPath);
		if (db) {
			boolean ub = upload(ftpClient, tempPath, targetRemote);
			if (ub) {
				File tempFile = new File(tempPath);
				if (tempFile.exists()) {
					tempFile.delete();
					return true;
				}
			}
		}
		return false;
	}

	public static boolean delete(FTPClient ftpClient, String remote) throws Exception {
		return ftpClient.deleteFile(remote);
	}

	public static boolean rename(FTPClient ftpClient, String sourceRemote, String targetRemote) throws Exception {
		return ftpClient.rename(sourceRemote, targetRemote);
	}

	public synchronized static boolean mkdir(FTPClient ftpClient, String remote) throws Exception {
		String[] folders = remote.split(SEP);
		for (int i = 0; i < folders.length; i++) {
			String folder = folders[i];
			if ("".equals(folder)) {
				continue;
			}
			if (!ftpClient.changeWorkingDirectory(folder)) {
				boolean mb = ftpClient.makeDirectory(folder);
				if (!mb) {
					return false;
				} else {
					ftpClient.changeWorkingDirectory(folder);
				}
			}
		}
		return true;
	}

	public static boolean rmdir(FTPClient ftpClient, String pathname) throws Exception {
		return diGui(ftpClient, pathname);
	}

	private static boolean diGui(FTPClient ftpClient, String pathname) throws Exception {
		if (!pathname.startsWith(SEP)) {
			pathname = SEP + pathname;
		}
		if (pathname.endsWith(SEP)) {
			pathname = pathname.substring(0, pathname.length() - 1);
		}
		int sepIndex = pathname.lastIndexOf(SEP);
		if (sepIndex != -1) {
			String workDir = pathname.substring(0, sepIndex);
			boolean cb = ftpClient.changeWorkingDirectory(workDir);
			if (cb) {
				String deleDir = pathname.substring(1 + sepIndex);
				boolean rb = ftpClient.removeDirectory(deleDir);
				if (rb) {
					diGui(ftpClient, workDir);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean logout(FTPClient ftpClient) throws Exception {
		if (null != ftpClient) {
			ftpClient.logout();
			ftpClient.disconnect();
			ftpClient = null;
		}
		return true;
	}

}
