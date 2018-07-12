package ex15_digester.digestertest;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.digester.Digester;

public class Test02 {

//  Digester类的第二个例子说明了如何创建两个对象，并建立它们之间的关系。关系的定义需要事先定义好。
  public static void main(String[] args) {
    String path = System.getProperty("user.dir") + File.separator  + "etc";
    File file = new File(path, "employee2.xml");
    Digester digester = new Digester();
    // add rules  建立employee和office的关系
    digester.addObjectCreate("employee", "ex15.pyrmont.digestertest.Employee");
    digester.addSetProperties("employee");

    digester.addObjectCreate("employee/office", "ex15.pyrmont.digestertest.Office");
    digester.addSetProperties("employee/office");

//    用addOffice方法建立联系
    digester.addSetNext("employee/office", "addOffice");

    digester.addObjectCreate("employee/office/address", 
      "ex15.pyrmont.digestertest.Address");
    digester.addSetProperties("employee/office/address");
//    要将一个address赋值给office，可以使用Office类的setAddress方法
    digester.addSetNext("employee/office/address", "setAddress"); 
    try {
      Employee employee = (Employee) digester.parse(file);
      ArrayList offices = employee.getOffices();
      Iterator iterator = offices.iterator();
      System.out.println("-------------------------------------------------");
      while (iterator.hasNext()) {
        Office office = (Office) iterator.next();
        Address address = office.getAddress();
        System.out.println(office.getDescription());
        System.out.println("Address : " + 
          address.getStreetNumber() + " " + address.getStreetName());
        System.out.println("--------------------------------");
      }
      
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }  
}
