package org.apache.catalina.startup;


import java.io.File;
import java.lang.reflect.Method;


/**
 * Boostrap loader for Catalina.  This application constructs a class loader
 * for use in loading the Catalina internal classes (by accumulating all of the
 * JAR files found in the "server" directory under "catalina.home"), and
 * starts the regular execution of the container.  The purpose of this
 * roundabout approach is to keep the Catalina internal classes (and any
 * other classes they depend on, such as an XML parser) out of the system
 * class path and therefore not visible to application level classes.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.36 $ $Date: 2002/04/01 19:51:31 $
 */

//1、Bootstrap类创建一个Catalina的实例并调用它的process方法。
// 理论上，这两个类可以合成一个类。
// 但是，为了支持Tomcat的多模式启动，提供了多个引导类。

// 2、Bootstrap类提供了Tomcat的启动入口。当你运行startup.bat或者是startup.sh的时候，
//           实际上运行的就是该类中的主方法。
// 3、主方法创建三个类加载器并初始化Catalina类，然后调用Catalina的process方法。
public final class Bootstrap {


    // ------------------------------------------------------- Static Variables


    /**
     * Debugging detail level for processing the startup.
     */
    private static int debug = 0;


    // ----------------------------------------------------------- Main Program


    /**
     * The main program for the bootstrap.
     *
     * @param args Command line arguments to be processed
     */
    public static void main(String args[]) {

        // Set the debug flag appropriately
        for (int i = 0; i < args.length; i++)  {
            if ("-debug".equals(args[i]))
                debug = 1;
        }
        
        // Configure catalina.base from catalina.home if not yet set
        if (System.getProperty("catalina.base") == null)
            System.setProperty("catalina.base", getCatalinaHome());

        // Construct the class loaders we will need
//        1、Bootstrap类的主方法还构造了三个加载器用于不同的目的。
//        2、使用不同加载器的主要原因是防止WEB-INF/classes以及WEB-INF/lib下面的类。
//        %CATALINE_HOME%/common/lib目录下的jar包也可以访问。
        ClassLoader commonLoader = null;
//        catalinaLoader负责加载Catalina容器要求的类，
//        可以加载%CATALINA_HOME%/server/classes和%CATALINA_HOME%/server/lib目录下面的类
        ClassLoader catalinaLoader = null;
//        1、sharedLoader可以访问%CATALINA_HOME%/shared/classes和%CATALJNA_HOME%/shared/lib目录下的类
//        2、以及commondLoader类可以访问的类。
//        3、sharedLoader是该Tomcat容器相关联的所有web应用的类加载器的父类加载器。
//        4、注意一点是sharedLoader加载器不能加载Catalina的内部类加载器以及环境变量下面的CLASSPATH下面的类
        ClassLoader sharedLoader = null;
        try {

            File unpacked[] = new File[1];
            File packed[] = new File[1];
            File packed2[] = new File[2];
            ClassLoaderFactory.setDebug(debug);

            unpacked[0] = new File(getCatalinaHome(),
                                   "common" + File.separator + "classes");
            packed2[0] = new File(getCatalinaHome(),
                                  "common" + File.separator + "endorsed");
            packed2[1] = new File(getCatalinaHome(),
                                  "common" + File.separator + "lib");
            commonLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed2, null);

            unpacked[0] = new File(getCatalinaHome(),
                                   "server" + File.separator + "classes");
            packed[0] = new File(getCatalinaHome(),
                                 "server" + File.separator + "lib");
            catalinaLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);

            unpacked[0] = new File(getCatalinaBase(),
                                   "shared" + File.separator + "classes");
            packed[0] = new File(getCatalinaBase(),
                                 "shared" + File.separator + "lib");
            sharedLoader =
                ClassLoaderFactory.createClassLoader(unpacked, packed,
                                                     commonLoader);
        } catch (Throwable t) {

            log("Class loader creation threw exception", t);
            System.exit(1);

        }

        Thread.currentThread().setContextClassLoader(catalinaLoader);

        // Load our startup class and call its process() method
        try {

            SecurityClassLoad.securityClassLoad(catalinaLoader);

            // Instantiate a startup class instance
            if (debug >= 1)
                log("Loading startup class");
//            创建完三个类加载器后，主方法加载了Catalina类然后创建它的实例并将其赋值给startupInstance变量
            Class startupClass =
                catalinaLoader.loadClass
                ("org.apache.catalina.stasrtup.Catalina");
            Object startupInstance = startupClas.newInstance();

//            然后调用setParentClassLoader方法，将sharedLoader作为参数：
            // Set the shared extensions class loader
            if (debug >= 1)
                log("Setting startup class properties");
            String methodName = "setParentClassLoader";
            Class paramTypes[] = new Class[1];
            paramTypes[0] = Class.forName("java.lang.ClassLoader");
            Object paramValues[] = new Object[1];
            paramValues[0] = sharedLoader;
            Method method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
            method.invoke(startupInstance, paramValues);

            // Call the process() method
            if (debug >= 1)
                log("Calling startup class process() method");
            methodName = "process";
            paramTypes = new Class[1];
            paramTypes[0] = args.getClass();
            paramValues = new Object[1];
            paramValues[0] = args;
            method =
                startupInstance.getClass().getMethod(methodName, paramTypes);
            method.invoke(startupInstance, paramValues);

        } catch (Exception e) {
            System.out.println("Exception during startup processing");
            e.printStackTrace(System.out);
            System.exit(2);
        }

    }


    /**
     * Get the value of the catalina.home environment variable.
     */
//    如果在前面没有提供catalina.home的值，它会使用user.dir的值。
    private static String getCatalinaHome() {
        return System.getProperty("catalina.home",
                                  System.getProperty("user.dir"));
    }


    /**
     * Get the value of the catalina.base environment variable.
     */
//    返回catalina.base的值，如果该值不存在返回catalina.home的值。
    private static String getCatalinaBase() {
        return System.getProperty("catalina.base", getCatalinaHome());
    }


    /**
     * Log a debugging detail message.
     *
     * @param message The message to be logged
     */
    private static void log(String message) {

        System.out.print("Bootstrap: ");
        System.out.println(message);

    }


    /**
     * Log a debugging detail message with an exception.
     *
     * @param message The message to be logged
     * @param exception The exception to be logged
     */
    private static void log(String message, Throwable exception) {

        log(message);
        exception.printStackTrace(System.out);

    }


}
