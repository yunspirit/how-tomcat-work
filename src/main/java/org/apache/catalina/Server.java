/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Server.java,v 1.10 2002/03/06 06:49:10 craigmcc Exp $
 * $Revision: 1.10 $
 * $Date: 2002/03/06 06:49:10 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */


package org.apache.catalina;

import org.apache.catalina.deploy.NamingResources;

/**
 * A <code>Server</code> element represents the entire Catalina
 * servlet container.  Its attributes represent the characteristics of
 * the servlet container as a whole.  A <code>Server</code> may contain
 * one or more <code>Services</code>, and the top level set of naming
 * resources.
 * <p>
 * Normally, an implementation of this interface will also implement
 * <code>Lifecycle</code>, such that when the <code>start()</code> and
 * <code>stop()</code> methods are called, all of the defined
 * <code>Services</code> are also started or stopped.
 * <p>
 * In between, the implementation must open a server socket on the port number
 * specified by the <code>port</code> property.  When a connection is accepted,
 * the first line is read and compared with the specified shutdown command.
 * If the command matches, shutdown of the server is initiated.
 * <p>
 * <strong>NOTE</strong> - The concrete implementation of this class should
 * register the (singleton) instance with the <code>ServerFactory</code>
 * class in its constructor(s).
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.10 $ $Date: 2002/03/06 06:49:10 $
 */

//org.apache.catalina.Server接口表示整个Catalina Servlet容器以及其它组件。
//     一个服务器相当有用，因为它提供了一种优雅的机制来启动和停止整个系统。
//     不必再单独的启动连接器和容器了。
// ---------------------------------------------------------------工作过程---------------------------
//   1、org.apache.catalina.Server接口表示整个Catalina Servlet容器以及其它组件。
//     一个服务器相当有用，因为它提供了一种优雅的机制来启动和停止整个系统。
//     不必再单独的启动连接器和容器了。
//   2、无限期的等待关闭命令
//   3、如果你想要关闭系统，发送一个关闭命令道指定端口即可。
//            当服务器收到正确的关闭指令后，
// 它停止所有组件的服务。
//    -------------------------------------------------------------概括---------------
//    server的作用是，管理所有的service，一个server可以包括多个service。
//    server负责管理所有service的生命周期，
//    这样就管理了所有的connector和container，以及connector和container的所有内部组件。
//    这样就不需要单独对connector和container单独进行开启或关闭了。
public interface Server {


    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Server implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


    /**
     * Return the global naming resources.
     */
    public NamingResources getGlobalNamingResources();


    /**
     * Set the global naming resources.
     * 
     * @param namingResources The new global naming resources
     */
    public void setGlobalNamingResources
    (NamingResources globalNamingResources);


    /**
     * Return the port number we listen to for shutdown commands.
     */
//  属性port则是服务器等待关闭命令的端口
    public int getPort();


    /**
     * Set the port number we listen to for shutdown commands.
     *
     * @param port The new port number
     */
    public void setPort(int port);


    /**
     * Return the shutdown command string we are waiting for.
     */
//    属性shutdown用来持有一个停止服务的指令。
    public String getShutdown();


    /**
     * Set the shutdown command we are waiting for.
     *
     * @param shutdown The new shutdown command
     */
    public void setShutdown(String shutdown);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a new Service to the set of defined Services.
     *
     * @param service The Service to be added
     */
//    可以调用服务器的addService方法将服务添加到服务
    public void addService(Service service);


    /**
     * Wait until a proper shutdown command is received, then return.
     */
    public void await();


    /**
     * Return the specified Service (if it exists); otherwise return
     * <code>null</code>.
     *
     * @param name Name of the Service to be returned
     */
//   findServices返回所有服务器中所有的服务
    public Service findService(String name);


    /**
     * Return the set of Services defined within this Server.
     */
    public Service[] findServices();


    /**
     * Remove the specified Service from the set associated from this
     * Server.
     *
     * @param service The Service to be removed
     */
//   使用removeService方法将服务删除
    public void removeService(Service service);

    /**
     * Invoke a pre-startup initialization. This is used to allow connectors
     * to bind to restricted ports under Unix operating environments.
     *
     * @exception org.apache.catalina.LifecycleException If this server was already initialized.
     */
//    Initialize方法包括在启动之前需要执行的代码。
    public void initialize()
    throws LifecycleException;
}
