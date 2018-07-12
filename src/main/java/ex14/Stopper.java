package ex14;

/**
 * @author yunqian.yq
 * @date 2018/7/12 10:17
 */
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;

//------------------------关闭connector和关闭container部分--------------
//在前面的应用程序中，是通过按键来关闭容器
//        在本章中，Stopper类提供了一种优雅的方式来关闭Catalina服务器。
//        它还保证了所有生命周期组件的stop方法会被调用
public class Stopper {
    public static void main(String[] args) {
        // the following code is taken from the Stop method of //
        // the org.apache.catalina.startup.Catalina class
         int port = 8005;
         try {
             Socket socket = new Socket("127.0.0.1", port);
             OutputStream stream = socket.getOutputStream();
             String shutdown = "SHUTDOWN";
             for(int i = 0; i < shutdown.length(); i++) {
                 stream.write(shutdown.charAt(i));
             }

             stream.flush();
             stream.close();
             socket.close();
             System.out.println("The server was successfully shut down.");
         } catch (IOException e) {
             System.out.println("Error. The server has not been started.");
         }
    }
}