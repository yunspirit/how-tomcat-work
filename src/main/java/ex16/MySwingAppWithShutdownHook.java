package ex16;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

//当用户关闭该应用程序的时候，程序必须删除该临时文件。
//		我们希望用户总是点击Exit按钮，这样在shutdown方法中就可以总是删除临时文件。
//		但是，临时文件在用户非正常退出的时候也必须被删除。
public class MySwingAppWithShutdownHook extends JFrame {
	
	JButton exitButton = new JButton();
	JTextArea jTextArea1 = new JTextArea();
	String dir = System.getProperty("user.dir");
	String filename = "temp.txt";

	public MySwingAppWithShutdownHook() {
		exitButton.setText("Exit");
		exitButton.setBounds(new Rectangle(304, 248, 76, 37));
		exitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitButton_actionPerformed(e);
			}
		});
		this.getContentPane().setLayout(null);
		jTextArea1.setText("Click the Exit button to quit");
		jTextArea1.setBounds(new Rectangle(9, 7, 371, 235));
		this.getContentPane().add(exitButton, null);
		this.getContentPane().add(jTextArea1, null);
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setBounds(0, 0, 400, 330);
		this.setVisible(true);
		initialize();
	}

	private void initialize() {
		// add shutdown hook
//		注意该类的initialize方法，它做的第一件事情就是创建一个MyshutdownHook内部类对象。
		MyShutdownHook shutdownHook = new MyShutdownHook();
//		一旦你获得一个该类的实例，就可以将其传递给Runtime的addShutDownHook方法
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		// create a temp file
		File file = new File(dir, filename);
		try {
			System.out.println("Creating temporary file");
			file.createNewFile();
		} catch (IOException e) {
			System.out.println("Failed creating temporary file.");
		}
	}

	private void shutdown() {
		// delete the temp file
		File file = new File(dir, filename);
		if (file.exists()) {
			System.out.println("Deleting temporary file.");
			file.delete();
		}
	}

	void exitButton_actionPerformed(ActionEvent e) {
		shutdown();
		System.exit(0);
	}

	public static void main(String[] args) {
		MySwingAppWithShutdownHook mySwingApp = new MySwingAppWithShutdownHook();
	}

	private class MyShutdownHook extends Thread {
		public void run() {
			shutdown();
		}
	}
}