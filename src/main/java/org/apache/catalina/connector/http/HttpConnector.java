package org.apache.catalina.connector.http;


import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.AccessControlException;
import java.util.Stack;
import java.util.Vector;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.KeyManagementException;
import org.apache.catalina.Connector;
import org.apache.catalina.Container;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Logger;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Service;
import org.apache.catalina.net.DefaultServerSocketFactory;
import org.apache.catalina.net.ServerSocketFactory;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;


/**
 * Implementation of an HTTP/1.1 connector.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.34 $ $Date: 2002/03/18 07:15:39 $
 * @deprecated
 */

//在这个接口的众多方法中，最重要的是getContainer,setContainer, createRequest和createResponse。
//        setContainer是用来关联连接器和容器用的。getContainer返回关联的容器。
//        createRequest为前来的HTTP请求构造一个请求对象，而createResponse创建一个响应对象。
//    Connector知道Container但反过来就不成立了

//    Lifecycle将在第6章中解释，现在你不需要担心它，只要明白通过实现Lifecycle,
//            在你创建HttpConnector实例之后，

//            你应该 调用它的initialize和start方法。这两个方法在组件的生命周期里必须只调用一次。
public final class HttpConnector
    implements Connector, Lifecycle, Runnable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The <code>Service</code> we are associated with (if any).
     */
    private Service service = null;


    /**
     * The accept count for this Connector.
     */
    private int acceptCount = 10;


    /**
     * The IP address on which to bind, if any.  If <code>null</code>, all
     * addresses on the server will be bound.
     */
    private String address = null;


    /**
     * The input buffer size we should create on input streams.
     */
    private int bufferSize = 2048;


    /**
     * The Container used for processing requests received by this Connector.
     */
    protected Container container = null;


    /**
     * The set of processors that have ever been created.
     */
    private Vector created = new Vector();


    /**
     * The current number of processors that have been created.
     */
    private int curProcessors = 0;


    /**
     * The debugging detail level for this component.
     */
    private int debug = 0;


    /**
     * The "enable DNS lookups" flag for this Connector.
     */
    private boolean enableLookups = false;


    /**
     * The server socket factory for this component.
     */
    private ServerSocketFactory factory = null;


    /**
     * Descriptive information about this Connector implementation.
     */
    private static final String info =
        "org.apache.catalina.connector.http.HttpConnector/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);


    /**
     * The minimum number of processors to start at initialization time.
     */
    protected int minProcessors = 5;


    /**
     * The maximum number of processors allowed, or <0 for unlimited.
     */
    private int maxProcessors = 20;


    /**
     * Timeout value on the incoming connection.
     * Note : a value of 0 means no timeout.
     */
    private int connectionTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT;


    /**
     * The port number on which we listen for HTTP requests.
     */
    private int port = 8080;


    /**
     * The set of processors that have been created but are not currently
     * being used to process a request.
     */
//    while (curProcessors < minProcessors)
//    HttpConnector拥有一个HttpProcessor对象池，每个HttpProcessor实例拥有一个独立线程。
//    因此，HttpConnector可以同时处理多个HTTP请求。
//    HttpConnector维护一个HttpProcessor的实例池，从而避免每次创建HttpProcessor实例。
//    这些HttpProcessor实例是存放在一个叫processors的java.io.Stack中：
    private Stack processors = new Stack();


    /**
     * The server name to which we should pretend requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the server name included in the <code>Host</code> header is used.
     */
    private String proxyName = null;


    /**
     * The server port to which we should pretent requests to this Connector
     * were directed.  This is useful when operating Tomcat behind a proxy
     * server, so that redirects get constructed accurately.  If not specified,
     * the port number specified by the <code>port</code> property is used.
     */
    private int proxyPort = 0;


    /**
     * The redirect port for non-SSL to SSL redirects.
     */
    private int redirectPort = 443;


    /**
     * The request scheme that will be set on all requests received
     * through this connector.
     */
    private String scheme = "http";


    /**
     * The secure connection flag that will be set on all requests received
     * through this connector.
     */
    private boolean secure = false;


    /**
     * The server socket through which we listen for incoming TCP connections.
     */
    private ServerSocket serverSocket = null;


    /**
     * The string manager for this package.
     */
    private StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been initialized yet?
     */
    private boolean initialized = false;


    /**
     * Has this component been started yet?
     */
    private boolean started = false;


    /**
     * The shutdown signal to our background thread
     */
    private boolean stopped = false;


    /**
     * The background thread.
     */
    private Thread thread = null;


    /**
     * The name to register for the background thread.
     */
    private String threadName = null;


    /**
     * The thread synchronization object.
     */
    private Object threadSync = new Object();


    /**
     * Is chunking allowed ?
     */
    private boolean allowChunking = true;


    /**
     * Use TCP no delay ?
     */
    private boolean tcpNoDelay = true;


    // ------------------------------------------------------------- Properties


    /**
     * Return the <code>Service</code> with which we are associated (if any).
     */
    public Service getService() {

        return (this.service);

    }


    /**
     * Set the <code>Service</code> with which we are associated (if any).
     *
     * @param service The service that owns this Engine
     */
    public void setService(Service service) {

        this.service = service;

    }


    /**
     * Return the connection timeout for this Connector.
     */
    public int getConnectionTimeout() {

        return (connectionTimeout);

    }


