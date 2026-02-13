package org.apache.jmeter.threads.gui;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LoopControlPanel;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.threads.VirtualThreadGroup;
import org.apache.jmeter.testelement.TestElement;

public class VirtualThreadGroupGui extends AbstractThreadGroupGui {

    private static final long serialVersionUID = 285L;

    private JTextField numThreadsField;
    private JTextField rampUpField;
    private LoopControlPanel loopPanel;

    public VirtualThreadGroupGui() {
        super();
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 5));
        setBorder(makeBorder());
        add(makeTitlePanel(), BorderLayout.NORTH);

        JPanel mainPanel = new VerticalPanel();

        // Number of Threads
        JPanel numThreadsPanel = new JPanel(new BorderLayout(5, 0));
        JLabel numThreadsLabel = new JLabel("Number of Virtual Threads:");
        numThreadsField = new JTextField("1", 6);
        numThreadsPanel.add(numThreadsLabel, BorderLayout.WEST);
        numThreadsPanel.add(numThreadsField, BorderLayout.CENTER);

        // Ramp-Up Period
        JPanel rampUpPanel = new JPanel(new BorderLayout(5, 0));
        JLabel rampUpLabel = new JLabel("Ramp-Up Period (seconds):");
        rampUpField = new JTextField("1", 6);
        rampUpPanel.add(rampUpLabel, BorderLayout.WEST);
        rampUpPanel.add(rampUpField, BorderLayout.CENTER);

        // Loop Controller Panel
        loopPanel = new LoopControlPanel();

        mainPanel.add(numThreadsPanel);
        mainPanel.add(rampUpPanel);
        mainPanel.add(loopPanel);

        add(mainPanel, BorderLayout.CENTER);
    }

    @Override
    public String getLabelResource() {
        return "virtual_thread_group";
    }

    @Override
    public TestElement createTestElement() {
        VirtualThreadGroup tg = new VirtualThreadGroup();
        modifyTestElement(tg);
        return tg;
    }

    @Override
    public void modifyTestElement(TestElement element) {
        super.modifyTestElement(element);
        if (element instanceof VirtualThreadGroup) {
            VirtualThreadGroup vtg = (VirtualThreadGroup) element;

            // Set basic properties
            try {
                vtg.setNumThreads(Integer.parseInt(numThreadsField.getText()));
                vtg.setRampUp(Integer.parseInt(rampUpField.getText()));
            } catch (NumberFormatException e) {
                vtg.setNumThreads(1);
                vtg.setRampUp(1);
            }

            // CRITICAL: Set the main controller
            LoopController controller = (LoopController) loopPanel.createTestElement();
            vtg.setSamplerController(controller);
        }
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        if (element instanceof VirtualThreadGroup) {
            VirtualThreadGroup vtg = (VirtualThreadGroup) element;
            numThreadsField.setText(String.valueOf(vtg.getNumThreads()));
            rampUpField.setText(String.valueOf(vtg.getRampUp()));

            // Configure loop controller
            if (vtg.getSamplerController() != null) {
                loopPanel.configure(vtg.getSamplerController());
            }
        }
    }

    @Override
    public void clearGui() {
        super.clearGui();
        numThreadsField.setText("1");
        rampUpField.setText("1");
        loopPanel.clearGui();
    }
}
