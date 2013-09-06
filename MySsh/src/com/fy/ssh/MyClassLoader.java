package com.fy.ssh;

import java.io.FileInputStream;
import java.io.IOException;


// 自定义一个类加载器，使得类加载器能够通过类文件路径获得该类的字节码文件
/**
 * @author yinbin
 *
 */
public class MyClassLoader extends ClassLoader {
	public MyClassLoader(ClassLoader parent) {
		super(parent);
	}

	public Class<?> load(String path) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			byte[] buf = new byte[fis.available()];
			int len = 0;
			int total = 0;
			int fileLength = buf.length;
			while (total < fileLength) {
				len = fis.read(buf, total, fileLength - total);
				total = total + len;
			}
			return super.defineClass(null, buf, 0, fileLength);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				fis = null;
			}
		}
	}
}
