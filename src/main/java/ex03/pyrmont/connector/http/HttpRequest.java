package ex03.pyrmont.connector.http;

/** this class copies methods from org.apache.catalina.connector.HttpRequestBase
 *  and org.apache.catalina.connector.http.HttpRequestImpl.
 *  The HttpRequestImpl class employs a pool of HttpHeader objects for performance
 *  These two classes will be explained in Chapter 4.
 */
import ex03.pyrmont.connector.RequestStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import java.security.Principal;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.catalina.util.Enumerator;
import org.apache.catalina.util.ParameterMap;
import org.apache.catalina.util.RequestUtil;

public class HttpRequest implements HttpServletRequest {

  private String contentType;
  private int contentLength;
  private InetAddress inetAddress;
  private InputStream input;
  private String method;
  private String protocol;
  private String queryString;
  private String requestURI;
  private String serverName;
  private int serverPort;
  private Socket socket;
  private boolean requestedSessionCookie;
  private String requestedSessionId;
  private boolean requestedSessionURL;

  /**
   * The request attributes for this request.
   */
  protected HashMap attributes = new HashMap();
  /**
   * The authorization credentials sent with this Request.
   */
  protected String authorization = null;
  /**
   * The context path for this request.
   */
  protected String contextPath = "";
  /**
   * The set of cookies associated with this Request.
   */

  protected ArrayList cookies = new ArrayList();
  /**
   * An empty collection to use for returning empty Enumerations.  Do not
   * add any elements to this collection!
   */
  protected static ArrayList empty = new ArrayList();
  /**
   * The set of SimpleDateFormat formats to use in getDateHeader().
   */
  protected SimpleDateFormat formats[] = {
    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
    new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
    new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
  };

  /**
   * The HTTP headers associated with this Request, keyed by name.  The
   * values are ArrayLists of the corresponding header values.
   */
//  HTTP请求中获得头部
  protected HashMap headers = new HashMap();
  /**
   * The parsed parameters for this request.  This is populated only if
   * parameter information is requested via one of the
   * <code>getParameter()</code> family of method calls.  The key is the
   * parameter name, while the value is a String array of values for this
   * parameter.
   * <p>
   * <strong>IMPLEMENTATION NOTE</strong> - Once the parameters for a
   * particular request are parsed and stored here, they are not modified.
   * Therefore, application level access to the parameters need not be
   * synchronized.
   */
//  请求参数
  protected ParameterMap parameters = null;

  /**
   * Have the parameters for this request been parsed yet?
   */
  protected boolean parsed = false;
  protected String pathInfo = null;

  /**
   * The reader that has been returned by <code>getReader</code>, if any.
   */
  protected BufferedReader reader = null;

  /**
   * The ServletInputStream that has been returned by
   * <code>getInputStream()</code>, if any.
   */
  protected ServletInputStream stream = null;

  public HttpRequest(InputStream input) {
    this.input = input;
  }

  public void addHeader(String name, String value) {
    name = name.toLowerCase();
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values == null) {
        values = new ArrayList();
        headers.put(name, values);
      }
      values.add(value);
    }
  }

  /**
   * Parse the parameters of this request, if it has not already occurred.
   * If parameters are present in both the query string and the request
   * content, they are merged.
   */
  //-----------------------------------解析请求参数---------------------------------
