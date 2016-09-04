package com.chii.netty.sample.HttpServe;

import java.io.File;

/**
 * Describe:
 * Author:  JerryChii.
 * Date:    2016/9/4
 */
public class SystemProperty {
	public static void main(String[] args) {
		System.out.println("user.dir : " + System.getProperty("user.dir"));
		System.out.println("File.pathSeparator : " + File.pathSeparator);
		System.out.println("File.separator : " + File.separator);
		System.out.println("File.separatorChar : " + File.separatorChar);
	}
}
