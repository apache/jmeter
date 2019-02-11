package org.apache.jmeter.protocol.http.gui.action;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.jmeter.gui.GuiPackage;
import org.apache.jmeter.gui.action.AbstractAction;
import org.apache.jmeter.gui.action.ActionNames;
import org.apache.jmeter.gui.action.ActionRouter;
import org.apache.jmeter.gui.plugin.MenuCreator;
import org.apache.jmeter.gui.util.EscapeDialog;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.gui.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.glass.events.KeyEvent;

public class HtmlReportCommandAction extends AbstractAction implements MenuCreator, ActionListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(HtmlReportCommandAction.class);
    private static final Set<String> commands = new HashSet<>();
    public static final String HTML_REPORT = "html_report";
    private static final String CREATE_REQUEST = "CRATE_REQUEST";
    private static final String BROWSE_CSV = "BROWSE_CSV";
    private static final String BROWSE_USER_PROPERTIES = "BROWSE_USER_PROPERTIES";
    
    static {
        commands.add(HTML_REPORT);
    }
    
    private JFileChooser cSVFileChooser;
    private JFileChooser userPropertiesFileChooser;
    private JTextField cSVFilePathTextField;
    private JTextField userPropertiesFilePathTextField;

    public HtmlReportCommandAction() {
        super();
    }
    
    @Override
    public void doAction(ActionEvent e){
        ActionRouter.getInstance().doActionNow(new ActionEvent(e.getSource(), e.getID(), ActionNames.CLOSE));
        showInputDialog();
    }
    
    private void showInputDialog() {
        EscapeDialog messageDialog = new EscapeDialog(GuiPackage.getInstance().getMainFrame(), JMeterUtils.getResString("html_report_menu"), false);
        Container contentPane = messageDialog.getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        JPanel fileChooserPanel = new JPanel(new GridLayout(2,3));
        
        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("csv_file")));
        cSVFilePathTextField = new JTextField();
        fileChooserPanel.add(cSVFilePathTextField);
        
        JButton cSVFileButton = new JButton(JMeterUtils.getResString("browse"));
        cSVFileButton.setActionCommand(BROWSE_CSV);
        cSVFileButton.addActionListener(this);
        fileChooserPanel.add(cSVFileButton);
        
        
        fileChooserPanel.add(new JLabel(JMeterUtils.getResString("user_properties_file")));
        
        userPropertiesFilePathTextField = new JTextField();
        fileChooserPanel.add(userPropertiesFilePathTextField);
        
        JButton userPropertiesFileButton = new JButton(JMeterUtils.getResString("browse"));
        cSVFileButton.setActionCommand(BROWSE_USER_PROPERTIES);
        cSVFileButton.addActionListener(this);
        fileChooserPanel.add(userPropertiesFileButton);
        
        
        contentPane.add(fileChooserPanel,BorderLayout.CENTER);
        
        
        JPanel buttonPanel = new JPanel(new GridLayout(1,1));
        JButton reportLaunchButton = new JButton(JMeterUtils.getResString("html_report_request"));
        reportLaunchButton.setActionCommand(CREATE_REQUEST);
        reportLaunchButton.addActionListener(this);
        buttonPanel.add(reportLaunchButton);
        contentPane.add(buttonPanel,BorderLayout.SOUTH);
        
        //cSVFileChooser = new JFileChooser();
        //cSVFileChooser.setFileFilter(new FileNameExtensionFilter("csv_file", "csv"));
        
        //userPropertiesFileChooser = new JFileChooser();
        //userPropertiesFileChooser.setFileFilter(new FileNameExtensionFilter("user_properties_file", "properties"));
        
        messageDialog.pack();
        ComponentUtil.centerComponentInComponent(GuiPackage.getInstance().getMainFrame(), messageDialog);
        SwingUtilities.invokeLater(()-> messageDialog.setVisible(true));
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
        case CREATE_REQUEST:
            LOGGER.info("CSV file path : {}",cSVFilePathTextField.getText());
            LOGGER.info("user.properties file path : {}", userPropertiesFilePathTextField.getText());
            generateReport(cSVFilePathTextField.getText());
            break;
        case BROWSE_CSV:
            int cSVFileChooserResult = cSVFileChooser.showOpenDialog(cSVFilePathTextField);
            if(cSVFileChooserResult == JFileChooser.APPROVE_OPTION) {
                userPropertiesFilePathTextField.setText(userPropertiesFileChooser.getSelectedFile().getPath());
            }
            break;
        case BROWSE_USER_PROPERTIES:
            int userPropertiesFileChooserResult = userPropertiesFileChooser.showOpenDialog(userPropertiesFilePathTextField);
            if(userPropertiesFileChooserResult == JFileChooser.APPROVE_OPTION) {
                userPropertiesFilePathTextField.setText(userPropertiesFileChooser.getSelectedFile().getPath());
            }
            break;
        default:
            break;
        }
    }

    /**
     * Generate the HTML Report with ReportGenerator class
     * @param cSVFilePath
     */
    private void generateReport(String cSVFilePath) {
        try {
            ReportGenerator rg = new ReportGenerator(cSVFilePath, null);
            rg.generate();
        } catch (GenerationException | ConfigurationException e) {
             LOGGER.error("Error generating HTML report : {}",e.getMessage(),e);
        }
    }

    @Override
    public Set<String> getActionNames() {
        return commands;
    }



    @Override
    public JMenuItem[] getMenuItemsAtLocation(MENU_LOCATION location) {
        if(location == MENU_LOCATION.TOOLS) {
            JMenuItem menuItemIC = new JMenuItem(JMeterUtils.getResString("html_report_menu"),KeyEvent.VK_UNDEFINED);
            menuItemIC.setName(HTML_REPORT);
            menuItemIC.setActionCommand(HTML_REPORT);
            menuItemIC.setAccelerator(null);
            menuItemIC.addActionListener(ActionRouter.getInstance());
            return new JMenuItem[] {menuItemIC};
        }
        return new JMenuItem[0];
    }

    @Override
    public JMenu[] getTopLevelMenus() {
        return new JMenu[0];
    }

    @Override
    public boolean localeChanged(MenuElement menu) {
        return false;
    }

    @Override
    public void localeChanged() {
        // NOOP
    }
}
