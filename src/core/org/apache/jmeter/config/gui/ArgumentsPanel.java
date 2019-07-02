/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.jmeter.config.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang3.StringUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.gui.util.HeaderAsPropertyRenderer;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.reflect.Functor;

/**
 * A GUI panel allowing the user to enter name-value argument pairs. These
 * arguments (or parameters) are usually used to provide configuration values
 * for some other component.
 *
 */
public class ArgumentsPanel extends AbstractConfigGui implements ActionListener {

    private static final long serialVersionUID = 240L;

    /** The title label for this component. */
    private JLabel tableLabel;

    /** The table containing the list of arguments. */
    private transient JTable table;

    /** The model for the arguments table. */
    protected transient ObjectTableModel tableModel; // will only contain Argument or HTTPArgument

    /** A button for adding new arguments to the table. */
    private JButton add;

    /** A button for removing arguments from the table. */
    private JButton delete;

    /**
     * Added background support for reporting tool
     */
    private Color background;

    /**
     * Boolean indicating whether this component is a standalone component or it
     * is intended to be used as a subpanel for another component.
     */
    private final boolean standalone;

    /** Button to move an argument up*/
    private JButton up;

    /** Button to move an argument down*/
    private JButton down;

    /** Button to show the detail of an argument*/
    private JButton showDetail;

    /** Enable Up and Down buttons */
    private final boolean enableUpDown;

    /** Disable buttons :Detail, Add, Add from Clipboard, Delete, Up and Down*/
    private final boolean disableButtons;

    /** Command for adding a row to the table. */
    private static final String ADD = "add"; // $NON-NLS-1$

    /** Command for adding rows from the clipboard */
    private static final String ADD_FROM_CLIPBOARD = "addFromClipboard"; // $NON-NLS-1$

    /** Command for removing a row from the table. */
    private static final String DELETE = "delete"; // $NON-NLS-1$

    /** Command for moving a row up in the table. */
    private static final String UP = "up"; // $NON-NLS-1$

    /** Command for moving a row down in the table. */
    private static final String DOWN = "down"; // $NON-NLS-1$

    /** When pasting from the clipboard, split lines on linebreak */
    private static final String CLIPBOARD_LINE_DELIMITERS = "\n"; //$NON-NLS-1$

    /** When pasting from the clipboard, split parameters on tab */
    private static final String CLIPBOARD_ARG_DELIMITERS = "\t"; //$NON-NLS-1$

    /** Command for showing detail. */
    private static final String DETAIL = "detail"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_0 = "name"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_1 = "value"; // $NON-NLS-1$

    public static final String COLUMN_RESOURCE_NAMES_2 = "description"; // $NON-NLS-1$

    /**
     * Create a new ArgumentsPanel as a standalone component.
     */
    public ArgumentsPanel() {
        this(JMeterUtils.getResString("user_defined_variables"),null, true, true);// $NON-NLS-1$
    }

    /**
     * Create a new ArgumentsPanel as an embedded component, using the specified
     * title.
     *
     * @param label
     *            the title for the component.
     */
    public ArgumentsPanel(String label) {
        this(label, null, true, false);
    }

    /**
     * Create a new ArgumentsPanel as an embedded component, using the specified
     * title.
     *
     * @param label
     *            the title for the component.
     * @param enableUpDown Add up/down buttons
     */
    public ArgumentsPanel(String label, boolean enableUpDown) {
        this(label, null, enableUpDown, false);
    }

    /**
     * Create a new ArgumentsPanel as an embedded component, using the specified
     * title.
     *
     * @param disableButtons Remove Edit all buttons
     * @param label the title for the component.
     */
    public ArgumentsPanel(boolean disableButtons, String label) {
        this(label, null, false, false, null, disableButtons);
    }

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     */
    public ArgumentsPanel(String label, Color bkg) {
        this(label, bkg, true, false);
    }

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     * @param enableUpDown Add up/down buttons
     * @param standalone is standalone
     */
    public ArgumentsPanel(String label, Color bkg, boolean enableUpDown, boolean standalone) {
        this(label, bkg, enableUpDown, standalone, null, false);
    }

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     * @param enableUpDown Add up/down buttons
     * @param standalone is standalone
     * @param model the table model to use
     */
    public ArgumentsPanel(String label, Color bkg, boolean enableUpDown, boolean standalone, ObjectTableModel model) {
        this(label, bkg, enableUpDown, standalone, model, false);
    }

