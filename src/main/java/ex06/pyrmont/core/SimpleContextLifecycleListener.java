package ex06.pyrmont.core;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

//注意SimpleContextLifecycleListener类表示了SimpleContext的监听器。
public class SimpleContextLifecycleListener implements LifecycleListener {

  public void lifecycleEvent(LifecycleEvent event) {
    Lifecycle lifecycle = event.getLifecycle();
//    打印触发事件
    System.out.println("SimpleContextLifecycleListener's event " +
      event.getType().toString());
//    如果事件为START_EVENT事件，则打印出“Starting context.”
    if (Lifecycle.START_EVENT.equals(event.getType())) {
      System.out.println("Starting context.");
    }
//    如果事件为STOP_EVENTT，则打印出“Stopping contex”。
    else if (Lifecycle.STOP_EVENT.equals(event.getType())) {
      System.out.println("Stopping context.");
    }
  }
}