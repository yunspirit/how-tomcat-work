package ex05.pyrmont.core;

import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.Contained;
import org.apache.catalina.Container;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.ValveContext;

public class SimplePipeline implements Pipeline {

  public SimplePipeline(Container container) {
    setContainer(container);
  }

  // The basic Valve (if any) associated with this Pipeline.
  protected Valve basic = null;
  // The Container with which this Pipeline is associated.
  protected Container container = null;
  // the array of Valves
//  流水线包括一个基本阀门（ex05.pyrmont.core.SimpleWrapperValve）
//  和两个另外的阀门(ex05.pyrmont.core.ClientIPLoggerValve
//  和 ex05.pyrmont.core.HeaderLoggerValve)
  protected Valve valves[] = new Valve[0];

  public void setContainer(Container container) {
    this.container = container;
  }

  public Valve getBasic() {
    return basic;
  }

  public void setBasic(Valve valve) {
    this.basic = valve;
    ((Contained) valve).setContainer(container);
  }

  public void addValve(Valve valve) {
    if (valve instanceof Contained)
      ((Contained) valve).setContainer(this.container);

    synchronized (valves) {
      Valve results[] = new Valve[valves.length +1];
      System.arraycopy(valves, 0, results, 0, valves.length);
      results[valves.length] = valve;
      valves = results;
    }
  }

  public Valve[] getValves() {
    return valves;
  }

//          pipeline的启动处
//  在创建一个ValveContext实例之后，流水线调用ValveContext的invokeNext方法。
//  ValveContext会先唤醒流水线的第一个阀门，然后第一个阀门会在完成它的任务之前唤醒下一个阀门
  public void invoke(Request request, Response response)
    throws IOException, ServletException {
    // Invoke the first Valve in this pipeline for this request
    (new SimplePipelineValveContext()).invokeNext(request, response);
  }

  public void removeValve(Valve valve) {
  }

// 内部类
// 流水线必须保证说要添加给它的阀门必须被调用一次，流水线通过创建一个ValveContext接口的实例来实现它。
// ValveContext是流水线的的内部类，这样ValveContext就可以访问流水线中所有的成员。
// ValveContext中最重要的方法是invokeNext方法：
// this class is copied from org.apache.catalina.core.StandardPipeline class's
// StandardPipelineValveContext inner class.

//  一个容器可以有一个流水线。
//  当容器的invoke方法被调用的时候，容器将会处理流水线中的阀门，并一个接一个的处理，直到所有的阀门都被处理完毕。
  // invoke each valve added to the pipeline
  // for (int n=0; n<valves.length; n++)
  // { valve[n].invoke( ... ); }
  // then, invoke the basic valve basicValve.invoke( ... );
  // 但是，Tomcat的设计者选择了一种不同的方式

//  -------------------------------为什么要这样设计-------------------

//  Tomcat5从StandardPipeline中删除了StandardPipelineValveContext类，
//  而是使用rg.apache.catalina.core.StandardValveContext类来代替
//  使用外部类  代替内部类

  protected class SimplePipelineValveContext implements ValveContext {

    protected int stage = 0;

    public String getInfo() {
      return null;
    }

//    该类中最重要的方法是invoke方法，其中包括了一个内部类SimplePipelineValveContext。
    public void invokeNext(Request request, Response response)
      throws IOException, ServletException {
      int subscript = stage;
      stage = stage + 1;
      // Invoke the requested Valve for the current request thread
      if (subscript < valves.length) {
        valves[subscript].invoke(request, response, this);
      }
      else if ((subscript == valves.length) && (basic != null)) {
        basic.invoke(request, response, this);
      }
      else {
        throw new ServletException("No valve");
      }
    }
  } // end of inner class

}