    /**
     * Create a new ArgumentsPanel with a border and color background
     * @param label text for label
     * @param bkg background colour
     * @param enableUpDown Add up/down buttons
     * @param standalone is standalone
     * @param model the table model to use
     * @param disableButtons Remove all buttons
     */
    public ArgumentsPanel(String label, Color bkg, boolean enableUpDown, boolean standalone, ObjectTableModel model, boolean disableButtons) {
        tableLabel = new JLabel(label);
        this.enableUpDown = enableUpDown;
        this.disableButtons = disableButtons;
        this.background = bkg;
        this.standalone = standalone;
        this.tableModel = model;
        init();
    }

    /**
     * This is the list of menu categories this gui component will be available
     * under.
     *
     * @return a Collection of Strings, where each element is one of the
     *         constants defined in MenuFactory
     */
    @Override
    public Collection<String> getMenuCategories() {
        if (standalone) {
            return super.getMenuCategories();
        }
        return null;
    }

    @Override
    public String getLabelResource() {
        return "user_defined_variables"; // $NON-NLS-1$
    }

    /* Implements JMeterGUIComponent.createTestElement() */
    @Override
    public TestElement createTestElement() {
        Arguments args = new Arguments();
        modifyTestElement(args);
        return args;
    }

    /* Implements JMeterGUIComponent.modifyTestElement(TestElement) */
    @Override
    public void modifyTestElement(TestElement args) {
        GuiUtils.stopTableEditing(table);
        if (args instanceof Arguments) {
            Arguments arguments = (Arguments) args;
            arguments.clear();
            @SuppressWarnings("unchecked") // only contains Argument (or HTTPArgument)
            Iterator<Argument> modelData = (Iterator<Argument>) tableModel.iterator();
            while (modelData.hasNext()) {
                Argument arg = modelData.next();
                if(StringUtils.isEmpty(arg.getName()) && StringUtils.isEmpty(arg.getValue())) {
                    continue;
                }
                arg.setMetaData("="); // $NON-NLS-1$
                arguments.addArgument(arg);
            }
        }
        super.configureTestElement(args);
    }

    /**
     * A newly created component can be initialized with the contents of a Test
     * Element object by calling this method. The component is responsible for
     * querying the Test Element object for the relevant information to display
     * in its GUI.
     *
     * @param el the TestElement to configure
     */
    @Override
    public void configure(TestElement el) {
        super.configure(el);
        if (el instanceof Arguments) {
            tableModel.clearData();
            for (JMeterProperty jMeterProperty : (Arguments) el) {
                Argument arg = (Argument) jMeterProperty.getObjectValue();
                tableModel.addRow(arg);
            }
        }
        checkButtonsStatus();
    }

    /**
     * Get the table used to enter arguments.
     *
     * @return the table used to enter arguments
     */
    protected JTable getTable() {
        return table;
    }

    /**
     * Get the title label for this component.
     *
     * @return the title label displayed with the table
     */
    protected JLabel getTableLabel() {
        return tableLabel;
    }

    /**
     * Get the button used to delete rows from the table.
     *
     * @return the button used to delete rows from the table
     */
    protected JButton getDeleteButton() {
        return delete;
    }

    /**
     * Get the button used to add rows to the table.
     *
     * @return the button used to add rows to the table
     */
    protected JButton getAddButton() {
        return add;
    }

    protected void checkButtonsStatus() {
        if (!disableButtons) {
            // Disable DELETE if there are no rows in the table to delete.
            if (tableModel.getRowCount() == 0) {
                delete.setEnabled(false);
                showDetail.setEnabled(false);
            } else {
                delete.setEnabled(true);
                showDetail.setEnabled(true);
            }
            if(enableUpDown) {
                if(tableModel.getRowCount()>1) {
                    up.setEnabled(true);
                    down.setEnabled(true);
                }
                else {
                    up.setEnabled(false);
                    down.setEnabled(false);
                }
            }
        }

    }

    @Override
    public void clearGui(){
        super.clearGui();
        clear();
    }

