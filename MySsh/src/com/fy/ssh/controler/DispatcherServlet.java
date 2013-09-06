package com.fy.ssh.controler;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author yinbin
 *
 */
public class DispatcherServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");

		String uri = request.getRequestURI();
		// 去掉工程路径与.do后缀名
		uri = uri.substring(request.getContextPath().length(), uri.length() - 3);

		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) this.getServletContext().getAttribute("mapPath");

		if (map.containsKey(uri)) {
			// 通过http请求uri获得相对应的Action对象
			Object obj = map.get(uri);
			// 获得http请求的方法名
			String methodName = request.getParameter("method");
			// 如果请求的方法null，则默认调用Action对象中的index方法
			if (methodName == null) {
				methodName = "index";
			}
			Method method = null;
			try {
				// 通过反射获得要执行的方法对象
				method = obj.getClass().getMethod(methodName, HttpServletRequest.class, HttpServletResponse.class);
			} catch (Exception e) {
				throw new RuntimeException("在[" + obj.getClass().getName() + "]中找不到与" + methodName + "相对应的方法！！！");
			}
			try {
				// 执行Controller对象中的方法
				method.invoke(obj, request, response);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("您访问的url并不存在:");
			System.out.println("\t系统的url：");
			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				System.out.println("\t\turl:" + key + "\tclass:" + map.get(key).getClass().getName());
			}
			System.err.println("\t您的url:");
			System.err.println("\t\turl:" + uri);
			response.sendRedirect("/index.html");
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}