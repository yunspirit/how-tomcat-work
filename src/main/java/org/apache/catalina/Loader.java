/*
 * $Header: /home/cvs/jakarta-tomcat-4.0/catalina/src/share/org/apache/catalina/Loader.java,v 1.6 2002/09/19 22:55:47 amyroh Exp $
 * $Revision: 1.6 $
 * $Date: 2002/09/19 22:55:47 $
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


import java.beans.PropertyChangeListener;


/**
 * A <b>Loader</b> represents a Java ClassLoader implementation that can
 * be used by a Container to load class files (within a repository associated
 * with the Loader) that are designed to be reloaded upon request, as well as
 * a mechanism to detect whether changes have occurred in the underlying
 * repository.
 * <p>
 * In order for a <code>Loader</code> implementation to successfully operate
 * with a <code>Context</code> implementation that implements reloading, it
 * must obey the following constraints:
 * <ul>
 * <li>Must implement <code>Lifecycle</code> so that the Context can indicate
 *     that a new class loader is required.
 * <li>The <code>start()</code> method must unconditionally create a new
 *     <code>ClassLoader</code> implementation.
 * <li>The <code>stop()</code> method must throw away its reference to the
 *     <code>ClassLoader</code> previously utilized, so that the class loader,
 *     all classes loaded by it, and all objects of those classes, can be
 *     garbage collected.
 * <li>Must allow a call to <code>stop()</code> to be followed by a call to
 *     <code>start()</code> on the same <code>Loader</code> instance.
 * <li>Based on a policy chosen by the implementation, must call the
 *     <code>Context.reload()</code> method on the owning <code>Context</code>
 *     when a change to one or more of the class files loaded by this class
 *     loader is detected.
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2002/09/19 22:55:47 $
 */
//像前面章节中那样使用系统的加载器来加载servlet和其他需要的类，
//        这样servlet就可以进入Java虚拟机CLASSPATH环境下面的任何类和类库，
//        这会带来安全隐患。
//        Servlet只允许访问WEB-INF/目录及其子目录下面的类以及部署在WEB-INF/lib目录 下的类库。
//        所以一个servlet容器需要一个自己的加载器，该加载器遵守一些特定的规则来加载类。
//        在Catalina中，加载器使用org.apache.catalina.Loader接口表示。

//    在Web应用程序中加载servlet和其他类需要遵循一些规则。
//            例如，在一个应用程序中Servlet可以使用部署到WEB-INF/classes目录和任何子目录下面的类。
//            然而，没有servlet的不能访问其他类，即使这些类是在运行Tomcat 所在的JVM的CLASSPATH中。
//  一个servlet只能访问WEB-INF/lib目录下的类库，而不能访问其他目录下面的。
//  --------->>>>重点>>>>>>>>>一个Tomcat类加载器表示一个Web应用程序加载器，而不是一个类加载器。
//  一个加载器必须实现org.apache.catalina.Loader接口。
//   加载器的实现使用定制的类加载器org.apache.catalina.loader.WebappClassLoader。
public interface Loader {


    // ------------------------------------------------------------- Properties


    /**
     * Return the Java class loader to be used by this Container.
     */
    public ClassLoader getClassLoader();


    /**
     * Return the Container with which this Loader has been associated.
     */
//    与上下文相关联
    public Container getContainer();


    /**
     * Set the Container with which this Loader has been associated.
     *
     * @param container The associated Container
     */
//    与上下文相关联
    public void setContainer(Container container);


    /**
     * Return the DefaultContext with which this Manager is associated.
     */
    public DefaultContext getDefaultContext();


    /**
     * Set the DefaultContext with which this Manager is associated.
     *
     * @param defaultContext The newly associated DefaultContext
     */
    public void setDefaultContext(DefaultContext defaultContext);
    

    /**
     * Return the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     */
//  一个加载器的实现可以确定是否委派给父加载器
    public boolean getDelegate();


    /**
     * Set the "follow standard delegation model" flag used to configure
     * our ClassLoader.
     *
     * @param delegate The new flag
     */
//  一个加载器的实现可以确定是否委派给父加载器类
    public void setDelegate(boolean delegate);


    /**
     * Return descriptive information about this Loader implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo();


// 另外两种方法，setReloadable和getReloadable，用于确定加载器中是否可以使用重加载。
// 默认情况下，在标准的上下文实现中（org.apache.catalina.core.StandardContext类将在第12章讨论）重载机制并未启用。
// 因此，要使得上下文启动重载机制，需要在server.xml文件添加一些元素如下：
// <Context path="/myApp" docBase="myApp" debug="0" reloadable="true"/>
    /**
     * Return the reloadable flag for this Loader.
     */
//    setReloadable和getReloadable，用于确定加载器中是否可以使用重加载
    public boolean getReloadable();


    /**
     * Set the reloadable flag for this Loader.
     *
     * @param reloadable The new reloadable flag
     */
//    setReloadable和getReloadable，用于确定加载器中是否可以使用重加载
    public void setReloadable(boolean reloadable);


    // --------------------------------------------------------- Public Methods


    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);


    /**
     * Add a new repository to the set of repositories for this class loader.
     *
     * @param repository Repository to be added
     */
//    用于添加一个库
    public void addRepository(String repository);


    /**
     * Return the set of repositories defined for this class loader.
     * If none are defined, a zero-length array is returned.
     */
//    用于返回一个所有库的队列
    public String[] findRepositories();


    /**
     * Has the internal repository associated with this Loader been modified,
     * such that the loaded classes should be reloaded?
     */
//    一个servlet程序员可以重新编译servlet或辅助类，新类将被重新加载而不需要重新启动Tomcat加载。
//    为了达到重新加载的目的，Loader接口有修改方法。
//    在加载器的实现中，如果在其库中一个或多个类别已被修改，modeified方法必须返回true，因此需要重新加载。
//    一个加载器自己并不进行重新加载，而是调用上下文接口的重载方法。
//
    public boolean modified();


    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);


}
