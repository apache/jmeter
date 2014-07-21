package org.apache.jmeter.protocol.http.gui;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.gui.util.PowerTableModel;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.protocol.http.control.dnscachemanager.ARecord;
import org.apache.jmeter.protocol.http.control.dnscachemanager.Constants;
import org.apache.jmeter.protocol.http.control.dnscachemanager.DNSServer;
import org.apache.jmeter.protocol.http.gui.dnspanel.ARecordsPanel;
import org.apache.jmeter.protocol.http.gui.dnspanel.DNSServersPanel;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.property.PropertyIterator;
import org.apache.jorphan.gui.GuiUtils;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dzmitrykashlach on 6/13/14.
 */
public class DNSPanel extends AbstractConfigGui {

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();

    private JPanel dnsServersPanel;

    private JPanel aRecordsPanel;


    private JCheckBox clearEachIteration;

    /**
     * Default constructor.
     */
    public DNSPanel() {
        init();
    }

    @Override
    public String getStaticLabel() {
        return Constants.DNS_CACHE_MANAGER; //$NON-NLS-1$
    }

    @Override
    public String getLabelResource() {
        return null;
    }

    private void addDNSToTable(DNSServer dnsServer) {
        ((DNSServersPanel) dnsServersPanel).getTableModel().
                addRow(new Object[]{dnsServer.getName(), dnsServer.getIP(), dnsServer.getPriority()});
    }

    private void addARecordToTable(ARecord aRecord) {
        ((ARecordsPanel) aRecordsPanel).getTableModel().
                addRow(new Object[]{aRecord.getName(), aRecord.getIP(), aRecord.getExpires()});
    }

    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement dnsCm) {
        GuiUtils.stopTableEditing(((DNSServersPanel) dnsServersPanel).getDnsServersTable());
        GuiUtils.stopTableEditing(((ARecordsPanel) aRecordsPanel).getARecordsTable());
        dnsCm.clear();
        configureTestElement(dnsCm);
        if (dnsCm instanceof DNSCacheManager) {
            DNSCacheManager dnsCacheManager = (DNSCacheManager) dnsCm;
            PowerTableModel dnsServersModel = ((DNSServersPanel) dnsServersPanel).getTableModel();
            for (int i = 0; i < dnsServersModel.getRowCount(); i++) {
                DNSServer dnsServer = createDNSServer(dnsServersModel.getRowData(i));
                dnsCacheManager.addDNSServer(dnsServer);
            }
            PowerTableModel aRecordsModel = ((ARecordsPanel) aRecordsPanel).getTableModel();

            for (int i = 0; i < aRecordsModel.getRowCount(); i++) {
                ARecord aRecord = createARecord(aRecordsModel.getRowData(i));
                dnsCacheManager.addARecord(aRecord);
            }

            dnsCacheManager.setClearEachIteration(clearEachIteration.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        ((DNSServersPanel) dnsServersPanel).resetButtons();
        ((ARecordsPanel) aRecordsPanel).resetButtons();
        PowerTableModel dnsServersModel = ((DNSServersPanel) dnsServersPanel).getTableModel();
        dnsServersModel.clearData();
        PowerTableModel aRecordsModel = ((ARecordsPanel) aRecordsPanel).getTableModel();
        aRecordsModel.clearData();
        clearEachIteration.setSelected(false);
    }

    private DNSServer createDNSServer(Object[] rowData) {
        DNSServer dnsServer = new DNSServer(
                (String) rowData[0],
                (String) rowData[1],
                (Integer) rowData[2]); // Non-expiring
        return dnsServer;
    }

    private ARecord createARecord(Object[] rowData) {
        ARecord aRecord = new ARecord(
                (String) rowData[0],
                (String) rowData[1],
                (Long) rowData[2]); // Non-expiring
        return aRecord;
    }

    private void populateTables(DNSCacheManager manager) {
        PowerTableModel dnsServersModel = ((DNSServersPanel) dnsServersPanel).getTableModel();
        dnsServersModel.clearData();
        PowerTableModel aRecordsModel = ((ARecordsPanel) aRecordsPanel).getTableModel();
        aRecordsModel.clearData();
        PropertyIterator iter;

        iter = manager.getDNSServers().iterator();
        while (iter.hasNext()) {
            addDNSToTable((DNSServer) iter.next().getObjectValue());
        }

        iter = manager.getARecords().iterator();
        while (iter.hasNext()) {
            addARecordToTable((ARecord) iter.next().getObjectValue());
        }
    }

    @Override
    public TestElement createTestElement() {
        DNSCacheManager dnsCacheManager = new DNSCacheManager();
        modifyTestElement(dnsCacheManager);
        return dnsCacheManager;
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);

        DNSCacheManager dnsCacheManager = (DNSCacheManager) el;
        populateTables(dnsCacheManager);
        clearEachIteration.setSelected((dnsCacheManager).getClearEachIteration());
    }

    /**
     * Shows the main cookie configuration panel.
     */
    private void init() {
        clearEachIteration =
                new JCheckBox(Constants.CLEAR_CACHE_EACH_ITER, false); //$NON-NLS-1$
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        JPanel optionsPane = new JPanel();
        optionsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), Constants.OPTIONS)); // $NON-NLS-1$
        optionsPane.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        optionsPane.add(clearEachIteration);
        JPanel policyTypePane = new JPanel();
        policyTypePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        optionsPane.add(policyTypePane);
        northPanel.add(optionsPane);
        add(northPanel, BorderLayout.NORTH);
        dnsServersPanel = new DNSServersPanel();
        aRecordsPanel = new ARecordsPanel();
        add(dnsServersPanel, BorderLayout.CENTER);
        add(aRecordsPanel, BorderLayout.SOUTH);
    }
}

