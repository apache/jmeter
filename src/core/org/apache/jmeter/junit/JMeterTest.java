package org.apache.jmeter.junit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.jmeter.gui.JMeterGUIComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.log.Logger;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;

/****************************************
 * Title: JMeter Description: Copyright: Copyright (c) 2000 Company: Apache
 *
 *@author    Michael Stover
 *@created   $Date$
 *@version   1.0
 ***************************************/

public class JMeterTest extends TestCase
{
	private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.TEST);

	/****************************************
	 * !ToDo (Constructor description)
	 *
	 *@param name  !ToDo (Parameter description)
	 ***************************************/
	public JMeterTest(String name)
	{
		super(name);
	}

	/****************************************
	 * !ToDo
	 *
	 *@exception Exception  !ToDo (Exception description)
	 ***************************************/
	public void testGUIComponents() throws Exception
	{
		Iterator iter = getObjects(JMeterGUIComponent.class).iterator();
		while(iter.hasNext())
		{
			JMeterGUIComponent item = (JMeterGUIComponent)iter.next();
			if(item instanceof JMeterTreeNode)
			{
				continue;
			}
			this.assertEquals("Failed on " + item.getClass().getName(), 
					item.getStaticLabel(), item.getName());
			TestElement el = item.createTestElement();
			assertEquals("GUI-CLASS: Failed on " + item.getClass().getName(), item.getClass().getName(),
					el.getProperty(TestElement.GUI_CLASS));
			assertEquals("NAME: Failed on " + item.getClass().getName(), item.getName(),
					el.getProperty(TestElement.NAME));
			assertEquals("TEST-CLASS: Failed on " + item.getClass().getName(),
					el.getClass().getName(), el.getProperty(TestElement.TEST_CLASS));
			el.setProperty(TestElement.NAME, "hey, new name!:");
			el.setProperty("NOT","Shouldn't be here");
			TestElement el2 = item.createTestElement();
			assertNull("GUI-CLASS: Failed on " + item.getClass().getName(),
			el2.getProperty("NOT"));
			el = SaveService.createTestElement(SaveService.getConfigForTestElement(null,
					el));
			item.configure(el);
			assertEquals("CONFIGURE-TEST: Failed on " + item.getClass().getName(),
					el.getProperty(TestElement.NAME), item.getName());
		}
	}
	
	public void testSerializableElements() throws Exception
	{
		Iterator iter = getObjects(Serializable.class).iterator();
		while(iter.hasNext())
		{
			Serializable serObj = (Serializable)iter.next();
			if(serObj.getClass().getName().endsWith("_Stub"))
			{
				continue;
			}
			try
			{
				log.debug("serializing class: "+serObj.getClass().getName());
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bytes);
				out.writeObject(serObj);
				out.close();
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()));
				Object readObject = in.readObject();
				in.close();
				assertEquals("deserializing class: "+serObj.getClass().getName(),
						serObj.getClass(),readObject.getClass());
			}
			catch (Exception e)
			{
				log.error("Trying to serialize object: "+serObj.getClass().getName(),
						e);
				throw e;
			}
		}
	}

	/****************************************
	 * !ToDo
	 *
	 *@exception Exception  !ToDo (Exception description)
	 ***************************************/
	public void testTestElements() throws Exception
	{
		Iterator iter = getObjects(TestElement.class).iterator();
		while(iter.hasNext())
		{
			TestElement item = (TestElement)iter.next();
			checkElementCloning(item);
			assertTrue(item.getClass().getName()+" must implement Serializable",
					item instanceof Serializable);
		}
	}


	/****************************************
	 * !ToDoo (Method description)
	 *
	 *@param extendsClass   !ToDo (Parameter description)
	 *@return               !ToDo (Return description)
	 *@exception Exception  !ToDo (Exception description)
	 ***************************************/
	protected Collection getObjects(Class extendsClass) throws Exception
	{
		Iterator classes = ClassFinder.findClassesThatExtend(
				JMeterUtils.getSearchPaths(),
				new Class[]{extendsClass}).iterator();
		List objects = new LinkedList();
		while(classes.hasNext())
		{
		    Class c= Class.forName((String)classes.next());
		    try
		    {
			try
			{
			    objects.add(c.newInstance());
			}
			catch (InstantiationException e)
			{
			    objects.add(c.getConstructor(
				  new Class[] {Object.class}).newInstance(
				      new Object[] {this} ));
			}
		    }
		    catch (IllegalAccessException e)
		    {
		      // We won't test serialization of restricted-access
		      // classes.
		    }
		}
		return objects;
	}

	private void cloneTesting(TestElement item, TestElement clonedItem)
	{
		this.assertTrue(item != clonedItem);
		this.assertEquals("CLONE-SAME-CLASS: testing " + item.getClass().getName(),
				item.getClass().getName(), clonedItem.getClass().getName());
	}

	private void checkElementCloning(TestElement item)
	{
		TestElement clonedItem = (TestElement)item.clone();
		cloneTesting(item, clonedItem);
		Iterator iter2 = item.getPropertyNames().iterator();
		while(iter2.hasNext())
		{
			Object item2 = iter2.next();
			if(item2 instanceof TestElement)
			{
				checkElementCloning((TestElement)item2);
			}
		}
	}

}
