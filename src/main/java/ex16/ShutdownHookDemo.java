package ex16;

//-------------------------钩子----------------------
//在关闭应用程序的时候需要做一些清理工作。问题在于，用户并不是经常的按照要求的流程来退出
//		如果突然的关闭程序，如关闭运行程序的控制台可能会发生意想不到的事情。
//
//		在Java中，虚拟机遇到两种事件的时候会关闭虚拟机：
//1、· 应用程序正常退出如System.exit方法被调用或者最后一个非守护退出。
//2、· 用户突然强制终止虚拟机，例如键入CTRL+C或者在关闭Java程序之前从系统注销。

//----------------当关闭的时候，虚拟机会有以下两个步骤：
//		1. 虚拟机启动所有注册的关闭钩子。关闭钩子是实现在Runtime上面注册的线程。所有的关闭钩子会被同时执行直到完成。
//		2. 虚拟机调用所有的未被调用的finalizers

//------------------创建钩子-----------------lang.Thread类的子类，可以如下创建一个关闭钩子：
//1、· 写一个类继承Thread类
//2、· 提供你的实现类中的run方法。该方法是应用程序被关闭的时候要提交的代码，无论是正常退出还是非正常退出。
//3、· 在你的应用程序中，初始化一个关闭钩子
//4、· 在当前的Runtime上使用addShutdownHook方法来注册该关闭钩子。


public class ShutdownHookDemo {
	public void start() {
		System.out.println("Demo");
		ShutdownHook ShutdownHook = new ShutdownHook();
		Runtime.getRuntime().addShutdownHook(ShutdownHook);
	}

	public static void main(String[] args) {
		ShutdownHookDemo demo = new ShutdownHookDemo();
		demo.start();
		try {
			System.in.read();
		}catch(Exception e) {}
	}
}

class ShutdownHook extends Thread {

	public void run() {
		System.out.println("Shutting down");
	}
}
