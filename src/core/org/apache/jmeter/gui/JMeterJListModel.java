package org.apache.jmeter.gui;

import java.util.*;
import javax.swing.*;


/**
 * Title:        Jakarta-JMeter
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Apache
 * @author Michael Stover
 * @version 1.0
 */

public class JMeterJListModel extends AbstractListModel
{
	List data;

	public JMeterJListModel()
	{
		data = new ArrayList();
	}

	public JMeterJListModel(List data)
	{
		this.data = new ArrayList(data);
	}

	public void setData(List data)
	{
		this.data.clear();
		this.data.addAll(data);
		this.fireContentsChanged(this,0,data.size()-1);
	}

	public List getData()
	{
		return data;
	}


	public int getSize() {
		if(data.size() == 0)
		{
			return 1;
		}
		return data.size();
	}
	public Object getElementAt(int parm1) {
		if(parm1 == 0 && data.size() == 0)
		{
			return "     ";
		}
		return data.get(parm1);
	}

	public void addItem(Object item)
	{
		data.add(item);
		this.fireContentsChanged(this,data.size()-1,data.size()-1);
	}

	public void clear()
	{
		data.clear();
		this.fireContentsChanged(this,0,0);
	}
}