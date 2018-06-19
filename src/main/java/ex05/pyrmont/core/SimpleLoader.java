package ex05.pyrmont.core;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import org.apache.catalina.Container;
import org.apache.catalina.Loader;
import org.apache.catalina.DefaultContext;

//        容器中加载servlet的任务被分配给了Loader实现
//        容器中加载servlet的任务被分配给了Loader实现。
//        在该程序中SimpleLoader就是一个Loader实现。它知道如何定位一个servlet，
//        并且通过getClassLoader获得一个java.lang.ClassLoader实例用来查找servlet类位置。
//        SimpleLoader定义了3个变量
public class SimpleLoader implements Loader {

//  WEB_ROOT用来指明在哪个文件夹下查找servlet类
  public static final String WEB_ROOT =
    System.getProperty("user.dir") + File.separator  + "webroot";

  ClassLoader classLoader = null;
//容器变量表示容器跟该加载器是相关联的。
  Container container = null;

//SimpleLoader类的构造器初始化类加载器，那样准备返回一个SimpleWrapper实例。
//该程序的构造器用于初始化一个类加载器如前面章节所用的。
  public SimpleLoader() {
    try {
      URL[] urls = new URL[1];
      URLStreamHandler streamHandler = null;
      File classPath = new File(WEB_ROOT);
      String repository = (new URL("file", null, classPath.getCanonicalPath() + File.separator)).toString() ;
      urls[0] = new URL(null, repository, streamHandler);
      classLoader = new URLClassLoader(urls);
    }
    catch (IOException e) {
      System.out.println(e.toString() );
    }


  }

  public ClassLoader getClassLoader() {
    return classLoader;
  }

  public Container getContainer() {
    return container;
  }

  public void setContainer(Container container) {
    this.container = container;
  }

  public DefaultContext getDefaultContext() {
    return null;
  }

  public void setDefaultContext(DefaultContext defaultContext) {
  }

  public boolean getDelegate() {
    return false;
  }

  public void setDelegate(boolean delegate) {
  }

  public String getInfo() {
    return "A simple loader";
  }

  public boolean getReloadable() {
    return false;
  }

  public void setReloadable(boolean reloadable) {
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public void addRepository(String repository) {
  }

  public String[] findRepositories() {
    return null;
  }

  public boolean modified() {
    return false;
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

}