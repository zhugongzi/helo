package springBootJolokia;

import org.apache.naming.ResourceRef;
import org.apache.naming.factory.Constants;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class PigBeanFactory implements ObjectFactory {

    /**
     * Create a new Bean instance.
     *
     * @param obj The reference object describing the Bean
     */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
            throws NamingException, IntrospectionException,
            NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {

        if (obj instanceof ResourceRef) {

            try {

                Reference ref = (Reference) obj;
                String beanClassName = ref.getClassName();
                Class beanClass = null;
                ClassLoader tcl =
                        Thread.currentThread().getContextClassLoader();
                if (tcl != null) {
                    try {
                        beanClass = tcl.loadClass(beanClassName);
                    } catch(ClassNotFoundException e) {
                    }
                } else {
                    try {
                        beanClass = Class.forName(beanClassName);
                    } catch(ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                //...

                BeanInfo bi = Introspector.getBeanInfo(beanClass);
                PropertyDescriptor[] pda = bi.getPropertyDescriptors();

                Object bean = beanClass.getConstructor().newInstance();

                /* Look for properties with explicitly configured setter */
                RefAddr ra = ref.get("forceString");
                Map forced = new HashMap<>();
                String value;

                if (ra != null) {
                    value = (String)ra.getContent();
                    Class paramTypes[] = new Class[1];
                    paramTypes[0] = String.class;
                    String setterName;
                    int index;

                    /* Items are given as comma separated list */
                    for (String param: value.split(",")) {
                        param = param.trim();
                        /* A single item can either be of the form name=method
                         * or just a property name (and we will use a standard
                         * setter) */
                        index = param.indexOf('=');
                        if (index >= 0) {
                            setterName = param.substring(index + 1).trim();
                            param = param.substring(0, index).trim();
                        } else {
                            setterName = "set" +
                                    param.substring(0, 1).toUpperCase(Locale.ENGLISH) +
                                    param.substring(1);
                        }
                        try {
                            forced.put(param,
                                    beanClass.getMethod(setterName, paramTypes));
                        } catch (NoSuchMethodException|SecurityException ex) {
                            throw new NamingException
                                    ("Forced String setter " + setterName +
                                            " not found for property " + param);
                        }
                    }
                }

                Enumeration e = ref.getAll();

                while (e.hasMoreElements()) {

                    ra = (RefAddr) e.nextElement();
                    String propName = ra.getType();

                    if (propName.equals(Constants.FACTORY) ||
                            propName.equals("scope") || propName.equals("auth") ||
                            propName.equals("forceString") ||
                            propName.equals("singleton")) {
                        continue;
                    }

                    value = (String)ra.getContent();

                    Object[] valueArray = new Object[1];

                    /* Shortcut for properties with explicitly configured setter */
                    Method method = (Method) forced.get(propName);
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    if (method != null) {
                        valueArray[0] = value;
                        try {
                            method.invoke(bean, valueArray);
                        } catch (IllegalAccessException|
                                IllegalArgumentException|
                                InvocationTargetException ex) {
                            throw new NamingException
                                    ("Forced String setter " + method.getName() +
                                            " threw exception for property " + propName);
                        }
                        continue;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return null;
    }
}

