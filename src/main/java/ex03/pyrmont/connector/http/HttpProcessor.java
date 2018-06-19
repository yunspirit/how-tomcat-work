package ex03.pyrmont.connector.http;

import ex03.pyrmont.ServletProcessor;
import ex03.pyrmont.StaticResourceProcessor;

import java.net.Socket;
import java.io.OutputStream;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;

import org.apache.catalina.util.RequestUtil;
import org.apache.catalina.util.StringManager;

/* this class used to be called HttpServer */
public class HttpProcessor {

  public HttpProcessor(HttpConnector connector) {
    this.connector = connector;
  }
  /**
   * The HttpConnector with which this processor is associated.
   */
  private HttpConnector connector = null;
  private HttpRequest request;
  private HttpRequestLine requestLine = new HttpRequestLine();
  private HttpResponse response;

  protected String method = null;
  protected String queryString = null;

  /**
   * The string manager for this package.
   */
  protected StringManager sm =
    StringManager.getManager("ex03.pyrmont.connector.http");

//  HttpProcessor类的process方法接受前来的HTTP请求的套接字，会做下面的事情：
//          1. 创建一个HttpRequest对象。
//          2. 创建一个HttpResponse对象。
//          3. 解析HTTP请求的第一行和头部，并放到HttpRequest对象。
//          4. 解析HttpRequest和HttpResponse对象到一个ServletProcessor或者 StaticResourceProcessor。
//  像第2章里边说的，ServletProcessor调用被请求的servlet的service方法，
//  而StaticResourceProcessor发送一个静态资源的内容。

