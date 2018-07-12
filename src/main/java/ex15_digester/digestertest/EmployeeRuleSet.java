package ex15_digester.digestertest;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;

//当Digester实例遇到一个XML元素的开始标志的使用，调用所有匹配规则的begin方法

//当Digester实例遇到XML元素的end时候，调用所有匹配规则的end方法

//RuleSet表示了Rule对象，该接口定义了两个方法：addRuleInstance和getNamespaceURI

//基本类RuleSetBase实现了RuleSet接口，RuleSetBase是一个抽象类，
//        它提供了getNamespaceURI的实现，你需要做的只是提供addRuleInstances的实现即可。
public class EmployeeRuleSet extends RuleSetBase  {

//  下面规则同test02中的规则
  public void addRuleInstances(Digester digester) {
    // add rules
//创建对象 ----------attributeName参数定义了XML文档中的属性，名字有className指定
    digester.addObjectCreate("employee", "ex15.pyrmont.digestertest.Employee");

//设置属性  ------Employee使用第二条规则来根据XML文档调用setFirstName和setLasetName属性来设置Employee对象的值。
    digester.addSetProperties("employee");

    digester.addObjectCreate("employee/office", "ex15.pyrmont.digestertest.Office");
    digester.addSetProperties("employee/office");

//    addSetNext用于建立第一个对象和第二个对象之间的关系
//    该模式的形式如firstObject/secondObject。
    digester.addSetNext("employee/office", "addOffice");

    digester.addObjectCreate("employee/office/address", "ex15.pyrmont.digestertest.Address");
    digester.addSetProperties("employee/office/address");
    digester.addSetNext("employee/office/address", "setAddress"); 
  }
}
