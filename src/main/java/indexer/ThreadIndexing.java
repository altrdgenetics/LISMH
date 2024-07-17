/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package indexer;

import com.Global;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 *
 * @author Andrew
 */
public class ThreadIndexing {    
    
    static Logger log = Logger.getLogger(ThreadIndexing.class);
    
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
                   log.fatal("Thread Interrupted");
                }
            }
        } catch (InterruptedException ex) {
            log.fatal("Thread Interrupted");
        }
    }
    
}