//    /**
//     * Set the connection timeout for this Connector.
//     *
//     * @param count The new connection timeout
//     */
    public void setConnectionTimeout(int connectionTimeout) {

        this.connectionTimeout = connectionTimeout;

    }


    /**
     * Return the accept count for this Connector.
     */
    public int getAcceptCount() {

        return (acceptCount);

    }


    /**
     * Set the accept count for this Connector.
     *
     * @param count The new accept count
     */
    public void setAcceptCount(int count) {

        this.acceptCount = count;

    }


    /**
     * Get the allow chunking flag.
     */
    public boolean isChunkingAllowed() {

        return (allowChunking);

    }


    /**
     * Get the allow chunking flag.
     */
    public boolean getAllowChunking() {

        return isChunkingAllowed();

    }


    /**
     * Set the allow chunking flag.
     *
     * @param allowChunking Allow chunking flag
     */
    public void setAllowChunking(boolean allowChunking) {

        this.allowChunking = allowChunking;

    }


    /**
     * Return the bind IP address for this Connector.
     */
    public String getAddress() {

        return (this.address);

    }


    /**
     * Set the bind IP address for this Connector.
     *
     * @param address The bind IP address
     */
    public void setAddress(String address) {

        this.address = address;

    }


    /**
     * Is this connector available for processing requests?
     */
    public boolean isAvailable() {

        return (started);

    }


    /**
     * Return the input buffer size for this Connector.
     */
    public int getBufferSize() {

        return (this.bufferSize);

    }


    /**
     * Set the input buffer size for this Connector.
     *
     * @param bufferSize The new input buffer size.
     */
    public void setBufferSize(int bufferSize) {

        this.bufferSize = bufferSize;

    }


    /**
     * Return the Container used for processing requests received by this
     * Connector.
     */
    public Container getContainer() {

        return (container);

    }


    /**
     * Set the Container used for processing requests received by this
     * Connector.
     *
     * @param container The new Container to use
     */
    public void setContainer(Container container) {

        this.container = container;

    }


    /**
     * Return the current number of processors that have been created.
     */
    public int getCurProcessors() {

        return (curProcessors);

    }


    /**
     * Return the debugging detail level for this component.
     */
    public int getDebug() {

        return (debug);

    }


    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {

        this.debug = debug;

    }


    /**
     * Return the "enable DNS lookups" flag.
     */
    public boolean getEnableLookups() {

        return (this.enableLookups);

    }


    /**
     * Set the "enable DNS lookups" flag.
     *
     * @param enableLookups The new "enable DNS lookups" flag value
     */
    public void setEnableLookups(boolean enableLookups) {

        this.enableLookups = enableLookups;

    }


    /**
     * Return the server socket factory used by this Container.
     */
    public ServerSocketFactory getFactory() {

        if (this.factory == null) {
            synchronized (this) {
                this.factory = new DefaultServerSocketFactory();
            }
        }
        return (this.factory);

    }


    /**
     * Set the server socket factory used by this Container.
     *
     * @param factory The new server socket factory
     */
    public void setFactory(ServerSocketFactory factory) {

        this.factory = factory;

    }


    /**
     * Return descriptive information about this Connector implementation.
     */
    public String getInfo() {

        return (info);

    }


    /**
     * Return the minimum number of processors to start at initialization.
     */
    public int getMinProcessors() {

        return (minProcessors);

    }


    /**
     * Set the minimum number of processors to start at initialization.
     *
     * @param minProcessors The new minimum processors
     */
    public void setMinProcessors(int minProcessors) {

        this.minProcessors = minProcessors;

    }


    /**
     * Return the maximum number of processors allowed, or <0 for unlimited.
     */
    public int getMaxProcessors() {

        return (maxProcessors);

    }


    /**
     * Set the maximum number of processors allowed, or <0 for unlimited.
     *
     * @param maxProcessors The new maximum processors
     */
    public void setMaxProcessors(int maxProcessors) {

        this.maxProcessors = maxProcessors;

    }


    /**
     * Return the port number on which we listen for HTTP requests.
     */
    public int getPort() {

        return (this.port);

    }


    /**
     * Set the port number on which we listen for HTTP requests.
     *
     * @param port The new port number
     */
    public void setPort(int port) {

        this.port = port;

    }


    /**
     * Return the proxy server name for this Connector.
     */
    public String getProxyName() {

        return (this.proxyName);

    }


    /**
     * Set the proxy server name for this Connector.
     *
     * @param proxyName The new proxy server name
     */
    public void setProxyName(String proxyName) {

        this.proxyName = proxyName;

    }


    /**
     * Return the proxy server port for this Connector.
     */
    public int getProxyPort() {

        return (this.proxyPort);

    }


    /**
     * Set the proxy server port for this Connector.
     *
     * @param proxyPort The new proxy server port
     */
    public void setProxyPort(int proxyPort) {

        this.proxyPort = proxyPort;

    }


    /**
     * Return the port number to which a request should be redirected if
     * it comes in on a non-SSL port and is subject to a security constraint
     * with a transport guarantee that requires SSL.
     */
    public int getRedirectPort() {

        return (this.redirectPort);

    }


    /**
     * Set the redirect port number.
     *
     * @param redirectPort The redirect port number (non-SSL to SSL)
     */
    public void setRedirectPort(int redirectPort) {

        this.redirectPort = redirectPort;

    }


    /**
     * Return the scheme that will be assigned to requests received
     * through this connector.  Default value is "http".
     */
    public String getScheme() {

        return (this.scheme);

    }


    /**
     * Set the scheme that will be assigned to requests received through
     * this connector.
     *
     * @param scheme The new scheme
     */
    public void setScheme(String scheme) {

        this.scheme = scheme;

    }


    /**
     * Return the secure connection flag that will be assigned to requests
     * received through this connector.  Default value is "false".
     */
    public boolean getSecure() {

        return (this.secure);

    }


    /**
     * Set the secure connection flag that will be assigned to requests
     * received through this connector.
     *
     * @param secure The new secure connection flag
     */
    public void setSecure(boolean secure) {

        this.secure = secure;

    }


    /**
     * Return the TCP no delay flag value.
     */
    public boolean getTcpNoDelay() {

        return (this.tcpNoDelay);

    }


    /**
     * Set the TCP no delay flag which will be set on the socket after
     * accepting a connection.
     *
     * @param tcpNoDelay The new TCP no delay flag
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {

        this.tcpNoDelay = tcpNoDelay;

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Create (or allocate) and return a Request object suitable for
     * specifying the contents of a Request to the responsible Container.
     */
    public Request createRequest() {

        //        if (debug >= 2)
        //            log("createRequest: Creating new request");
        HttpRequestImpl request = new HttpRequestImpl();
        request.setConnector(this);
        return (request);

    }


    /**
     * Create (or allocate) and return a Response object suitable for
     * receiving the contents of a Response from the responsible Container.
     */
    public Response createResponse() {

        //        if (debug >= 2)
        //            log("createResponse: Creating new response");
        HttpResponseImpl response = new HttpResponseImpl();
        response.setConnector(this);
        return (response);

    }


    // -------------------------------------------------------- Package Methods


    /**
     * Recycle the specified Processor so that it can be used again.
     *
     * @param processor The processor to be recycled
     */
