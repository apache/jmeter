package org.apache.jorphan.reflect;

import java.lang.reflect.Method;

/**
 * @author mstover
 */
public class Functor
{
   Object invokee;
   String methodName;
   Object[] args;
   Class[] types;
   Method methodToInvoke;

   public Functor(Object invokee, String methodName)
   {
      this(methodName);
      this.invokee = invokee;
   }

   public Functor(Object invokee, String methodName, Class[] types)
   {
      this(invokee, methodName);
      this.types = types;
   }

   public Functor(String methodName)
   {
      this.methodName = methodName;
   }

   public Functor(String methodName, Class[] types)
   {
      this(methodName);
      this.types = types;
   }

   public Functor(Object invokee, String methodName, Object[] args)
   {
      this(invokee, methodName);
      this.args = args;
   }

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
         return createMethod(types).invoke(invokee, getArgs());
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
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
