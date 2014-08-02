package org.apache.jmeter.protocol.http.gui;

import org.apache.jmeter.config.gui.AbstractConfigGui;
import org.apache.jmeter.protocol.http.control.DNSCacheManager;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.layout.VerticalLayout;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import javax.swing.*;
import java.awt.*;

/**
 * Created by dzmitrykashlach on 6/13/14.
 */
public class DNSPanel extends AbstractConfigGui {
    public static final String CLEAR_CACHE_EACH_ITER = "Clear cache each iteration";
    public static final String OPTIONS = "Options";

    private static final long serialVersionUID = 240L;

    private static final Logger log = LoggingManager.getLoggerForClass();



    private JCheckBox clearEachIteration;

    /**
     * Default constructor.
     */
    public DNSPanel() {
        init();
    }


    @Override
    public String getLabelResource() {
        return "dns_cache_manager_title";
    }



    /**
     * Modifies a given TestElement to mirror the data in the gui components.
     *
     * @see org.apache.jmeter.gui.JMeterGUIComponent#modifyTestElement(org.apache.jmeter.testelement.TestElement)
     */
    @Override
    public void modifyTestElement(TestElement dnsCm) {
        dnsCm.clear();
        configureTestElement(dnsCm);
        if (dnsCm instanceof DNSCacheManager) {
            DNSCacheManager dnsCacheManager = (DNSCacheManager) dnsCm;
            dnsCacheManager.setClearEachIteration(clearEachIteration.isSelected());
        }
    }

    /**
     * Implements JMeterGUIComponent.clearGui
     */
    @Override
    public void clearGui() {
        super.clearGui();
        clearEachIteration.setSelected(false);
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
        clearEachIteration.setSelected((dnsCacheManager).getClearEachIteration());
    }

    /**
     * Shows the main cookie configuration panel.
     */
    private void init() {
        clearEachIteration =
                new JCheckBox(CLEAR_CACHE_EACH_ITER, false); //$NON-NLS-1$
        setLayout(new BorderLayout());
        setBorder(makeBorder());
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        northPanel.add(makeTitlePanel());
        JPanel optionsPane = new JPanel();
        optionsPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), OPTIONS)); // $NON-NLS-1$
        optionsPane.setLayout(new VerticalLayout(5, VerticalLayout.BOTH));
        optionsPane.add(clearEachIteration);
        JPanel policyTypePane = new JPanel();
        policyTypePane.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        optionsPane.add(policyTypePane);
        northPanel.add(optionsPane);
        add(northPanel, BorderLayout.NORTH);
    }
}

