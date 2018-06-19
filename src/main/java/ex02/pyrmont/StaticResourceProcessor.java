package ex02.pyrmont;

import java.io.IOException;

/**
 * @author mazhiqiang
 * @date 14-3-11.
 */
//要测试该应用程序，在浏览器的地址栏或者网址框中敲入：
// http://localhost:8080/index.html
public class StaticResourceProcessor {
	public void process(Request request, Response response) {
		try {
			response.sendStaticResource();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
