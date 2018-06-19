/* explains Tomcat's default container */
package ex04.pyrmont.startup;

import ex04.pyrmont.core.SimpleContainer;
import org.apache.catalina.connector.http.HttpConnector;

public final class Bootstrap {

//  Bootstrap 类的main方法构造了一个org.apache.catalina.connector.http.HttpConnector实例
//  和一个 SimpleContainer实例。它接下去调用conncetor的setContainer方法传递container，
//  让connector和container关联起来。下一步，它调用connector的initialize和start方法。
//  这将会使得connector为处理8080端口上的任何请求做好了准备。
  public static void main(String[] args) {
    HttpConnector connector = new HttpConnector();
    SimpleContainer container = new SimpleContainer();
    connector.setContainer(container);
    try {
//      先调用initialize方法
      connector.initialize();
//      后调用线程的connector方法
      connector.start();

      // make the application wait until we press any key.
      System.in.read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}