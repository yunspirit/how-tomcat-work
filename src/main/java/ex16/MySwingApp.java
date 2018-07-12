package ex16;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;


//该应用程序启动的时候创建一个临时的文件，它关闭的时候，该临时文件必须被删除。
public class MySwingApp extends JFrame {
  JButton exitButton = new JButton();
  JTextArea jTextArea1 = new JTextArea();
  String dir = System.getProperty("user.dir");
  String filename = "temp.txt";

  public MySwingApp() {
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
    this.setBounds(0,0, 400, 330);
    this.setVisible(true);
    initialize();
  }

//  运行的时候，该应用程序调用它的initialize方法。该方法在用户工作目录下创建一个临时文件名为temp.txt
  private void initialize() {
    // create a temp file
    File file = new File(dir, filename);
    try {
      System.out.println("Creating temporary file");
      file.createNewFile();
    }
    catch (IOException e) {
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
    MySwingApp mySwingApp = new MySwingApp();
  }
}
