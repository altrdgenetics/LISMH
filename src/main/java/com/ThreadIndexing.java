/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ThreadIndexing {    
    /**
     * Thread for indexing with Lucene 
     * 
     * @param global This is for reference to the global class variables 
     * and methods.
     */
    public void IndexThread(Global global) {
        try {
            Thread.sleep(1000);
            while (true) {
                try {
                    //lock index so the application can cleanly exit.
                    global.lockIndex = true;
                    
                    //Index the files to the database
                    LuceneIndexer.IndexFiles(global, global.newIndex);
                    
                    //Printout the sleep information
                    System.out.println("Sleeping for: " + TimeUnit.MILLISECONDS.toMinutes(global.THREAD_SLEEP) + "min");
                    //unlock the application
                    global.lockIndex = false;

                    //Sleep the thread based on the INI file variable.
                    Thread.sleep(global.THREAD_SLEEP);
                    
                } catch (InterruptedException ex) {
                   Logger.getLogger(ThreadIndexing.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ThreadIndexing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
