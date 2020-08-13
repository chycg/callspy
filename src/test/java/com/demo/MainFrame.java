package com.demo;

import javax.swing.JFrame;

import com.cc.graph.Painter;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = -1266662931999876034L;

	public MainFrame() {
		initLayout();

		setSize(800, 600);
		setVisible(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private void initLayout() {
		Painter p = new Painter();
		p.addLine("ClassA", "ClassB", "m1");
		p.addLine("ClassB", "ClassC", "m2");
		p.addLine("ClassB", "ClassD", "m3");
		p.addLine("ClassD", "ClassA", "m2");
		p.addLine("ClassA", "ClassB", "m0");
		p.addLine("ClassB", "ClassB", "m2");

		this.add(p);
	}

	public static void main(String[] args) {
		new MainFrame();
	}
}
