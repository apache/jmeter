package org.apache.jorphan.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * @author mstover
 */
public class Functor
{
   private static Logger log = LoggingManager.getLoggerForClass();
   Object invokee;
   String methodName;
   Object[] args;
   Class[] types;
   Method methodToInvoke;

   /**
    * Create a functor with the invokee and a method name.
    * @param invokee
    * @param methodName
    */
   public Functor(Object invokee, String methodName)
   {
      this(methodName);
      this.invokee = invokee;
   }

   /**
    * Create a functor with the invokee, method name, and argument class types.
    * @param invokee
    * @param methodName
    * @param types
    */
   public Functor(Object invokee, String methodName, Class[] types)
   {
      this(invokee, methodName);
      this.types = types;
   }

   /**
    * Create a functor with just the method name.
    * @param methodName
    */
   public Functor(String methodName)
   {
      this.methodName = methodName;
   }

   /**
    * Create a functor with the method name and argument class types.
    * @param methodName
    * @param types
    */
   public Functor(String methodName, Class[] types)
   {
      this(methodName);
      this.types = types;
   }

   /**
    * Create a functor with an invokee, method name, and argument values.
    * @param invokee
    * @param methodName
    * @param args
    */
   public Functor(Object invokee, String methodName, Object[] args)
   {
      this(invokee, methodName);
      this.args = args;
   }
   
   public Functor(String methodName,Object[] args)
   {
      this(methodName);
      this.args = args;
   }

   /**
    * Create a functor with an invokee, method name, argument values, and argument
    * class types.
    * @param invokee
    * @param methodName
    * @param args
    * @param types
    */
   public Functor(Object invokee, String methodName, Object[] args,
         Class[] types)
   {
      this(invokee, methodName, args);
      this.types = types;
   }

   public Object invoke()
   {
      try
      {
         return createMethod(getTypes()).invoke(invokee, getArgs());
      }
      catch (Exception e)
      {
         log.warn("Trouble functing method: ",e);
         throw new org.apache.jorphan.util.JMeterError(e); //JDK1.4
      }
   }

   public Object invoke(Object invokee)
   {
      this.invokee = invokee;
      return invoke();
   }

   public Object invoke(Object[] args)
   {
      this.args = args;
      return invoke();
   }

   public Object invoke(Object invokee, Object[] args)
   {
      this.args = args;
      this.invokee = invokee;
      return invoke();
   }

   private Method createMethod(Class[] types)
   {
      log.debug("Trying to functorize invokee: " + invokee.getClass().getName() + " method: " + methodName + " types: " + Arrays.asList(types));
      if (methodToInvoke == null)
      {
         try
         {
            methodToInvoke = invokee.getClass().getMethod(methodName, types);
         }
         catch (Exception e)
         {
            for (int i = 0; i < types.length; i++)
            {
               Class primitive = getPrimitive(types[i]);
               if(primitive != null)
               {
                  methodToInvoke = createMethod(getNewArray(i,primitive,types));
                  if(methodToInvoke != null) return methodToInvoke;
               }
               Class[] interfaces = types[i].getInterfaces();
               for (int j = 0; j < interfaces.length; j++)
               {
                  methodToInvoke = createMethod(getNewArray(i, interfaces[j], types));
                  if (methodToInvoke != null) { return methodToInvoke; }
               }
               Class parent = types[i].getSuperclass();
               methodToInvoke = createMethod(getNewArray(i, parent, types));
               if (methodToInvoke != null) { return methodToInvoke; }
            }
         }
      }
      return methodToInvoke;
   }
   
   protected Class getPrimitive(Class t)
   {
      if(t.equals(Integer.class))
      {
         return int.class;
      }
      else if(t.equals(Long.class))
      {
         return long.class;
      }
      else if(t.equals(Double.class))
      {
         return double.class;
      }
      else if(t.equals(Float.class))
      {
         return float.class;
      }
      else if(t.equals(Byte.class))
      {
         return byte.class;
      }
      else if(t.equals(Boolean.class))
      {
         return boolean.class;
      }
      else if(t.equals(Short.class))
      {
         return short.class;
      }
      else if(t.equals(Character.class))
      {
         return char.class;
      }
      return null;
   }

   protected Class[] getNewArray(int i, Class replacement, Class[] orig)
   {
      Class[] newArray = new Class[orig.length];
      for (int j = 0; j < newArray.length; j++)
      {
         newArray[j] = orig[j];
         if (j == i)
         {
            newArray[j] = replacement;
         }
      }
      return newArray;
   }

   private Class[] getTypes()
   {
      if (types == null) // do only once per functor instance. Could
      // cause errors if functor used for multiple
      // same-named-different-parametered methods.
      {
         if (args != null)
         {
            types = new Class[args.length];
            for (int i = 0; i < args.length; i++)
            {
               types[i] = args[i].getClass();
            }
         }
         else
         {
            types = new Class[0];
         }
      }
      return types;
   }

   private Object[] getArgs()
   {
      if (args == null)
      {
         args = new Object[0];
      }
      return args;
   }
}