//    recycle方法把HttpProcessor队会栈。
    void recycle(HttpProcessor processor) {

        //        if (debug >= 2)
        //            log("recycle: Recycling processor " + processor);
        processors.push(processor);

    }


    // -------------------------------------------------------- Private Methods


    /**
     * Create (or allocate) and return an available processor for use in
     * processing a specific HTTP request, if possible.  If the maximum
     * allowed processors have already been created and are in use, return
     * <code>null</code> instead.
     */
//    当池子里的proccesor不够  就创建新的processor
    private HttpProcessor createProcessor() {

//        然而，大部分时候createProcessor方法并不创建一个新的HttpProcessor对象。
//        相反，它从池子中获取一个。如果在栈中已经存在一 个HttpProcessor实例，createProcessor将弹出一个。
//        如果栈是空的并且没有超过HttpProcessor实例的最大数 量，
//        createProcessor将会创建一个。然而，如果已经达到最大数量的话，createProcessor将会返回null。
        synchronized (processors) {
            if (processors.size() > 0) {
                // if (debug >= 2)
                // log("createProcessor: Reusing existing processor");
                return ((HttpProcessor) processors.pop());
            }
//        HttpConnector对象创建minProcessors个HttpProcessor实例。
//        如果一次有比HtppProcessor 实例更多的请求需要处理时，
//        HttpConnector创建更多的HttpProcessor实例，直到实例数量达到maxProcessors个。
//        在到 达这点之后，仍不够HttpProcessor实例的话，请来的请求将会给忽略掉。
//        如果你想让HttpConnector继续创建 HttpProcessor实例的话，把maxProcessors设置为一个负数。
//        还有就是变量curProcessors保存了 HttpProcessor实例的当前数量。
            if ((maxProcessors > 0) && (curProcessors < maxProcessors)) {
                // if (debug >= 2)
                // log("createProcessor: Creating new processor");
                return (newProcessor());
            } else {
                if (maxProcessors < 0) {
                    // if (debug >= 2)
                    // log("createProcessor: Creating new processor");
                    return (newProcessor());
                } else {
                    // if (debug >= 2)
                    // log("createProcessor: Cannot create new processor");
                    return (null);
                }
            }
        }

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null)
            localName = "HttpConnector";
        if (logger != null)
            logger.log(localName + " " + message);
        else
            System.out.println(localName + " " + message);

    }


    /**
     * Log a message on the Logger associated with our Container (if any).
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    private void log(String message, Throwable throwable) {

        Logger logger = container.getLogger();
        String localName = threadName;
        if (localName == null)
            localName = "HttpConnector";
        if (logger != null)
            logger.log(localName + " " + message, throwable);
        else {
            System.out.println(localName + " " + message);
            throwable.printStackTrace(System.out);
        }

    }


    /**
     * Create and return a new processor suitable for processing HTTP
     * requests and returning the corresponding responses.
     */
