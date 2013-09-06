package com.fy.ssh.repository.file;


/**
 * Created by IntelliJ IDEA.
 * User: yinbin
 * Date: 12-11-28
 * Time: 下午4:41
 * To change this template use File | Settings | File Templates.
 */
public class LoginBean {

    String ip;
    int port;
    String serverName;
    String username;
    String password;

    public LoginBean() {
    }

    public LoginBean(String ip, int port, String serverName, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.serverName = serverName;
        this.username = username;
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
