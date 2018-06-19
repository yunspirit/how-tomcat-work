package ex05.pyrmont.startup;

import ex05.pyrmont.core.SimpleLoader;
import ex05.pyrmont.core.SimpleWrapper;
import ex05.pyrmont.valves.ClientIPLoggerValve;
import ex05.pyrmont.valves.HeaderLoggerValve;
import org.apache.catalina.Loader;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.http.HttpConnector;

public final class Bootstrap1 {
  public static void main(String[] args) {

/* call by using http://localhost:8080/ModernServlet,
   but could be invoked by any name */

//    创建HttpConnector 和SimpleWrapper类的实例以后，
//    主方法里分配ModernServlet给SimpleWrapper的setServletClass方法，
//    告诉包装器要加载的类的名字以便于加载。
    HttpConnector connector = new HttpConnector();
    Wrapper wrapper = new SimpleWrapper();
    wrapper.setServletClass("ModernServlet");
//    然后它创建了加载器和两个阀门然后将把加载器给包装器：
    Loader loader = new SimpleLoader();
    Valve valve1 = new HeaderLoggerValve();
    Valve valve2 = new ClientIPLoggerValve();

//    然后把两个阀门添加到包装器流水线中。
    wrapper.setLoader(loader);
    ((Pipeline) wrapper).addValve(valve1);
    ((Pipeline) wrapper).addValve(valve2);
//    把包装器当做容器添加到连接器中，然后初始化并启动连接器。
    connector.setContainer(wrapper);

    try {
      connector.initialize();
      connector.start();

//      下一行允许用户在控制台键入回车键以停止程序。
      // make the application wait until we press a key.
      System.in.read();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}