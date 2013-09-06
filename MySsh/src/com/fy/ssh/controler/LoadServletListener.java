package com.fy.ssh.controler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.fy.ssh.MyClassLoader;

/**
 * @author yinbin
 * 
 */
public class LoadServletListener implements ServletContextListener {
	// Map中的key用来存放URI，value用来存放URI相对应的Action对象（Action指处理各类请求的控制器）
	private static Map<String, Object> map = new HashMap<String, Object>();

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		if (map != null)
			map = null;
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext context = event.getServletContext();
		String servletPackage = context.getInitParameter("servletPackage");
		String classPath = context.getRealPath("/WEB-INF/classes/") + File.separatorChar
				+ servletPackage.replace('.', File.separatorChar);
		scanClassPath(new File(classPath));
		context.setAttribute("mapPath", map);
		Set<String> keySet = map.keySet();

		System.out
				.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println("扫描到的Action：");
		for (String key : keySet) {
			System.out.println("\t\t------>\turl:" + key + "\tclass:" + map.get(key).getClass().getName());
		}
		System.out
				.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
	}

	/*
	 * 扫描类路径所有类文件,如果类文件含有Control注解，则把注解的value(URI)放进Map中作为key，
	 * 并将类的实例对象作为Map当中的value
	 */
	private void scanClassPath(File file) {
		try {
			if (file.isFile()) {
				if (file.getName().endsWith(".class")) {
					String path = file.getPath();
					MyClassLoader myClassLoader = new MyClassLoader(this.getClass().getClassLoader());
					Class<?> clazz = myClassLoader.load(path);
					Controller controller = (Controller) clazz.getAnnotation(Controller.class);
					if (controller != null) {
						String uri = controller.value();
						Object action = clazz.newInstance();
						map.put(uri, action);
					}
				}
			} else {
				File[] files = file.listFiles();
				for (File child : files) {
					scanClassPath(child);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}