  public void process(Socket socket) {
    SocketInputStream input = null;
    OutputStream output = null;
    try {
      input = new SocketInputStream(socket.getInputStream(), 2048);
      output = socket.getOutputStream();

      // create HttpRequest object and parse
      request = new HttpRequest(input);

      // create HttpResponse object
      response = new HttpResponse(output);
      response.setRequest(request);

      response.setHeader("Server", "Pyrmont Servlet Container");
//解析之后把对应信息放在request的属性中
      parseRequest(input, output);
      parseHeaders(input);

      //check if this is a request for a servlet or a static resource
      //a request for a servlet begins with "/servlet/"
//      如果是/servlet/   使用servletProccessor
      if (request.getRequestURI().startsWith("/servlet/")) {
        ServletProcessor processor = new ServletProcessor();
        processor.process(request, response);
      }
//			静态资源processsor
      else {
        StaticResourceProcessor processor = new StaticResourceProcessor();
        processor.process(request, response);
      }

      // Close the socket
      socket.close();
      // no shutdown for this application
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * This method is the simplified version of the similar method in
   * org.apache.catalina.connector.http.HttpProcessor.
   * However, this method only parses some "easy" headers, such as
   * "cookie", "content-length", and "content-type", and ignore other headers.
   * @param input The input stream connected to our socket
   *
   * @exception java.io.IOException if an input/output error occurs
   * @exception javax.servlet.ServletException if a parsing error occurs
   */
//  HttpProcessor类使用它的parse方法 来解析一个HTTP请求中的请求行和头部。
//  解析出来并把值赋给HttpProcessor对象的这些字段。不过，parse方法并不解析请求内容或者请求 字符串里边的参数。
//  这个任务留给了HttpRequest对象它们。只是当servlet需要一个参数时，查询字符串或者请求内容才会被解析。

//  HttpProcessor类中的私有方法--parseRequest，parseHeaders和normalize，是用来帮助填充HttpRequest的。
private void parseHeaders(SocketInputStream input)
    throws IOException, ServletException {
//    parseHeaders方法包括一个while循环用于持续的从SocketInputStream中读取头部，
//    直到再也没有头部出现为止。
//    循环从构建一个HttpHeader对象开始，并把它传递给类SocketInputStream的readHeader方法：
    while (true) {
      HttpHeader header = new HttpHeader();;
      // Read the next header
      input.readHeader(header);
// 通过检测HttpHeader实例的nameEnd和valueEnd字段来测试是否可以从输入流中读取下一个头部信息
      if (header.nameEnd == 0) {
        if (header.valueEnd == 0) {
          return;
        }
        else {
          throw new ServletException
            (sm.getString("httpProcessor.parseHeaders.colon"));
        }
      }
// 假如存在下一个头部，那么头部的名称和值可以通过下面方法进行检索
      String name = new String(header.name, 0, header.nameEnd);
      String value = new String(header.value, 0, header.valueEnd);
//一旦你获取到头部的名称和值，你通过调用HttpRequest对象的addHeader方法来把它加入headers这个HashMap
      request.addHeader(name, value);

      // do something for some headers, ignore others.
//        Cookies是作为一个Http请求头部通过浏览器来发送的。
//        这样一个头部名为"cookie"并且它的值是一些cookie名/值对。
//        这里是一个包括两个cookie:username和password的cookie头部的例子。

//        Cookie: userName=budi; password=pwd;

//        Cookie的解析是通过类org.apache.catalina.util.RequestUtil的parseCookieHeader方法来处理的。
//        这个方法接受cookie头部并返回一个javax.servlet.http.Cookie数组。
//        数组内的元素数量和头部里边的cookie名/值对个数是一样的
      if (name.equals("cookie")) {
        Cookie cookies[] = RequestUtil.parseCookieHeader(value);
        for (int i = 0; i < cookies.length; i++) {
          if (cookies[i].getName().equals("jsessionid")) {
            // Override anything requested in the URL
            if (!request.isRequestedSessionIdFromCookie()) {
              // Accept only the first session id cookie
              request.setRequestedSessionId(cookies[i].getValue());
//               从cookie中获得session      true
              request.setRequestedSessionCookie(true);
//               从URL中获得session         false
              request.setRequestedSessionURL(false);
            }
          }
          request.addCookie(cookies[i]);
        }
      }
      //一些头部也需要某些属性的设置。
//        例如，当servlet调用javax.servlet.ServletRequest的getContentLength方法的时候，
//        content-length头部的值将被返回。
//        而包含cookies的cookie头部将会给添加到cookie集合中。
//        就这样，下面是其中一些过程：
      else if (name.equals("content-length")) {
        int n = -1;
        try {
          n = Integer.parseInt(value);
        }
        catch (Exception e) {
          throw new ServletException(sm.getString("httpProcessor.parseHeaders.contentLength"));
        }
        request.setContentLength(n);
      }
      else if (name.equals("content-type")) {
        request.setContentType(value);
      }
    } //end while
  }


  //  HttpProcessor类中的私有方法--parseRequest，parseHeaders和normalize，是用来帮助填充HttpRequest的。

//  HttpProcessor的process方法调用私有方法parseRequest用来解析请求行，例如一个HTTP请求的第一行。

//  GET  /myApp/ModernServlet?userName=tarzan&password=pwd  HTTP/1.1

//  请求行的第二部分是URI加上一个查询字符串。
//  在上面的例子中，URI是这样的： /myApp/ModernServlet
//  另外，在？后面的任何东西都是查询字符串。因此，查询字符串是这样的： userName=tarzan&password=pwd
//  查询字符串可以包括零个或多个参数。
//  在上面的例子中，有两个参数名/值对，userName/tarzan和password/pwd。
//  在servlet/JSP编程中，参数名jsessionid是用来携带一个会话标识符。
//  会话标识符经常被作为cookie来嵌入，但是程序员可以选择把它嵌入到查询字符串去，
//  例如，当浏览器的cookie被禁用的时候。
// 当parseRequest方法被HttpProcessor类的process方法调用的时候，
//  request变量指向一个HttpRequest实例。parseRequest方法解析请求行用来获得几个值并把这些值赋给HttpRequest对象。
  private void parseRequest(SocketInputStream input, OutputStream output)
    throws IOException, ServletException {

    // Parse the incoming request line
//    调用它的readRequestLine方法来告诉SocketInputStream去填入HttpRequestLine实例。
    input.readRequestLine(requestLine);
//    请求行的方法，URI和协议：
    String method =
      new String(requestLine.method, 0, requestLine.methodEnd);
//    因为URI中可能有？查询字符串  所有需要后续处理
    String uri = null;
    String protocol = new String(requestLine.protocol, 0, requestLine.protocolEnd);

    // Validate the incoming request line
    if (method.length() < 1) {
      throw new ServletException("Missing HTTP request method");
    }
    else if (requestLine.uriEnd < 1) {
      throw new ServletException("Missing HTTP request URI");
    }
    // Parse any query parameters out of the request URI
//    在URI后面可以有查询字符串，假如存在的话，查询字符串会被一个？分隔开来。
//    因此，parseRequest方法试图首先获取查询字符串。并调用setQueryString方法来填充HttpRequest对象：
    int question = requestLine.indexOf("?");
    if (question >= 0) {
      request.setQueryString(new String(requestLine.uri, question + 1,
              requestLine.uriEnd - question - 1));
      uri = new String(requestLine.uri, 0, question);
    }
// 不存在查询字符串
    else {
      request.setQueryString(null);
      uri = new String(requestLine.uri, 0, requestLine.uriEnd);
  }

//    不过，大多数情况下，URI指向一个相对资源，URI还可以是一个绝对值，就像下面所示：
//    http://www.brainysoftware.com/index.html?name=Tarzan 经过前面处理  ？后面的查询字符串已经没有了
// Checking for an absolute URI (with the HTTP protocol)
    if (!uri.startsWith("/")) {
      int pos = uri.indexOf("://");
      // Parsing out protocol and host name
      if (pos != -1) {
        pos = uri.indexOf('/', pos + 3);
        if (pos == -1) {
          uri = "";
        }
        else {
          uri = uri.substring(pos);
        }
      }
    }
//   查询字符串也可以包含一个会话标识符，用jsessionid参数名来指代。
//    因此，parseRequest方法也检查一个会话标识符。假如在查询字符串里边找到jessionid，
//    方法就取得会话标识符，并通过调用setRequestedSessionId方法把值交给HttpRequest实例：
    // Parse any requested session ID out of the request URI
    String match = ";jsessionid=";
    int semicolon = uri.indexOf(match);
    if (semicolon >= 0) {
//      截取 ";jsessionid=" 后面的内容
      String rest = uri.substring(semicolon + match.length());
      int semicolon2 = rest.indexOf(';');
      if (semicolon2 >= 0) {
        request.setRequestedSessionId(rest.substring(0, semicolon2));
        rest = rest.substring(semicolon2);
      }
      else {
        request.setRequestedSessionId(rest);
        rest = "";
      }
      request.setRequestedSessionURL(true);
      uri = uri.substring(0, semicolon) + rest;
    }
    else {
//      当jsessionid被找到，也意味着会话标识符是携带在查询字符串里边，而不是在cookie里边。
//      因此，传递true给request的 setRequestSessionURL方法。
//      否则，传递false给setRequestSessionURL方法
//          并传递null给setRequestedSessionURL方法。
      request.setRequestedSessionId(null);
      request.setRequestedSessionURL(false);
    }

//    parseRequest方法传递uri给normalize方法，用于纠正“异常”的URI。
//    例如，任何\的出现都会给/替代。
//    假如uri是正确的格式或者异常可以给纠正的话，normalize将会返回相同的或者被纠正后的URI。
//    假如URI不能纠正的话，它将会给认为是非法的并且通常会返回null。
//    在这种情况下(通常返回null)，parseRequest将会在方法的最后抛出一个异常。
    // Normalize URI (using String operations at the moment)
    String normalizedUri = normalize(uri);

    // Set the corresponding request properties
    ((HttpRequest) request).setMethod(method);
    request.setProtocol(protocol);
    if (normalizedUri != null) {
      ((HttpRequest) request).setRequestURI(normalizedUri);
    }
    else {
      ((HttpRequest) request).setRequestURI(uri);
    }

    if (normalizedUri == null) {
      throw new ServletException("Invalid URI: " + uri + "'");
    }
  }

  /**
   * Return a context-relative path, beginning with a "/", that represents
   * the canonical version of the specified path after ".." and "." elements
   * are resolved out.  If the specified path attempts to go outside the
   * boundaries of the current context (i.e. too many ".." path elements
   * are present), return <code>null</code> instead.
   *
   * @param path Path to be normalized
   */
  protected String normalize(String path) {
    if (path == null)
      return null;
    // Create a place for the normalized path
    String normalized = path;

    // Normalize "/%7E" and "/%7e" at the beginning to "/~"
    if (normalized.startsWith("/%7E") || normalized.startsWith("/%7e"))
      normalized = "/~" + normalized.substring(4);

    // Prevent encoding '%', '/', '.' and '\', which are special reserved
    // characters
    if ((normalized.indexOf("%25") >= 0)
      || (normalized.indexOf("%2F") >= 0)
      || (normalized.indexOf("%2E") >= 0)
      || (normalized.indexOf("%5C") >= 0)
      || (normalized.indexOf("%2f") >= 0)
      || (normalized.indexOf("%2e") >= 0)
      || (normalized.indexOf("%5c") >= 0)) {
      return null;
    }

    if (normalized.equals("/."))
      return "/";

    // Normalize the slashes and add leading slash if necessary
    if (normalized.indexOf('\\') >= 0)
      normalized = normalized.replace('\\', '/');
    if (!normalized.startsWith("/"))
      normalized = "/" + normalized;

    // Resolve occurrences of "//" in the normalized path
    while (true) {
      int index = normalized.indexOf("//");
      if (index < 0)
        break;
      normalized = normalized.substring(0, index) +
        normalized.substring(index + 1);
    }

    // Resolve occurrences of "/./" in the normalized path
    while (true) {
      int index = normalized.indexOf("/./");
      if (index < 0)
        break;
      normalized = normalized.substring(0, index) +
        normalized.substring(index + 2);
    }

    // Resolve occurrences of "/../" in the normalized path
    while (true) {
      int index = normalized.indexOf("/../");
      if (index < 0)
        break;
      if (index == 0)
        return (null);  // Trying to go outside our context
      int index2 = normalized.lastIndexOf('/', index - 1);
      normalized = normalized.substring(0, index2) +
        normalized.substring(index + 3);
    }

    // Declare occurrences of "/..." (three or more dots) to be invalid
    // (on some Windows platforms this walks the directory tree!!!)
    if (normalized.indexOf("/...") >= 0)
      return (null);

    // Return the normalized path that we have completed
    return (normalized);

  }

}