//    你不需要马上解析查询字符串或者HTTP请求内容，
//    直到servlet需要通过调用getParameter, getParameterMap, getParameterNames或者getParameterValues方法
//    来读取参数。因此，HttpRequest的这四个方法开头调用了parseParameter方法。
//    这些参数只需要解析一次就够了，因为假如参数在请求内容里边被找到的话，参数解析将会使得SocketInputStream到达字节流的尾部。
//    类HttpRequest使用一个布尔变量parsed来指示是否已经解析过了。
  protected void parseParameters() {
//      因为参数可以存在于查询字符串或者HTTP请求内容中，所以parseParameters方法会检查查询字符串和请求内容。
//      一旦解析过后，参数将会在对象变量parameters中找到，所以方法的开头会检查parsed布尔变量，
//      假如已经解析过的话，parsed将会返回true。
    if (parsed)
      return;
    ParameterMap results = parameters;
//      parseParameters方法创建一个名为results的ParameterMap变量，并指向parameters。
//      假如 parameters为null的话，它将创建一个新的ParameterMap。
//      ParameterMap results = parameters;
    if (results == null)
      results = new ParameterMap();
//  使用了一个布尔变量locked。当locked是false的时候，名/值对仅仅可以添加，更新或者移除。
//  否则，异常IllegalStateException会抛出。而随时都可以读取参数值。
//  最后，parseParameters方法锁定ParameterMap，设置parsed为true，并把results赋予parameters。
    results.setLocked(false);
    String encoding = getCharacterEncoding();
    if (encoding == null)
      encoding = "ISO-8859-1";

    // Parse any parameters specified in the query string
//      parseParameters方法尝试解析查询字符串
//    之前只是获得查询字符串  还没有解析
    String queryString = getQueryString();
    try {
      RequestUtil.parseParameters(results, queryString, encoding);
    }
    catch (UnsupportedEncodingException e) {
      ;
    }
//方法尝试查看HTTP请求内容是否包含参数。   POST方法发送请求
//这种情况发生在当用户使用POST方法发送请求的时候，内容长度大于零，
//并且内容类型是application/x-www-form-urlencoded的时候。
//所以，这里是解析请求内容的代码：
// Parse any parameters specified in the input stream
    String contentType = getContentType();
    if (contentType == null)
      contentType = "";
    int semicolon = contentType.indexOf(';');
    if (semicolon >= 0) {
      contentType = contentType.substring(0, semicolon).trim();
    }
    else {
      contentType = contentType.trim();
    }
    if ("POST".equals(getMethod()) && (getContentLength() > 0)
      && "application/x-www-form-urlencoded".equals(contentType)) {
      try {
        int max = getContentLength();
        int len = 0;
        byte buf[] = new byte[getContentLength()];
        ServletInputStream is = getInputStream();
        while (len < max) {
          int next = is.read(buf, len, max - len);
          if (next < 0 ) {
            break;
          }
          len += next;
        }
        is.close();
        if (len < max) {
          throw new RuntimeException("Content length mismatch");
        }
        RequestUtil.parseParameters(results, buf, encoding);
      }
      catch (UnsupportedEncodingException ue) {
        ;
      }
      catch (IOException e) {
        throw new RuntimeException("Content read fail");
      }
    }

    // Store the final results
//      使用了一个布尔变量locked。当locked是false的时候，名/值对仅仅可以添加，更新或者移除。
//      否则，异常IllegalStateException会抛出。而随时都可以读取参数值。
    results.setLocked(true);
    parsed = true;
    parameters = results;
  }

  public void addCookie(Cookie cookie) {
    synchronized (cookies) {
      cookies.add(cookie);
    }
  }

  /**
   * Create and return a ServletInputStream to read the content
   * associated with this Request.  The default implementation creates an
   * instance of RequestStream associated with this request, but this can
   * be overridden if necessary.
   *
   * @exception java.io.IOException if an input/output error occurs
   */
  public ServletInputStream createInputStream() throws IOException {
    return (new RequestStream(this));
  }

  public InputStream getStream() {
    return input;
  }
  public void setContentLength(int length) {
    this.contentLength = length;
  }

  public void setContentType(String type) {
    this.contentType = type;
  }

  public void setInet(InetAddress inetAddress) {
    this.inetAddress = inetAddress;
  }

  public void setContextPath(String path) {
    if (path == null)
      this.contextPath = "";
    else
      this.contextPath = path;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  public void setPathInfo(String path) {
    this.pathInfo = path;
  }

  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public void setQueryString(String queryString) {
    this.queryString = queryString;
  }

  public void setRequestURI(String requestURI) {
    this.requestURI = requestURI;
  }
  /**
   * Set the name of the server (virtual host) to process this request.
   *
   * @param name The server name
   */
  public void setServerName(String name) {
    this.serverName = name;
  }
  /**
   * Set the port number of the server to process this request.
   *
   * @param port The server port
   */
  public void setServerPort(int port) {
    this.serverPort = port;
  }

  public void setSocket(Socket socket) {
    this.socket = socket;
  }

  /**
   * Set a flag indicating whether or not the requested session ID for this
   * request came in through a cookie.  This is normally called by the
   * HTTP Connector, when it parses the request headers.
   *
   * @param flag The new flag
   */
  public void setRequestedSessionCookie(boolean flag) {
    this.requestedSessionCookie = flag;
  }

  public void setRequestedSessionId(String requestedSessionId) {
    this.requestedSessionId = requestedSessionId;
  }

  public void setRequestedSessionURL(boolean flag) {
    requestedSessionURL = flag;
  }

  /* implementation of the HttpServletRequest*/
  public Object getAttribute(String name) {
    synchronized (attributes) {
      return (attributes.get(name));
    }
  }

  public Enumeration getAttributeNames() {
    synchronized (attributes) {
      return (new Enumerator(attributes.keySet()));
    }
  }

  public String getAuthType() {
    return null;
  }

  public String getCharacterEncoding() {
    return null;
  }

  public int getContentLength() {
    return contentLength ;
  }

  public String getContentType() {
    return contentType;
  }

  public String getContextPath() {
    return contextPath;
  }

  public Cookie[] getCookies() {
    synchronized (cookies) {
      if (cookies.size() < 1)
        return (null);
      Cookie results[] = new Cookie[cookies.size()];
      return ((Cookie[]) cookies.toArray(results));
    }
  }

  public long getDateHeader(String name) {
    String value = getHeader(name);
    if (value == null)
      return (-1L);

    // Work around a bug in SimpleDateFormat in pre-JDK1.2b4
    // (Bug Parade bug #4106807)
    value += " ";

    // Attempt to convert the date header in a variety of formats
    for (int i = 0; i < formats.length; i++) {
      try {
        Date date = formats[i].parse(value);
        return (date.getTime());
      }
      catch (ParseException e) {
        ;
      }
    }
    throw new IllegalArgumentException(value);
  }

  public String getHeader(String name) {
    name = name.toLowerCase();
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values != null)
        return ((String) values.get(0));
      else
        return null;
    }
  }

  public Enumeration getHeaderNames() {
    synchronized (headers) {
      return (new Enumerator(headers.keySet()));
    }
  }

  public Enumeration getHeaders(String name) {
    name = name.toLowerCase();
    synchronized (headers) {
      ArrayList values = (ArrayList) headers.get(name);
      if (values != null)
        return (new Enumerator(values));
      else
        return (new Enumerator(empty));
    }
  }

  public ServletInputStream getInputStream() throws IOException {
    if (reader != null)
      throw new IllegalStateException("getInputStream has been called");

    if (stream == null)
      stream = createInputStream();
    return (stream);
  }

  public int getIntHeader(String name) {
    String value = getHeader(name);
    if (value == null)
      return (-1);
    else
      return (Integer.parseInt(value));
  }

  public Locale getLocale() {
    return null;
  }

  public Enumeration getLocales() {
    return null;
  }

  public String getMethod() {
    return method;
  }

  public String getParameter(String name) {
    parseParameters();
    String values[] = (String[]) parameters.get(name);
    if (values != null)
      return (values[0]);
    else
      return (null);
  }

  public Map getParameterMap() {
    parseParameters();
    return (this.parameters);
  }

  public Enumeration getParameterNames() {
    parseParameters();
    return (new Enumerator(parameters.keySet()));
  }

  public String[] getParameterValues(String name) {
    parseParameters();
    String values[] = (String[]) parameters.get(name);
    if (values != null)
      return (values);
    else
      return null;
  }

  public String getPathInfo() {
    return pathInfo;
  }

  public String getPathTranslated() {
    return null;
  }

  public String getProtocol() {
    return protocol;
  }

  public String getQueryString() {
    return queryString;
  }

  public BufferedReader getReader() throws IOException {
    if (stream != null)
      throw new IllegalStateException("getInputStream has been called.");
    if (reader == null) {
      String encoding = getCharacterEncoding();
      if (encoding == null)
        encoding = "ISO-8859-1";
      InputStreamReader isr =
        new InputStreamReader(createInputStream(), encoding);
        reader = new BufferedReader(isr);
    }
    return (reader);
  }

  public String getRealPath(String path) {
    return null;
  }

    @Override
    public int getRemotePort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getLocalAddr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getLocalPort() {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getRemoteAddr() {
    return null;
  }

  public String getRemoteHost() {
    return null;
  }

  public String getRemoteUser() {
    return null;
  }

  public RequestDispatcher getRequestDispatcher(String path) {
    return null;
  }

  public String getScheme() {
   return null;
  }

  public String getServerName() {
    return null;
  }

  public int getServerPort() {
    return 0;
  }

  public String getRequestedSessionId() {
    return null;
  }

  public String getRequestURI() {
    return requestURI;
  }

  public StringBuffer getRequestURL() {
    return null;
  }

  public HttpSession getSession() {
    return null;
  }

  public HttpSession getSession(boolean create) {
    return null;
  }

  public String getServletPath() {
    return null;
  }

  public Principal getUserPrincipal() {
    return null;
  }

  public boolean isRequestedSessionIdFromCookie() {
    return false;
  }

  public boolean isRequestedSessionIdFromUrl() {
    return isRequestedSessionIdFromURL();
  }

  public boolean isRequestedSessionIdFromURL() {
    return false;
  }

  public boolean isRequestedSessionIdValid() {
    return false;
  }

  public boolean isSecure() {
    return false;
  }

  public boolean isUserInRole(String role) {
    return false;
  }

  public void removeAttribute(String attribute) {
  }

  public void setAttribute(String key, Object value) {
  }

  /**
   * Set the authorization credentials sent with this request.
   *
   * @param authorization The new authorization credentials
   */
  public void setAuthorization(String authorization) {
    this.authorization = authorization;
  }

  public void setCharacterEncoding(String encoding) throws UnsupportedEncodingException {
  }
}
