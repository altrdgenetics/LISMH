/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

/**
 *
 * @author Andrew
 */
public class BaseClass {

Global global;
final Logger log = Logger.getLogger(BaseClass.class);
    
    public void setDefaults() {
        global = new Global();
        connectionINI();
        if (global.indexDir != null && global.dataDir != null){
            if (Files.isDirectory(Paths.get(global.indexDir)) && Files.isDirectory(Paths.get(global.dataDir))) {
                lastIndexTime();
                runMergeTimerThread();
                runIndexThread();
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
    private void connectionINI() {
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
    private void failedPaths(){
        log.fatal("Unable to resolve directories");
        JOptionPane.showMessageDialog(null, "<html>Unable to resolve directories.<br>Please verify they are accessible.</html>", "Error", JOptionPane.ERROR_MESSAGE);  
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
     * The thread for in indexing. Separating it out allows for the updating of 
     * the AWT elements so we can duplicate the console text.
     */
    private void runIndexThread() {
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
}


