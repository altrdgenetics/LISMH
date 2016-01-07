package com;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Andrew
 */
public class MainPanel extends javax.swing.JFrame {

    Global global;
    Image image;

    /**
     * Creates new form MainPanel
     *
     * @param globalPassed
     */
    public MainPanel(Global globalPassed) {
        initComponents();
        global = globalPassed;
        setDefaults();
        this.setLocationRelativeTo(null);
    }

    private void setDefaults() {
        global.setMainPanel(this);
//        getImage();
//        setIcons();
//        windowListeners();
//        checkSystemTray();
        lastIndexTime();
        runMergeTimerThread();
        runIndexThread();
    }

    
    /**
     * load image for use in windows experience.
     */
    private void getImage() {
        image = new ImageIcon(this.getClass().getResource("image.png")).getImage();
    }

    /**
     * Changes the image resource in the header and in the task-bar.
     */
    private void setIcons() {
        setIconImage(image);
    }

    /**
     * Windows specific: This checks to see if the system tray is available and
     * if so it will load up the ability to minimize the application to the tray.
     */
    private void checkSystemTray(){
        if (SystemTray.isSupported()) {
            this.setVisible(true);
        } else {
            systemTray();
        }
    }
    
    /**
     * If we are just updating the index and not creating a new one we pull the 
     * last time the index was ran if the application was closed out. That way
     * we can limit the amount of files that need to be index.
     */
    private void lastIndexTime(){
        if (global.newIndex == false){
            ParseTime connectionIni = new ParseTime();
            global.lastIndexTime = connectionIni.getLastIndexTime();
        }
    }    
    
    /**
     * This window listener just checks for tray icon capable and minimizing
     * if it is.
     */
    private void windowListeners() {
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowIconified(WindowEvent e) {
                systemTray();
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                removeTrayIcon();
            }

            @Override
            public void windowClosing(WindowEvent e) {
                systemTray();
            }
        });

    }

    /**
     * The thread for in indexing. Separating it out allows for the updating of 
     * the AWT elements so we can duplicate the console text.
     */
    private void runIndexThread() {
        DefaultCaret caret = (DefaultCaret) jTextArea1.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        global.threadOne = new Thread() {
            @Override
            public void run() {
                ThreadIndexing ixd = new ThreadIndexing();
                ixd.IndexThread(global);
            }
        };
        global.threadOne.start();
    }
    
    /**
     * When the INI flag is set to allow merging this sets the timer so we can
     * merge when the Day of the week and hour allows us to do so.
     */
    private void runMergeTimerThread() {
        if (global.mergeCapable == true){
        global.threadTwo = new Thread() {
            @Override
            public void run() {
                ThreadMergeScheduler mrg = new ThreadMergeScheduler();
                mrg.MergeThread(global);
            }
        };
        global.threadTwo.start();
        }
    }

    /**
     * Build the tray item and the context menu if supported. Once built it adds
     * the tray item.
     */
    private void systemTray() {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        final PopupMenu popup = new PopupMenu();
        global.trayIcon = new TrayIcon(image, "XLN Document Indexer");
        global.tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem Restore = new MenuItem("Restore");
        MenuItem exitItem = new MenuItem("Exit");

        //Add listners
        global.trayIcon.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    removeTrayIcon();
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
        });

        ActionListener restoreListener = (ActionEvent e) -> {
            removeTrayIcon();
        };

        ActionListener exitListener = (ActionEvent e) -> {
            exitApplication();
        };

        Restore.addActionListener(restoreListener);
        exitItem.addActionListener(exitListener);

        //Add components to pop-up menu
        popup.add(Restore);
        popup.addSeparator();
        popup.add(exitItem);

        global.trayIcon.setImageAutoSize(true);
        global.trayIcon.setPopupMenu(popup);

        try {
            global.tray.add(global.trayIcon);
            global.trayActive = true;
            this.setVisible(false);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    /**
     * Removing the tray icon from the tray if supported.
     */
    private void removeTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }
        this.setVisible(true);
        this.setExtendedState(javax.swing.JFrame.NORMAL);
        global.tray.remove(global.trayIcon);
        global.trayActive = false;
    }

    /**
     * Exit the Application as long as the index is not locked
     */
    private void exitApplication() {
        if (global.lockIndex == false) {
            System.exit(0);
        } else {
            global.exitNow = true;
        }
    }

    /**
     * Gets the main text area where the console printout is duplicated.
     * @return 
     */
    public JTextArea getjTextArea1() {
        return jTextArea1;
    }

    /**
     * Sets the main text area where the console printout is duplicated.
     * @param jTextArea1 
     */
    public void setjTextArea1(JTextArea jTextArea1) {
        this.jTextArea1 = jTextArea1;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        FileMenu = new javax.swing.JMenu();
        MenuExit = new javax.swing.JMenuItem();
        MenuForceExit = new javax.swing.JMenuItem();
        EditMenu = new javax.swing.JMenu();
        MenuClearText = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("XLN Document Indexer");

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("XLN Document Indexing Server");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        FileMenu.setText("File");

        MenuExit.setText("Exit");
        MenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuExitActionPerformed(evt);
            }
        });
        FileMenu.add(MenuExit);

        MenuForceExit.setText("Force Exit");
        MenuForceExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuForceExitActionPerformed(evt);
            }
        });
        FileMenu.add(MenuForceExit);

        jMenuBar1.add(FileMenu);

        EditMenu.setText("Edit");

        MenuClearText.setText("Clear Text");
        MenuClearText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuClearTextActionPerformed(evt);
            }
        });
        EditMenu.add(MenuClearText);

        jMenuBar1.add(EditMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 973, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void MenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuExitActionPerformed
        this.setVisible(false);
        exitApplication();
    }//GEN-LAST:event_MenuExitActionPerformed

    private void MenuForceExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuForceExitActionPerformed
        global.forceExit = true;
        if (global.executorRunning == true){
            LuceneIndexer.kill();
        }
        System.err.println("EXIT NOW!");
        exitApplication();
    }//GEN-LAST:event_MenuForceExitActionPerformed

    private void MenuClearTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuClearTextActionPerformed
        jTextArea1.setText("");
    }//GEN-LAST:event_MenuClearTextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu EditMenu;
    private javax.swing.JMenu FileMenu;
    private javax.swing.JMenuItem MenuClearText;
    private javax.swing.JMenuItem MenuExit;
    private javax.swing.JMenuItem MenuForceExit;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration//GEN-END:variables
}
