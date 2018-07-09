/**
 * copied from ex08.pyrmont.core.SimpleContextConfig
 */
package ex09.pyrmont.core;

import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;

//StandardContext使用一个事件监听器来作为它的配置器。当StandardContext实例的start方法被调用的时候，首先触发一个生命周期事件。
//        该事件唤醒一个监听器来配置该StandardContext实例。配置成功后，该监听器将configured属性设置为true。
//        否则，StandardContext对象拒绝启动，这样就不能对HTTP请求进行服务了。
public class SimpleContextConfig implements LifecycleListener {

  public void lifecycleEvent(LifecycleEvent event) {
    if (Lifecycle.START_EVENT.equals(event.getType())) {
      Context context = (Context) event.getLifecycle();
      context.setConfigured(true);
    }
  }
}