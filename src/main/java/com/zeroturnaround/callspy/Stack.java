package com.zeroturnaround.callspy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Stack {

	static BufferedWriter bw;

	static String filePath;

	static String indent = "";

	public static void push() {
		indent += " ";
	}

	public static void pop() {
		if (indent.isEmpty())
			return;

		indent = indent.substring(1);
	}

	public static void push(String method, Object[] args) {
		push();

		log(method, args);
	}

	public static void log(String string) {
		String line = indent + string;
		System.out.println(line);

		if (filePath != null) {
			try {
				if (bw == null) {
					File file = new File(filePath);
					if (file.exists())
						file.delete();

					bw = new BufferedWriter(new FileWriter(file));
				}

				bw.append(line).append("\n");
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void log(String method, Object[] args) {
		log(method + "(" + Utils.toString(args) + ")");
	}

	public static void log(String method, Object[] args, Object returnValue) {
		log(method + "(" + Utils.toString(args) + ") -> " + returnValue);
	}
}
