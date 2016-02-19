package ru.semiot.configprototype;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 *
 * @author Daniil Garayzuev <garayzuev@gmail.com>
 */
public class Confi implements ManagedService{

    
    @Override
    public void updated(Dictionary props) throws ConfigurationException {
        System.out.println("Hello from Confi.updated!");
        if(props == null ){
            System.out.println("Confi.updated1: props is null");
            //props = loadExistingProps();
            props = new Hashtable();
            props.put("qwerty", "123");
        }
        else{
            System.out.println("Confi.updated2: props is " + props.toString());
        }
    }
  
    private Dictionary loadExistingProps(){
        Dictionary nProps = new Hashtable();
        return nProps;
    }
    
}
