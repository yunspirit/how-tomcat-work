package ex05.pyrmont.core;


import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import javax.naming.directory.DirContext;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.catalina.Cluster;
import org.apache.catalina.Container;
import org.apache.catalina.ContainerListener;
import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import org.apache.catalina.Logger;
import org.apache.catalina.Manager;
import org.apache.catalina.Mapper;
import org.apache.catalina.Pipeline;
import org.apache.catalina.Realm;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.deploy.ApplicationParameter;
import org.apache.catalina.deploy.ContextEjb;
import org.apache.catalina.deploy.ContextEnvironment;
import org.apache.catalina.deploy.ContextLocalEjb;
import org.apache.catalina.deploy.ContextResource;
import org.apache.catalina.deploy.ContextResourceLink;
import org.apache.catalina.deploy.ErrorPage;
import org.apache.catalina.deploy.FilterDef;
import org.apache.catalina.deploy.FilterMap;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.CharsetMapper;

//------------------------整体架构--------------------------
//SimpleContext表示一个上下文，它使用SimpleContextMapper作为它的mapper，
//SimpleContextValve作为它的基本阀门。
//该上下文包括两个阀门ClientIPLoggerValve和HeaderLoggerValve
//---------------------------------------------------------------------------
//        该上下文应用程序使用同一个加载器、两个阀门。
//        但是加载器和阀门时跟该上下文关联的，而不是跟包装器关联。
//        这样，两个加载器就可以都使用该加载器。该上下文被当做连接器的容器。
//        根据前面介绍的内容，其余的工作不难理解。
//         因此，连接器每次收到一个HTTP请求可以使用上下文的invoke方法。
//        1. 一个容器有一个流水线，容器的invoke方法会调用流水线的invoke方法。
//        2. 流水线的invoke方法会调用添加到容器中的阀门的invoke方法，然后调用基本阀门的invoke方法。
//        3. 在一个包装器中，基本阀门负责加载相关的servlet类并对请求作出相应。
//        4. 在一个有子容器的上下文中，基本阀门使用mapper来查找负责处理请求的子容器。
//        如果一个子容器被找到，子容器的invoke方法会被调用，然后返回步骤1。

public class SimpleContext implements Context, Pipeline {

  public SimpleContext() {
    pipeline.setBasic(new SimpleContextValve());
  }

//  用SimpleWrapper表示的两个包装器作为该上下文的子容器被添加。
//  包装器把SimpleWrapperValve作为它的基本阀门，但是没有其它的阀门了。
  protected HashMap children = new HashMap();
  protected Loader loader = null;
  protected SimplePipeline pipeline = new SimplePipeline(this);
  protected HashMap servletMappings = new HashMap();
  protected Mapper mapper = null;
//  根据请求不同 使用mapper保存映射
  protected HashMap mappers = new HashMap();
  private Container parent = null;

  public Object[] getApplicationListeners() {
    return null;
  }

  public void setApplicationListeners(Object listeners[]) {
  }

  public boolean getAvailable() {
    return false;
  }

  public void setAvailable(boolean flag) {
  }

  public CharsetMapper getCharsetMapper() {
    return null;
  }

  public void setCharsetMapper(CharsetMapper mapper) {
  }

  public boolean getConfigured() {
    return false;
  }

  public void setConfigured(boolean configured) {
  }

  public boolean getCookies() {
    return false;
  }

  public void setCookies(boolean cookies) {
  }

  public boolean getCrossContext() {
    return false;
  }

  public void setCrossContext(boolean crossContext) {
  }

  public String getDisplayName() {
    return null;
  }

  public void setDisplayName(String displayName) {
  }

  public boolean getDistributable() {
    return false;
  }

  public void setDistributable(boolean distributable) {
  }

  public String getDocBase() {
    return null;
  }

  public void setDocBase(String docBase) {
  }

  public LoginConfig getLoginConfig() {
    return null;
  }

  public void setLoginConfig(LoginConfig config) {
  }

  public NamingResources getNamingResources() {
    return null;
  }

  public void setNamingResources(NamingResources namingResources) {
  }

  public String getPath() {
    return null;
  }

  public void setPath(String path) {
  }

  public String getPublicId() {
    return null;
  }

  public void setPublicId(String publicId) {
  }

  public boolean getReloadable() {
    return false;
  }

  public void setReloadable(boolean reloadable) {
  }

  public boolean getOverride() {
    return false;
  }

  public void setOverride(boolean override) {
  }

  public boolean getPrivileged() {
    return false;
  }

  public void setPrivileged(boolean privileged) {
  }

  public ServletContext getServletContext() {
    return null;
  }

