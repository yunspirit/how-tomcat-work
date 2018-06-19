package ex03.pyrmont.connector.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpConnector implements Runnable {

  boolean stopped;
  private String scheme = "http";

  public String getScheme() {
    return scheme;
  }

//  run方法包括一个while循环，用来做下面的事情：
// 等待HTTP请求
// 为每个请求创建个HttpProcessor实例
// 调用HttpProcessor的process方法
  public void run() {
    ServerSocket serverSocket = null;
    int port = 8080;
    try {
      serverSocket =  new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
    }
    catch (IOException e) {
      e.printStackTrace();
      System.exit(1);
    }
    while (!stopped) {
      // Accept the next incoming connection from the server socket
      Socket socket = null;
      try {
        socket = serverSocket.accept();
      }
      catch (Exception e) {
        continue;
      }
      // Hand this socket off to an HttpProcessor
//      HttpConnector在它自身的线程中运行。但是，
//      在处理下一个请求之前，它必须等待当前处理的HTTP请求结束。
//      下面是第3章中HttpProcessor类的run方法的部分代码：
//      第3章中的HttpProcessor类的process方法是同步的。
//      因此，在接受另一个请求之前，它的run方法要等待process方法运行结束。
      HttpProcessor processor = new HttpProcessor(this);
      processor.process(socket);
    }
  }

  public void start() {
    Thread thread = new Thread(this);
    thread.start();
  }
}