//    newProcessor方法构造一个HttpProcessor对象并增加curProcessors。
    private HttpProcessor newProcessor() {

        //        if (debug >= 2)
        //            log("newProcessor: Creating new processor");
        HttpProcessor processor = new HttpProcessor(this, curProcessors++);
        if (processor instanceof Lifecycle) {
            try {

//                processor开始之后  会一直运行
                ((Lifecycle) processor).start();
            } catch (LifecycleException e) {
                log("newProcessor", e);
                return (null);
            }
        }
//        保曾经创建过的proccessor
        created.addElement(processor);
        return (processor);

    }


    /**
     * Open and return the server socket for this Connector.  If an IP
     * address has been specified, the socket will be opened only on that
     * address; otherwise it will be opened on all addresses.
     *
     * @exception java.io.IOException                input/output or network error
     * @exception java.security.KeyStoreException          error instantiating the
     *                                       KeyStore from file (SSL only)
     * @exception java.security.NoSuchAlgorithmException   KeyStore algorithm unsupported
     *                                       by current provider (SSL only)
     * @exception java.security.cert.CertificateException       general certificate error (SSL only)
     * @exception java.security.UnrecoverableKeyException  internal KeyStore problem with
     *                                       the certificate (SSL only)
     * @exception java.security.KeyManagementException     problem in the key management
     *                                       layer (SSL only)
     */
    private ServerSocket open()
    throws IOException, KeyStoreException, NoSuchAlgorithmException,
           CertificateException, UnrecoverableKeyException,
           KeyManagementException
    {
//        open方法是从一个服务端套接字工厂中获得一个 ServerSocket实例。
        // Acquire the server socket factory for this Connector
        ServerSocketFactory factory = getFactory();

        // If no address is specified, open a connection on all addresses
        if (address == null) {
            log(sm.getString("httpConnector.allAddresses"));
            try {
                return (factory.createSocket(port, acceptCount));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + port);
            }
        }

        // Open a server socket on the specified address
        try {
            InetAddress is = InetAddress.getByName(address);
            log(sm.getString("httpConnector.anAddress", address));
            try {
                return (factory.createSocket(port, acceptCount, is));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + address +
                                        ":" + port);
            }
        } catch (Exception e) {
            log(sm.getString("httpConnector.noAddress", address));
            try {
                return (factory.createSocket(port, acceptCount));
            } catch (BindException be) {
                throw new BindException(be.getMessage() + ":" + port);
            }
        }
    }


    // ---------------------------------------------- Background Thread Methods


    /**
     * The background thread that listens for incoming TCP/IP connections and
     * hands them off to an appropriate processor.
     */
    public void run() {
//        Loop until we receive a shutdown command
//        run方法在一个服务端套接字等待HTTP请求的地方存在一个while循环，
//        一直运行直至HttpConnector被关闭了。
        while (!stopped) {
            // Accept the next incoming connection from the server socket
            Socket socket = null;
            try {
                //                if (debug >= 3)
                //                    log("run: Waiting on serverSocket.accept()");
                socket = serverSocket.accept();
                //                if (debug >= 3)
                //                    log("run: Returned from serverSocket.accept()");
                if (connectionTimeout > 0)   socket.setSoTimeout(connectionTimeout);
                socket.setTcpNoDelay(tcpNoDelay);
            } catch (AccessControlException ace) {
                log("socket accept security exception", ace);
                continue;
            } catch (IOException e) {
                //                if (debug >= 3)
                //                    log("run: Accept returned IOException", e);
                try {
                    // If reopening fails, exit
                    synchronized (threadSync) {
                        if (started && !stopped)
                            log("accept error: ", e);
                        if (!stopped) {
                            //                    if (debug >= 3)
                            //                        log("run: Closing server socket");
                            serverSocket.close();
                            //                        if (debug >= 3)
                            //                            log("run: Reopening server socket");
                            serverSocket = open();
                        }
                    }
                    // if (debug >= 3)
                    // log("run: IOException processing completed");
                } catch (IOException ioe) {
                    log("socket reopen, io problem: ", ioe);
                    break;
                } catch (KeyStoreException kse) {
                    log("socket reopen, keystore problem: ", kse);
                    break;
                } catch (NoSuchAlgorithmException nsae) {
                    log("socket reopen, keystore algorithm problem: ", nsae);
                    break;
                } catch (CertificateException ce) {
                    log("socket reopen, certificate problem: ", ce);
                    break;
                } catch (UnrecoverableKeyException uke) {
                    log("socket reopen, unrecoverable key: ", uke);
                    break;
                } catch (KeyManagementException kme) {
                    log("socket reopen, key management problem: ", kme);
                    break;
                }
                continue;
            }

            // Hand this socket off to an appropriate processor
//            对每个前来的HTTP请求，会通过调用私有方法createProcessor获得一个HttpProcessor实例。
            HttpProcessor processor = createProcessor();
//             不能创建processor的话  套接字将会简单关闭并且前来的HTTP请求不会被处理。
            if (processor == null) {
                try {
                    log(sm.getString("httpConnector.noProcessor"));
                    socket.close();
                } catch (IOException e) {
                    ;
                }
                continue;
            }
            //            if (debug >= 3)
            //                log("run: Assigning socket to processor " + processor);
//      如果createProcessor不是返回null，客户端套接字会传递给HttpProcessor类的assign方法：
            processor.assign(socket);

            // The processor will recycle itself when it finishes

        }

        // Notify the threadStop() method that we have shut ourselves down
        //        if (debug >= 3)
        //            log("run: Notifying threadStop() that we have shut down");
        synchronized (threadSync) {
            threadSync.notifyAll();
        }

    }


    /**
     * Start the background processing thread.
     */
    private void threadStart() {

        log(sm.getString("httpConnector.starting"));

        thread = new Thread(this, threadName);
        thread.setDaemon(true);
        thread.start();

    }


    /**
     * Stop the background processing thread.
     */
    private void threadStop() {

        log(sm.getString("httpConnector.stopping"));

        stopped = true;
        try {
            threadSync.wait(5000);
        } catch (InterruptedException e) {
            ;
        }
        thread = null;

    }


    // ------------------------------------------------------ Lifecycle Methods


    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /**
     * Get the lifecycle listeners associated with this lifecycle. If this
     * Lifecycle has no listeners registered, a zero-length array is returned.
     */
    public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to add
     */
    public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /**
     * Initialize this connector (create ServerSocket here!)
     */
    public void initialize()
    throws LifecycleException {
        if (initialized)
            throw new LifecycleException (
                sm.getString("httpConnector.alreadyInitialized"));

        this.initialized=true;
        Exception eRethrow = null;

        // Establish a server socket on the specified port
        try {
            serverSocket = open();
        } catch (IOException ioe) {
            log("httpConnector, io problem: ", ioe);
            eRethrow = ioe;
        } catch (KeyStoreException kse) {
            log("httpConnector, keystore problem: ", kse);
            eRethrow = kse;
        } catch (NoSuchAlgorithmException nsae) {
            log("httpConnector, keystore algorithm problem: ", nsae);
            eRethrow = nsae;
        } catch (CertificateException ce) {
            log("httpConnector, certificate problem: ", ce);
            eRethrow = ce;
        } catch (UnrecoverableKeyException uke) {
            log("httpConnector, unrecoverable key: ", uke);
            eRethrow = uke;
        } catch (KeyManagementException kme) {
            log("httpConnector, key management problem: ", kme);
            eRethrow = kme;
        }
        if ( eRethrow != null )
            throw new LifecycleException(threadName + ".open", eRethrow);
    }


    /**
     * Begin processing requests via this Connector.
     *
     * @exception org.apache.catalina.LifecycleException if a fatal startup error occurs
     */
