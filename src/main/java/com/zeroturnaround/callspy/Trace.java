package com.zeroturnaround.callspy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Trace {

	private final long threadId;

	private final boolean consoleLog;

	private String initIndent = "";

	private String indent = " ";

	private BufferedWriter bw;

	public Trace(boolean consoleLog, String indent, long threadId, String filePath) {
		this.threadId = threadId;
		this.indent = indent;
		this.consoleLog = consoleLog;

		File dir = new File("./" + DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDateTime.now()));
		dir.mkdirs();

		for (File file : dir.listFiles())
			file.delete();

		File file = new File(dir, filePath + "." + threadId);
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

	public void log(String str) {
		String line = initIndent + str;

		if (consoleLog)
			System.out.println(line);

		try {
			bw.append(line + "\n");
			bw.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public long getThreadId() {
		return threadId;
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
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
