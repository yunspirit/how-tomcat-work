package ex15_digester;

/**
 * @author yunqian.yq
 * @date 2018/7/12 10:37
 */


//为什么要使用digest-------server.xml
//所有的东西都是硬编码的。要更改一个组件或者一个属性的值都需要重新编译整个Bootstrap类。
//        幸运的是，Tomcat的设计者选择了一种更优雅的方式来进行配置，使用名为server.xml的XML文档。
//        Server.xml中的每一个元素都被转换为一个Java对象，元素的属性用来设置属性。
//        这样，就可以通过编辑server.xml来改变Tomcat的配置
//    -------------------------
//            例如，上下文容器元素就可以这样在server.xml中表示
//<context/> To set the path and docBase properties you use attributes in the XML element:
//<context docBase="myApp" path="/myApp"/>

//    Tomcat使用开源工具Digester来讲XML元素转换为Java对象
public class digester {
}
