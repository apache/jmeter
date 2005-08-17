/*
 * Created on Aug 16, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.jmeter.gui.tree;

/**
 * @author pete
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface NamedTreeNode {

    void setName(String name);
    String getName();
    void nameChanged();
}
