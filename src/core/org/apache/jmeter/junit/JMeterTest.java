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
import org.apache.jmeter.gui.UnsharedComponent;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.ClassFinder;
import org.apache.log.Logger;

/**
 * @author    Michael Stover
 * @version   $Revision$
 */
public class JMeterTest extends TestCase
{
    private static Logger log = LoggingManager.getLoggerFor(JMeterUtils.TEST);

    public JMeterTest(String name)
    {
        super(name);
    }

    public void testGUIComponents() throws Exception
    {
        Iterator iter = getObjects(JMeterGUIComponent.class).iterator();
        while (iter.hasNext())
        {
            JMeterGUIComponent item = (JMeterGUIComponent) iter.next();
            if (item instanceof JMeterTreeNode)
            {
                continue;
            }
            assertEquals(
                "Failed on " + item.getClass().getName(),
                item.getStaticLabel(),
                item.getName());
            TestElement el = item.createTestElement();
            assertEquals(
                "GUI-CLASS: Failed on " + item.getClass().getName(),
                item.getClass().getName(),
                el.getPropertyAsString(TestElement.GUI_CLASS));
            assertEquals(
                "NAME: Failed on " + item.getClass().getName(),
                item.getName(),
                el.getPropertyAsString(TestElement.NAME));
            assertEquals(
                "TEST-CLASS: Failed on " + item.getClass().getName(),
                el.getClass().getName(),
                el.getPropertyAsString(TestElement.TEST_CLASS));
            TestElement el2 = item.createTestElement();
            el.setProperty(TestElement.NAME, "hey, new name!:");
            el.setProperty("NOT", "Shouldn't be here");
            if (!(item instanceof UnsharedComponent))
            {
                assertEquals(
                    "GUI-CLASS: Failed on " + item.getClass().getName(),
                    "",
                    el2.getPropertyAsString("NOT"));
            }
            log.debug("Saving element: " + el.getClass());
            el =
                SaveService.createTestElement(
                    SaveService.getConfigForTestElement(null, el));
            log.debug("Successfully saved");
            item.configure(el);
            assertEquals(
                "CONFIGURE-TEST: Failed on " + item.getClass().getName(),
                el.getPropertyAsString(TestElement.NAME),
                item.getName());
            item.modifyTestElement(el2);
            assertEquals(
                "Modify Test: Failed on " + item.getClass().getName(),
                "hey, new name!:",
                el2.getPropertyAsString(TestElement.NAME));
        }
    }

    public void testSerializableElements() throws Exception
    {
        Iterator iter = getObjects(Serializable.class).iterator();
        while (iter.hasNext())
        {
            Serializable serObj = (Serializable) iter.next();
            if (serObj.getClass().getName().endsWith("_Stub"))
            {
                continue;
            }
            try
            {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bytes);
                out.writeObject(serObj);
                out.close();
                ObjectInputStream in =
                    new ObjectInputStream(
                        new ByteArrayInputStream(bytes.toByteArray()));
                Object readObject = in.readObject();
                in.close();
                assertEquals(
                    "deserializing class: " + serObj.getClass().getName(),
                    serObj.getClass(),
                    readObject.getClass());
            }
            catch (Exception e)
            {
                log.error(
                    "Trying to serialize object: "
                        + serObj.getClass().getName(),
                    e);
                throw e;
            }
        }
    }

    public void testTestElements() throws Exception
    {
        Iterator iter = getObjects(TestElement.class).iterator();
        while (iter.hasNext())
        {
            TestElement item = (TestElement) iter.next();
            checkElementCloning(item);
            assertTrue(
                item.getClass().getName() + " must implement Serializable",
                item instanceof Serializable);
        }
    }

    protected Collection getObjects(Class extendsClass) throws Exception
    {
        Iterator classes =
            ClassFinder
                .findClassesThatExtend(
                    JMeterUtils.getSearchPaths(),
                    new Class[] { extendsClass })
                .iterator();
        List objects = new LinkedList();
        while (classes.hasNext())
        {
            Class c = Class.forName((String) classes.next());
            try
            {
                try
                {
                    // Try with a parameter-less constructor first
                    objects.add(c.newInstance());
                }
                catch (InstantiationException e)
                {
                    try
                    {
                        // Events often have this constructor
                        objects.add(
                            c.getConstructor(
                                new Class[] { Object.class }).newInstance(
                                new Object[] { this }));
                    }
                    catch (NoSuchMethodException f)
                    {
                        // no luck. Ignore this class
                    }
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
        assertTrue(item != clonedItem);
        assertEquals(
            "CLONE-SAME-CLASS: testing " + item.getClass().getName(),
            item.getClass().getName(),
            clonedItem.getClass().getName());
    }

    private void checkElementCloning(TestElement item)
    {
        TestElement clonedItem = (TestElement) item.clone();
        cloneTesting(item, clonedItem);
        PropertyIterator iter2 = item.propertyIterator();
        while (iter2.hasNext())
        {
            JMeterProperty item2 = iter2.next();
            assertEquals(item2, clonedItem.getProperty(item2.getName()));
            assertTrue(item2 != clonedItem.getProperty(item2.getName()));
        }
    }
}