  public int getSessionTimeout() {
    return 0;
  }

  public void setSessionTimeout(int timeout) {
  }

  public String getWrapperClass() {
    return null;
  }

  public void setWrapperClass(String wrapperClass) {
  }

  public void addApplicationListener(String listener) {
  }

  public void addApplicationParameter(ApplicationParameter parameter) {
  }

  public void addConstraint(SecurityConstraint constraint) {
  }

  public void addEjb(ContextEjb ejb) {
  }

  public void addEnvironment(ContextEnvironment environment) {
  }

  public void addErrorPage(ErrorPage errorPage) {
  }

  public void addFilterDef(FilterDef filterDef) {
  }

  public void addFilterMap(FilterMap filterMap) {
  }

  public void addInstanceListener(String listener) {
  }

  public void addLocalEjb(ContextLocalEjb ejb) {
  }

  public void addMimeMapping(String extension, String mimeType) {
  }

  public void addParameter(String name, String value) {
  }

  public void addResource(ContextResource resource) {
  }

  public void addResourceEnvRef(String name, String type) {
  }

  public void addResourceLink(ContextResourceLink resourceLink) {
  }

  public void addRoleMapping(String role, String link) {
  }

  public void addSecurityRole(String role) {
  }

  public void addServletMapping(String pattern, String name) {
    synchronized (servletMappings) {
      servletMappings.put(pattern, name);
    }
  }

  public void addTaglib(String uri, String location) {
  }

  public void addWelcomeFile(String name) {
  }

  public void addWrapperLifecycle(String listener) {
  }

  public void addWrapperListener(String listener) {
  }

  public Wrapper createWrapper() {
    return null;
  }

  public String[] findApplicationListeners() {
    return null;
  }

  public ApplicationParameter[] findApplicationParameters() {
    return null;
  }

  public SecurityConstraint[] findConstraints() {
    return null;
  }

  public ContextEjb findEjb(String name) {
    return null;
  }

  public ContextEjb[] findEjbs() {
    return null;
  }

  public ContextEnvironment findEnvironment(String name) {
    return null;
  }

  public ContextEnvironment[] findEnvironments() {
    return null;
  }

  public ErrorPage findErrorPage(int errorCode) {
    return null;
  }

  public ErrorPage findErrorPage(String exceptionType) {
    return null;
  }

  public ErrorPage[] findErrorPages() {
    return null;
  }

  public FilterDef findFilterDef(String filterName) {
    return null;
  }

  public FilterDef[] findFilterDefs() {
    return null;
  }

  public FilterMap[] findFilterMaps() {
    return null;
  }

  public String[] findInstanceListeners() {
    return null;
  }

  public ContextLocalEjb findLocalEjb(String name) {
    return null;
  }

  public ContextLocalEjb[] findLocalEjbs() {
    return null;
  }

  public String findMimeMapping(String extension) {
    return null;
  }

  public String[] findMimeMappings() {
    return null;
  }

  public String findParameter(String name) {
    return null;
  }

  public String[] findParameters() {
    return null;
  }

  public ContextResource findResource(String name) {
    return null;
  }

  public String findResourceEnvRef(String name) {
    return null;
  }

  public String[] findResourceEnvRefs() {
    return null;
  }

  public ContextResourceLink findResourceLink(String name) {
    return null;
  }

  public ContextResourceLink[] findResourceLinks() {
    return null;
  }

  public ContextResource[] findResources() {
    return null;
  }

  public String findRoleMapping(String role) {
    return null;
  }

  public boolean findSecurityRole(String role) {
    return false;
  }

  public String[] findSecurityRoles() {
    return null;
  }

  public String findServletMapping(String pattern) {
    synchronized (servletMappings) {
      return ((String) servletMappings.get(pattern));
    }
  }

  public String[] findServletMappings() {
    return null;
  }

  public String findStatusPage(int status) {
    return null;
  }

  public int[] findStatusPages() {
    return null;
  }

  public String findTaglib(String uri) {
    return null;
  }

  public String[] findTaglibs() {
    return null;
  }

  public boolean findWelcomeFile(String name) {
    return false;
  }

  public String[] findWelcomeFiles() {
    return null;
  }

  public String[] findWrapperLifecycles() {
    return null;
  }

  public String[] findWrapperListeners() {
    return null;
  }

  public void reload() {
  }

  public void removeApplicationListener(String listener) {
  }

  public void removeApplicationParameter(String name) {
  }

  public void removeConstraint(SecurityConstraint constraint) {
  }

  public void removeEjb(String name) {
  }

  public void removeEnvironment(String name) {
  }

  public void removeErrorPage(ErrorPage errorPage) {
  }