// 启动connector的时候  同时创建minProcessors个processor实例
    public void start() throws LifecycleException {

        // Validate and update our current state
        if (started)
            throw new LifecycleException
                (sm.getString("httpConnector.alreadyStarted"));
        threadName = "HttpConnector[" + port + "]";
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Start our background thread
        threadStart();

//        开始的时候，HttpConnector对象创建minProcessors个HttpProcessor实例。
//        如果一次有比HtppProcessor 实例更多的请求需要处理时，HttpConnector创建更多的HttpProcessor实例，直到实例数量达到maxProcessors个。
//        在到 达这点之后，仍不够HttpProcessor实例的话，请来的请求将会给忽略掉。
//        如果你想让HttpConnector继续创建 HttpProcessor实例的话，把maxProcessors设置为一个负数。
//        还有就是变量curProcessors保存了 HttpProcessor实例的当前数量。
        // Create the specified minimum number of processors
        while (curProcessors < minProcessors) {
            if ((maxProcessors > 0) && (curProcessors >= maxProcessors))
                break;
            HttpProcessor processor = newProcessor();
            recycle(processor);
        }

    }


    /**
     * Terminate processing requests via this Connector.
     *
     * @exception org.apache.catalina.LifecycleException if a fatal shutdown error occurs
     */
    public void stop() throws LifecycleException {

        // Validate and update our current state
        if (!started)
            throw new LifecycleException
                (sm.getString("httpConnector.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Gracefully shut down all processors we have created
        for (int i = created.size() - 1; i >= 0; i--) {
            HttpProcessor processor = (HttpProcessor) created.elementAt(i);
            if (processor instanceof Lifecycle) {
                try {
                    ((Lifecycle) processor).stop();
                } catch (LifecycleException e) {
                    log("HttpConnector.stop", e);
                }
            }
        }

        synchronized (threadSync) {
            // Close the server socket we were using
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    ;
                }
            }
            // Stop our background thread
            threadStop();
        }
        serverSocket = null;

    }


}
