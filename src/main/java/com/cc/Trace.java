package com.cc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.cc.tree.Mod;

public class Trace {

	private final long threadId;

	private final String indent = "~";

	private String initIndent = "";

	private BufferedWriter bw;

	public Trace(long threadId, String filePath) {
		this.threadId = threadId;

		String path = "./";
		if (filePath.contains("/") || filePath.contains("\\")) {
			File f = new File(filePath);
			path = f.getParent() == null ? f.getPath() : f.getParent();
		}

		File dir = new File(path, DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()));
		dir.mkdirs();

		for (File file : dir.listFiles())
			file.delete();

		File file = new File(dir, "trace.log." + threadId);
		try {
			bw = new BufferedWriter(new FileWriter(file));
			if (file.exists()) {
				bw.write(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now()) + "\n--------------------\n");
				bw.flush();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void push() {
		initIndent += indent;
	}

	public void pop() {
		if (initIndent.isEmpty())
			return;

		initIndent = initIndent.substring(indent.length());
	}

	public void write(Mod mod, String str) {
		String prefix = getPrefix(mod);
		String line = prefix + str;

		try {
			bw.append(line + "\n");
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public int getDepth() {
		return initIndent.length();
	}

	/**
	 * change when output line
	 * 
	 * @param mod
	 * @return
	 */
	public String getPrefix(Mod mod) {
		return mod == Mod.DEFAULT ? initIndent : initIndent.replace('~', mod.getSign());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (threadId ^ threadId >>> 32);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;

		if (obj == null || getClass() != obj.getClass())
			return false;

		Trace other = (Trace) obj;
		if (threadId != other.threadId)
			return false;

		return true;
	}

	@Override
	public String toString() {
		return "Trace [threadId=" + threadId + "]";
	}
}