    /**
     * Clear all rows from the table. T.Elanjchezhiyan(chezhiyan@siptech.co.in)
     */
    public void clear() {
        GuiUtils.stopTableEditing(table);
        tableModel.clearData();
    }

    /**
     * Invoked when an action occurs. This implementation supports the add and
     * delete buttons.
     *
     * @param e the event that has occurred
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!disableButtons) {
            String action = e.getActionCommand();
            if (action.equals(DELETE)) {
                deleteArgument();
            } else if (action.equals(ADD)) {
                addArgument();
            } else if (action.equals(ADD_FROM_CLIPBOARD)) {
                addFromClipboard();
            } else if (action.equals(UP)) {
                moveUp();
            } else if (action.equals(DOWN)) {
                moveDown();
            } else if (action.equals(DETAIL)) {
                showDetail();
            }
        }

    }


    /**
     * Move a row down
     */
    private void moveDown() {
        //get the selected rows before stopping editing
        // or the selected rows will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length > 0 && rowsSelected[rowsSelected.length - 1] < table.getRowCount() - 1) {
            table.clearSelection();
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                int rowSelected = rowsSelected[i];
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected + 1);
            }
            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected + 1, rowSelected + 1);
            }

            scrollToRowIfNotVisible(rowsSelected[0]+1);
        }
    }


    /**
     * ensure that a row is visible in the viewport
     * @param rowIndx row index
     */
    private void scrollToRowIfNotVisible(int rowIndx) {
        if(table.getParent() instanceof JViewport) {
            Rectangle visibleRect = table.getVisibleRect();
            final int cellIndex = 0;
            Rectangle cellRect = table.getCellRect(rowIndx, cellIndex, false);
            if (visibleRect.y > cellRect.y) {
                table.scrollRectToVisible(cellRect);
            } else {
                Rectangle rect2 = table.getCellRect(rowIndx + getNumberOfVisibleRows(table), cellIndex, true);
                int width = rect2.y - cellRect.y;
                table.scrollRectToVisible(new Rectangle(cellRect.x, cellRect.y, cellRect.width, cellRect.height + width));
            }
        }
    }

    /**
     * @param table {@link JTable}
     * @return number of visible rows
     */
    private static int getNumberOfVisibleRows(JTable table) {
        Rectangle vr = table.getVisibleRect();
        int first = table.rowAtPoint(vr.getLocation());
        vr.translate(0, vr.height);
        return table.rowAtPoint(vr.getLocation()) - first;
    }

    /**
     *  Move a row down
     */
    private void moveUp() {
        //get the selected rows before stopping editing
        // or the selected rows will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length > 0 && rowsSelected[0] > 0) {
            table.clearSelection();
            for (int rowSelected : rowsSelected) {
                tableModel.moveRow(rowSelected, rowSelected + 1, rowSelected - 1);
            }

            for (int rowSelected : rowsSelected) {
                table.addRowSelectionInterval(rowSelected - 1, rowSelected - 1);
            }

            scrollToRowIfNotVisible(rowsSelected[0]-1);
        }
    }

    /**
     * Show Row Detail
     */
    private void showDetail() {
        //get the selected rows before stopping editing
        // or the selected will be unselected
        int[] rowsSelected = table.getSelectedRows();
        GuiUtils.stopTableEditing(table);

        if (rowsSelected.length == 1) {
            table.clearSelection();
            RowDetailDialog detailDialog = new RowDetailDialog(tableModel, rowsSelected[0]);
            detailDialog.setVisible(true);
        }
    }

    /**
     * Remove the currently selected argument from the table.
     */
    protected void deleteArgument() {
        GuiUtils.cancelEditing(table);

        int[] rowsSelected = table.getSelectedRows();
        int anchorSelection = table.getSelectionModel().getAnchorSelectionIndex();
        table.clearSelection();
        if (rowsSelected.length > 0) {
            for (int i = rowsSelected.length - 1; i >= 0; i--) {
                tableModel.removeRow(rowsSelected[i]);
            }

            // Table still contains one or more rows, so highlight (select)
            // the appropriate one.
            if (tableModel.getRowCount() > 0) {
                if (anchorSelection >= tableModel.getRowCount()) {
                    anchorSelection = tableModel.getRowCount() - 1;
                }
                table.setRowSelectionInterval(anchorSelection, anchorSelection);
            }

            checkButtonsStatus();
        }
    }

    /**
     * Add a new argument row to the table.
     */
    protected void addArgument() {
        // If a table cell is being edited, we should accept the current value
        // and stop the editing before adding a new row.
        GuiUtils.stopTableEditing(table);

        tableModel.addRow(makeNewArgument());

        checkButtonsStatus();

        // Highlight (select) and scroll to the appropriate row.
        int rowToSelect = tableModel.getRowCount() - 1;
        table.setRowSelectionInterval(rowToSelect, rowToSelect);
        table.scrollRectToVisible(table.getCellRect(rowToSelect, 0, true));
    }

    /**
     * Add values from the clipboard
     * @param lineDelimiter Delimiter string to split clipboard into lines
     * @param argDelimiter Delimiter string to split line into key-value pair
     */
    protected void addFromClipboard(String lineDelimiter, String argDelimiter) {
        GuiUtils.stopTableEditing(table);
        int rowCount = table.getRowCount();
        try {
            String clipboardContent = GuiUtils.getPastedText();
            if(clipboardContent == null) {
                return;
            }
            String[] clipboardLines = clipboardContent.split(lineDelimiter);
            for (String clipboardLine : clipboardLines) {
                String[] clipboardCols = clipboardLine.split(argDelimiter);
                if (clipboardCols.length > 0) {
                    Argument argument = createArgumentFromClipboard(clipboardCols);
                    tableModel.addRow(argument);
                }
            }
            if (table.getRowCount() > rowCount) {
                checkButtonsStatus();

                // Highlight (select) and scroll to the appropriate rows.
                int rowToSelect = tableModel.getRowCount() - 1;
                table.setRowSelectionInterval(rowCount, rowToSelect);
                table.scrollRectToVisible(table.getCellRect(rowCount, 0, true));
            }
        } catch (IOException ioe) {
            JOptionPane.showMessageDialog(this,
                    "Could not add read arguments from clipboard:\n" + ioe.getLocalizedMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (UnsupportedFlavorException ufe) {
            JOptionPane.showMessageDialog(this,
                    "Could not add retrieve " + DataFlavor.stringFlavor.getHumanPresentableName()
                            + " from clipboard" + ufe.getLocalizedMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void addFromClipboard() {
        addFromClipboard(CLIPBOARD_LINE_DELIMITERS, CLIPBOARD_ARG_DELIMITERS);
    }

    protected Argument createArgumentFromClipboard(String[] clipboardCols) {
        Argument argument = makeNewArgument();
        argument.setName(clipboardCols[0]);
        if (clipboardCols.length > 1) {
            argument.setValue(clipboardCols[1]);
            if (clipboardCols.length > 2) {
                argument.setDescription(clipboardCols[2]);
            }
        }
        return argument;
    }

    /**
     * Create a new Argument object.
     *
     * @return a new Argument object
     */
    protected Argument makeNewArgument() {
        return new Argument("", ""); // $NON-NLS-1$ // $NON-NLS-2$
    }

    /**
     * Stop any editing that is currently being done on the table. This will
     * save any changes that have already been made.
     * Needed for subclasses
     */
    protected void stopTableEditing() {
        GuiUtils.stopTableEditing(table);
    }

    /**
     * Initialize the table model used for the arguments table.
     */
    protected void initializeTableModel() {
        if (tableModel == null) {
            if(standalone) {
                tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1, COLUMN_RESOURCE_NAMES_2 },
                    Argument.class,
                    new Functor[] {
                    new Functor("getName"), // $NON-NLS-1$
                    new Functor("getValue"),  // $NON-NLS-1$
                    new Functor("getDescription") },  // $NON-NLS-1$
                    new Functor[] {
                    new Functor("setName"), // $NON-NLS-1$
                    new Functor("setValue"), // $NON-NLS-1$
                    new Functor("setDescription") },  // $NON-NLS-1$
                    new Class[] { String.class, String.class, String.class });
            } else {
                tableModel = new ObjectTableModel(new String[] { COLUMN_RESOURCE_NAMES_0, COLUMN_RESOURCE_NAMES_1 },
                    Argument.class,
                    new Functor[] {
                    new Functor("getName"), // $NON-NLS-1$
                    new Functor("getValue") },  // $NON-NLS-1$
                    new Functor[] {
                    new Functor("setName"), // $NON-NLS-1$
                    new Functor("setValue") }, // $NON-NLS-1$
                    new Class[] { String.class, String.class });
            }
        }
    }

    public static boolean testFunctors(){
        ArgumentsPanel instance = new ArgumentsPanel();
        instance.initializeTableModel();
        return instance.tableModel.checkFunctors(null,instance.getClass());
    }

    /**
     * Resize the table columns to appropriate widths.
     *
     * @param _table the table to resize columns for
     */
    protected void sizeColumns(JTable _table) {
    }

    /**
     * Create the main GUI panel which contains the argument table.
     *
     * @return the main GUI panel
     */
    private Component makeMainPanel() {
        initializeTableModel();
        table = new JTable(tableModel);
        table.getTableHeader().setDefaultRenderer(new HeaderAsPropertyRenderer());
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (this.background != null) {
            table.setBackground(this.background);
        }
        JMeterUtils.applyHiDPI(table);
        return makeScrollPane(table);
    }

    /**
     * Create a panel containing the title label for the table.
     *
     * @return a panel containing the title label
     */
    protected Component makeLabelPanel() {
        JPanel labelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        labelPanel.add(tableLabel);
        if (this.background != null) {
            labelPanel.setBackground(this.background);
        }
        return labelPanel;
    }

    /**
     * Create a panel containing the add and delete buttons.
     *
     * @return a GUI panel containing the buttons
     */
    private JPanel makeButtonPanel() {

        showDetail = new JButton(JMeterUtils.getResString("detail")); // $NON-NLS-1$
        showDetail.setActionCommand(DETAIL);
        showDetail.setEnabled(true);

        add = new JButton(JMeterUtils.getResString("add")); // $NON-NLS-1$
        add.setActionCommand(ADD);
        add.setEnabled(true);

        // A button for adding new arguments to the table from the clipboard
        JButton addFromClipboard = new JButton(JMeterUtils.getResString("add_from_clipboard")); // $NON-NLS-1$
        addFromClipboard.setActionCommand(ADD_FROM_CLIPBOARD);
        addFromClipboard.setEnabled(true);

        delete = new JButton(JMeterUtils.getResString("delete")); // $NON-NLS-1$
        delete.setActionCommand(DELETE);

        if (enableUpDown) {
            up = new JButton(JMeterUtils.getResString("up")); // $NON-NLS-1$
            up.setActionCommand(UP);

            down = new JButton(JMeterUtils.getResString("down")); // $NON-NLS-1$
            down.setActionCommand(DOWN);
        }
        checkButtonsStatus();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        if (this.background != null) {
            buttonPanel.setBackground(this.background);
        }
        showDetail.addActionListener(this);
        add.addActionListener(this);
        addFromClipboard.addActionListener(this);
        delete.addActionListener(this);
        buttonPanel.add(showDetail);
        buttonPanel.add(add);
        buttonPanel.add(addFromClipboard);
        buttonPanel.add(delete);
        if (enableUpDown) {
            up.addActionListener(this);
            down.addActionListener(this);
            buttonPanel.add(up);
            buttonPanel.add(down);
        }
        return buttonPanel;
    }

    /**
     * Initialize the components and layout of this component.
     */
    private void init() { // WARNING: called from ctor so must not be overridden (i.e. must be private or final)
        JPanel p = this;

        if (standalone) {
            setLayout(new BorderLayout(0, 5));
            setBorder(makeBorder());
            add(makeTitlePanel(), BorderLayout.NORTH);
            p = new JPanel();
        }

        p.setLayout(new BorderLayout());

        p.add(makeLabelPanel(), BorderLayout.NORTH);
        p.add(makeMainPanel(), BorderLayout.CENTER);
        // Force a minimum table height of 70 pixels
        p.add(Box.createVerticalStrut(70), BorderLayout.WEST);
        if (!disableButtons) {
            p.add(makeButtonPanel(), BorderLayout.SOUTH);
        }

        if (standalone) {
            add(p, BorderLayout.CENTER);
        }

        table.revalidate();
        sizeColumns(table);
    }
}
