package org.apache.jmeter.protocol.http.gui;

import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.protocol.http.util.HTTPArgument;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ObjectTableModel;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class HTTPArgumentsPanel extends ArgumentsPanel {
	
	private static final String ENCODED_VALUE = JMeterUtils.getResString("encoded_value");
	private static final String ENCODE_OR_NOT = JMeterUtils.getResString("encode?");
    private static final String INCLUDE_EQUALS = JMeterUtils.getResString("include_equals");
	
	
	protected void initializeTableModel() {
		tableModel = new ObjectTableModel(new String[]{Arguments.COLUMN_NAMES[0],Arguments.COLUMN_NAMES[1],
				ENCODE_OR_NOT,INCLUDE_EQUALS},
				new String[]{"name","value","alwaysEncoded","useEquals"},
                new Class[]{String.class,Object.class,boolean.class,boolean.class},
                new Class[]{String.class,String.class,Boolean.class,Boolean.class},
                new HTTPArgument());
	}
    
    protected void sizeColumns(JTable table)
    {
        int resizeMode = table.getAutoResizeMode();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        fixSize(table.getColumn(INCLUDE_EQUALS));   
        fixSize(table.getColumn(ENCODE_OR_NOT));
        table.setAutoResizeMode(resizeMode);  
    }
    
    protected Object makeNewArgument()
    {
        HTTPArgument arg = new HTTPArgument("","");
        arg.setAlwaysEncoded(false);
        arg.setUseEquals(true);
        return arg;
    }

    private void fixSize(TableColumn column)
    {
        column.sizeWidthToFit();
        //column.setMinWidth(column.getWidth());
        column.setMaxWidth((int)(column.getWidth() * 1.5));
        column.setWidth(column.getMaxWidth());
        column.setResizable(false);
    }
	
	public HTTPArgumentsPanel()
	{
		super(JMeterUtils.getResString("paramtable"));
	}		
	
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@return   !ToDo (Return description)
	 ***************************************/
	public TestElement createTestElement()
	{
		Iterator modelData = tableModel.iterator();
		Arguments args = new Arguments();
		while(modelData.hasNext())
		{
            HTTPArgument arg = (HTTPArgument)modelData.next();
            args.addArgument(arg);
		}
		this.configureTestElement(args);
		return (TestElement)args.clone();
	}
	
	/****************************************
	 * !ToDo (Method description)
	 *
	 *@param el  !ToDo (Parameter description)
	 ***************************************/
	public void configure(TestElement el)
	{
		super.configure(el);
		if(el instanceof Arguments)
		{
			tableModel.clearData();
			HTTPArgument.convertArgumentsToHTTP((Arguments)el);
			Iterator iter = ((Arguments)el).getArguments().iterator();
			while(iter.hasNext())
			{
				HTTPArgument arg = (HTTPArgument)iter.next();
				tableModel.addRow(arg);
			}
		}
		checkDeleteStatus();
	}
    
    protected boolean isMetaDataNormal(HTTPArgument arg)
    {
        return arg.getMetaData() == null || arg.getMetaData().equals("=") || (arg.getValue() != null && arg.getValue().toString().length() > 0);
    }
}
