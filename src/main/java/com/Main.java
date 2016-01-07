/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;

/**
 *
 * @author Andrew
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Global global = new Global();        
        connectionINI(global);
        if (global.indexDir != null && global.dataDir != null){
            if (Files.isDirectory(Paths.get(global.indexDir)) && Files.isDirectory(Paths.get(global.dataDir))) {
                MainPanel mp = new MainPanel(global);
            } else {
                failedPaths();
            }
        } else {
            failedPaths();
        }
    }
        
    /**
     * This reads the information from the INI file for all of the machine
     * variables that can customize the running of the indexing.
     * 
     * @param global This is for reference to the global class variables 
     * and methods.
     */
    private static void connectionINI(Global global) {
        ParseINI connectionIni = new ParseINI();
        global.indexDir = connectionIni.getIndexDirectory();
        global.dataDir = connectionIni.getDataDirectory();
        global.newIndex = connectionIni.getNewIndex();
        global.NUM_THREADS = connectionIni.getNumberOfThreads();
        global.RAM_BUFFER_SIZE = connectionIni.getRAM_BUFFER_SIZE();
        global.THREAD_SLEEP = connectionIni.getTHREAD_SLEEP();
        global.WRITE_LIMIT = connectionIni.getWRITE_LIMIT();
        global.MAX_BUFFERED_DOCS = connectionIni.getMAX_BUFFERED_DOCS();
        global.MERGE_FACTOR = connectionIni.getMERGE_FACTOR();
        global.mergeCapable = connectionIni.isMergeCapable();
        global.dayOfWeek = connectionIni.getDayOfWeek();
        global.hourOfDay = connectionIni.getHourOfDay();
    }
    
    /**
     * This is a pop-up for failed path resolve
     */
    private static void failedPaths(){
        JOptionPane.showMessageDialog(null, "<html>Unable to resolve directories.<br>Please verify they are accessible.</html>", "Error", JOptionPane.ERROR_MESSAGE);
    }
}