  public void removeFilterDef(FilterDef filterDef) {
  }

  public void removeFilterMap(FilterMap filterMap) {
  }

  public void removeInstanceListener(String listener) {
  }

  public void removeLocalEjb(String name) {
  }

  public void removeMimeMapping(String extension) {
  }

  public void removeParameter(String name) {
  }

  public void removeResource(String name) {
  }

  public void removeResourceEnvRef(String name) {
  }

  public void removeResourceLink(String name) {
  }

  public void removeRoleMapping(String role) {
  }

  public void removeSecurityRole(String role) {
  }

  public void removeServletMapping(String pattern) {
  }

  public void removeTaglib(String uri) {
  }

  public void removeWelcomeFile(String name) {
  }

  public void removeWrapperLifecycle(String listener) {
  }

  public void removeWrapperListener(String listener) {
  }


  //methods of the Container interface
  public String getInfo() {
    return null;
  }

  public Loader getLoader() {
    if (loader != null)
      return (loader);
    if (parent != null)
      return (parent.getLoader());
    return (null);
  }

  public void setLoader(Loader loader) {
    this.loader = loader;
  }

  public Logger getLogger() {
    return null;
  }

  public void setLogger(Logger logger) {
  }

  public Manager getManager() {
    return null;
  }

  public void setManager(Manager manager) {
  }

  public Cluster getCluster() {
    return null;
  }

  public void setCluster(Cluster cluster) {
  }

  public String getName() {
    return null;
  }

  public void setName(String name) {
  }

  public Container getParent() {
    return null;
  }

  public void setParent(Container container) {
  }

  public ClassLoader getParentClassLoader() {
    return null;
  }

  public void setParentClassLoader(ClassLoader parent) {
  }

  public Realm getRealm() {
    return null;
  }

  public void setRealm(Realm realm) {
  }

  public DirContext getResources() {
    return null;
  }

  public void setResources(DirContext resources) {
  }

  public void addChild(Container child) {
    child.setParent((Container) this);
    children.put(child.getName(), child);
  }

  public void addContainerListener(ContainerListener listener) {
  }

  public void addMapper(Mapper mapper) {
    // this method is adopted from addMapper in ContainerBase
    // the first mapper added becomes the default mapper
    mapper.setContainer((Container) this);      // May throw IAE
    this.mapper = mapper;
    synchronized(mappers) {
      if (mappers.get(mapper.getProtocol()) != null)
        throw new IllegalArgumentException("addMapper:  Protocol '" +
          mapper.getProtocol() + "' is not unique");
      mapper.setContainer((Container) this);      // May throw IAE
      mappers.put(mapper.getProtocol(), mapper);
      if (mappers.size() == 1)
        this.mapper = mapper;
      else
        this.mapper = null;
    }
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
  }

  public Container findChild(String name) {
    if (name == null)
      return (null);
    synchronized (children) {       // Required by post-start changes
      return ((Container) children.get(name));
    }
  }

  public Container[] findChildren() {
    synchronized (children) {
      Container results[] = new Container[children.size()];
      return ((Container[]) children.values().toArray(results));
    }
  }

  public ContainerListener[] findContainerListeners() {
    return null;
  }

  public Mapper findMapper(String protocol) {
    // the default mapper will always be returned, if any,
    // regardless the value of protocol
    if (mapper != null)
      return (mapper);
    else
      synchronized (mappers) {
        return ((Mapper) mappers.get(protocol));
      }
  }

  public Mapper[] findMappers() {
    return null;
  }

  public void invoke(Request request, Response response)
    throws IOException, ServletException {
    pipeline.invoke(request, response);
  }

  public Container map(Request request, boolean update) {
    //this method is taken from the map method in org.apache.cataline.core.ContainerBase
    //the findMapper method always returns the default mapper, if any, regardless the
    //request's protocol
    Mapper mapper = findMapper(request.getRequest().getProtocol());
    if (mapper == null)
      return (null);

    // Use this Mapper to perform this mapping
    return (mapper.map(request, update));
  }

  public void removeChild(Container child) {
  }

  public void removeContainerListener(ContainerListener listener) {
  }

  public void removeMapper(Mapper mapper) {
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
  }

  // method implementations of Pipeline
  public Valve getBasic() {
    return pipeline.getBasic();
  }

  public void setBasic(Valve valve) {
    pipeline.setBasic(valve);
  }

  public synchronized void addValve(Valve valve) {
    pipeline.addValve(valve);
  }

  public Valve[] getValves() {
    return pipeline.getValves();
  }

  public void removeValve(Valve valve) {
    pipeline.removeValve(valve);
  }

}