package by.andruhovich.icmp.traceroute;

import by.andruhovich.icmp.icmp.IcmpPingRequest;
import by.andruhovich.icmp.icmp.IcmpPingResponse;
import by.andruhovich.icmp.icmp.IcmpPingUtil;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Trace-route/ping application using ICMP from Jpcap
 *
 * @author Mikica B Kocic
 */
public class TraceroutePane extends JFrame implements Traceroute.Context {
    /**
     * Implements java.io.Serializable interface
     */
    private static final long serialVersionUID = 3590355845490077407L;

    /*  GUI components
     */
    private JButton startButton;
    private JTextField inputCmd;
    private JTextArea logArea;
    private Traceroute tracer;
    private JComboBox ifaceList;

    /**
     * Creates a new instance of the <code>ChatClientFrame</code>.
     *
     * @param args the command line arguments passed to main
     */
    public TraceroutePane(String args[]) {
        super("IP1-4.3: Traceroute, Idle");

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //////////////////////////////////////////////////////////////////////////////////

        /* GUI Components
         */
        Font textFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        Font logFont = new Font(Font.MONOSPACED, Font.PLAIN, 16);

        inputCmd = new JTextField("atlas.dsv.su.se");
        inputCmd.setFont(textFont);
        inputCmd.setColumns(30);
        inputCmd.selectAll();

        startButton = new JButton();
        startButton.setText("Go!");
        startButton.setForeground(Color.black);

        startButton.addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        startButton_Clicked(evt);
                    }
                }
        );

        ifaceList = new JComboBox();
        ifaceList.setEditable(false);

        /* Scrollable log area
         */
        logArea = new JTextArea();

        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setEditable(false);
        logArea.setFont(logFont);
        logArea.setBackground(new Color(32, 32, 0));
        logArea.setForeground(new Color(240, 192, 0));

        JScrollPane logPane = new JScrollPane();
        logPane.setViewportView(logArea);

        //////////////////////////////////////////////////////////////////////////////////

        /* Layout (self explained?):
         *     Upper: startButton, inputCmd and ifaceList
         *     Lower: logPane
         */
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);

        layout.setHorizontalGroup
                (
                        layout
                                .createParallelGroup(Alignment.LEADING)
                                .addGroup
                                        (
                                                layout
                                                        .createSequentialGroup()
                                                        .addGroup
                                                                (
                                                                        layout
                                                                                .createParallelGroup(Alignment.LEADING)
                                                                                .addGroup
                                                                                        (
                                                                                                Alignment.TRAILING,
                                                                                                layout
                                                                                                        .createSequentialGroup()
                                                                                                        .addComponent(startButton, 80, 80, 80)
                                                                                                        .addComponent(inputCmd)
                                                                                                        .addComponent(ifaceList, 100, 300, 300)

                                                                                        )
                                                                                .addComponent(logPane)
                                                                )
                                        )
                );

        layout.setVerticalGroup
                (
                        layout
                                .createParallelGroup(Alignment.LEADING)
                                .addGroup
                                        (
                                                layout
                                                        .createSequentialGroup()
                                                        .addGroup
                                                                (
                                                                        layout
                                                                                .createParallelGroup(Alignment.CENTER)
                                                                                .addComponent(startButton)
                                                                                .addComponent(inputCmd, Alignment.CENTER, 0, 27, 27)
                                                                                .addComponent(ifaceList, Alignment.CENTER, 0, 26, 26)
                                                                )
                                                        .addComponent(logPane)
                                        )
                );

        pack();

        //////////////////////////////////////////////////////////////////////////////////

        /* Adjust window dimensions not to exceed screen dimensions ...
         */
        Dimension win = new Dimension(1024, 600);
        Dimension scsz = Toolkit.getDefaultToolkit().getScreenSize();
        win.width = Math.min(win.width, scsz.width);
        win.height = Math.min(win.height, scsz.height - 40);
        setSize(win);

        /* ... then center window on the screen.
         */
        setLocation((scsz.width - win.width) / 2, (scsz.height - 40 - win.height) / 2);

        /* Ready for user to type in something...
         */
        inputCmd.requestFocus();

        /* Keyboard event handler for ENTER in input text field
         */
        inputCmd.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ENTER) {
                    parseCommand();
                }
            }
        });

        /* Creates trace-route instance. We should have heavy exception handling here
         * as we are relying on external components: Jpcap and WinPCAP.
         * Both must be installed properly for <code>Traceroute</code> to be
         * instantiated.
         */
        try {
            tracer = new Traceroute(this);
        } catch (NoClassDefFoundError e) // Jpcap is probably missing
        {
            tracer = null;

            if (e.getMessage().toLowerCase().contains("jpcap")) {
                println(" ");
                println(" ERROR:");
                println("     Jpcap is not installed!");
                logNewLine();
                println(" Please, download and install Jpcap from: ");
                println("     http://netresearch.ics.uci.edu/kfujii/Jpcap/doc/download.html");
                logNewLine();
                println(" You will also need to get and install WinPCAP from: ");
                println("     http://www.winpcap.org/install/default.htm");
            } else {
                e.printStackTrace();
            }
        } catch (UnsatisfiedLinkError e) // Wincap is probably missing
        {
            tracer = null;

            if (e.getMessage().toLowerCase().contains("cap.dll")) {
                println(" ");
                println(" ERROR:");
                println("     WinPCAP is not installed!");
                logNewLine();
                println(" Please, download and install WinPCAP from: ");
                println("     http://www.winpcap.org/install/default.htm");
                logNewLine();
            } else {
                e.printStackTrace();
            }
        }

        if (tracer != null) // survived Jpcap & WinPCAP i.e. up and running
        {
            /* Print usage
             */
            println(" ");
            println(" Usage:");
            println("    1) Select network interface used for transmission"
                    + " (from the upper right combo box)");
            println("    2) Enter hostname then press Enter or click 'Go!'");
            logNewLine();
            println(" To ping instead to trace route, add 'ping' after the hostname.");
            logNewLine();

            /* Add all interfaces to combo box
             */
            for (String s : tracer.getInterfaceList()) {
                ifaceList.addItem(s);
            }
        }

        /* Transfer command line arguments into inputCmd (if any)
         * then parse them immediately...
         */
        if (args.length > 0) {
            String join = "";
            for (String s : args) {
                join += (join.length() == 0 ? s : " " + s);
            }
            inputCmd.setText(join);
            parseCommand();
        }
    }

    /**
     * Starts fetching data from URL specified in <code>urlText</code>.
     */
    private void startButton_Clicked(MouseEvent evt) {
        parseCommand();
    }

    /**
     * Gets current time stamp (millis resolution)
     *
     * @return time in ISO format
     */
    private static String now() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        return sdf.format(cal.getTime());
    }

    /**
     * println() == short cut for logMesage() + logNewLine () without timestamp
     */
    public void println(String str) {
        logMessage(str, /*timestamp*/ false);
        logNewLine();
    }

    /**
     * Logs new line
     */
    @Override
    public void logNewLine() {
        synchronized (logArea) {
            if (logArea.getText().length() != 0) {
                logArea.append("\n");

                logArea.setRows(logArea.getRows() + 1);
                logArea.setCaretPosition(logArea.getText().length());
            }
        }
    }

    /**
     * Logs message, with optional timestamp
     *
     * @param str       message that will be logged
     * @param timestamp whether to prefix message with timestamp or not
     */
    @Override
    public void logMessage(String str, boolean timestamp) {
        synchronized (logArea) {
            if (timestamp) {
                // Ensure that there is a new-line before time-stamp
                //
                String log = logArea.getText();
                if (log.length() > 0 && log.charAt(log.length() - 1) != '\n') {

                    logNewLine();
                }

                logArea.append(now() + "  " + str);
            } else {
                logArea.append(str);
            }

            logArea.setCaretPosition(logArea.getText().length());
        }
    }

    /**
     * On trace route completed call-back from the instance of <code>Traceroute</code>
     */
    @Override
    public void onTracerouteCompleted() {
        setTitle("IP1-4.3: Traceroute, Idle");
        startButton.setText("Go!");
        startButton.setForeground(Color.black);
    }

    /**
     * Parses input message or command from inputMsg.
     * Also performs various commands :open, :close, :exit ...
     */
    public void parseCommand() {
        if (tracer == null) {
            return;
        }

        /* Split string into words, removing all leading, trailing
         * and superfluous (btw words) white-spaces
         */
        String[] args = inputCmd.getText().trim().split("\\s{1,}");

        if (args.length < 1) {
            return;
        }

        if (tracer.isIdle()) {
            boolean ping = (args.length >= 2 && args[1].equalsIgnoreCase("ping"));

            startButton.setText("Cancel");
            startButton.setForeground(Color.red);

            if (ping) {
                setTitle("IP1-4.3: Pinging " + args[0] + "...");
            } else {
                setTitle("IP1-4.3: Tracing route to " + args[0] + "...");
            }

            /* Start trace-route/ping. TTL is set to 64 if second word in cmd line
             * is 'ping'.
             */
            tracer.startPinging(ifaceList.getSelectedIndex(), args[0], ping ? 64 : 0);
        } else {
            tracer.stopTrace();
        }
    }

    /**
     * Main entry point. Creates and makes visible GUI...
     *
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        final IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();

        request.setHost("www.google.org");
        for (int count = 1; count <= 4; count++) {
            final IcmpPingResponse response = IcmpPingUtil.executePingRequest(request);
            final String formattedResponse = IcmpPingUtil.formatResponse(response);
            System.out.println(formattedResponse);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        System.loadLibrary("jpcap");
        System.loadLibrary("wpcap");
        final String[] copyOfArgs = args;

        java.awt.EventQueue.invokeLater(() -> new TraceroutePane(copyOfArgs).setVisible(true));
    }
}

/*!
 *  \mainpage ICMP Traceroute and Ping based on Jpcap
 *
 *  \section s_intro Introduction
 *
ef p_task as a part of
 *  the <a href="http://dsv.su.se/utbildning/distans/ip1" target="_blank"><b>SU/IP1
 *  course</b></a>.
 *
 *  \image html traceRouteApp.png
 *
 *  \section s_desc Description
 *
 *  Application's graphical user interface is implemented in the class TraceroutePane.
 *  The application offers user to enter IP address or hostname and to select network
 *  interface used for originating ICMP packets.
 *
 *  The applications accepts commands in the following format:
 *
 *   - <code>&lt;host&gt; [ ping ]</code>
 *
 *  where optional keyword <i>ping</i> (following the host name) indicates to
 *  application to perform ping instead of trace route.
 *
 *  ICMP packets are sent and received in a separate worker thread  Traceroute.run()
 *  instantiated from the class Traceroute. User can stop trace route at any time by
 *  pressing "Cancel" button (the thread will be stopped gracefully).
 *
 *  One can alternatively start application from the os command line with parameters:
 *
 *   - <code>java -jar <a href="../traceRoute.jar">traceRoute.jar</a>
 *     [ &lt;host&gt; [ ping ] ]</code>
 *
 *  where the default is to trace route and not to ping.
 *
 *  \section s_req Requirements
 *
 *  <div style="color:darkred; font-weight: bold;">
 *  Application needs the following components to be installed:
 *  </div>
 *
 *   - <a href="http://netresearch.ics.uci.edu/kfujii/Jpcap/doc/download.html" target="_blank">
 *     <b>Jpcap</b></a>: java library for capturing and sending network packets<br>
 *   - <a href="http://www.winpcap.org/install/default.htm" target="_blank">
 *     <b>WinPCAP</b></a>: (industry standard) windows capture library.
 *
 *  \section s_jar Executable
 *
 *  The jar file of the package can be found <a href="../traceRoute.jar"><b>here</b></a>.
 *
 *  \section s_src Sources
 *
 *  Source files:
ef Traceroute.java
ef TraceroutePane.java
 *
 */
/*! \page p_task IP1-4.3 Uppgift
 *
 *  Gor ett program som implementerar ping, traceroute eller nagot annat
 *  natverksprogram som anvander eller nagot som man inte kan gora i standardversionen
 *  av Java. Man ska inte anropa motsvarande funktioner i operativsystemet via
 *  exempelvis: <code>Runtime.getRuntime().exec("myprog.exe")</code> utan man ska
 *  anvanda ett Java-paket som anropar C-kod som implementerar raw sockets.
 */