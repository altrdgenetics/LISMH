/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;
import java.util.Scanner;
import org.apache.log4j.Logger;

/**
 *
 * @author Andrew
 */
public class ParseINI {
    final static Logger log = Logger.getLogger(ParseINI.class);
    private boolean mergeCapable;
    private boolean newIndex;
    private double RAM_BUFFER_SIZE;
    private File iniFile = new File("ini.txt");
    private int dayOfWeek;
    private int hourOfDay;
    private int MAX_BUFFERED_DOCS;
    private int MERGE_FACTOR;
    private int numberOfThreads;
    private int THREAD_SLEEP;
    private int WRITE_LIMIT;
    private String dataDir;
    private String indexDir;
    
    /**
     * Grabs the information used for the customization of the application for 
     * indexing purposes.
     */
    public ParseINI() {
        try {
            Scanner reader = new Scanner(new FileInputStream(iniFile));
            
            String line;
            while(reader.hasNextLine()) {
                line = reader.nextLine().trim();
                if(line.startsWith("DataDirectory =")) {
        //Directory where the files to index are.
                    dataDir = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                }else if(line.startsWith("IndexDirectory =")) {
        //Directory where the index is located
                    indexDir = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                }else if(line.startsWith("CreateNewIndex =")) {
        //Determines if a new index should be created
                    newIndex = "true".equals(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("NumberOfThreads =")) {
        //Determined how many threads the indexer will utilize, only use as many cores available.
                    numberOfThreads = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("RAM_BUFFER_SIZE =")) {
        //Set the ram buffer size, make sure the JVM is set to match            
                    RAM_BUFFER_SIZE = Double.parseDouble(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("THREAD_SLEEP =")) {
        //How long the thread will sleep between each time the index runs            
                    THREAD_SLEEP = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("WRITE_LIMIT =")) {
        //Apache Tika: maximum number of characters to include in the string, or -1 to disable the write limit.            
                    WRITE_LIMIT = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("MAX_BUFFERED_DOCS =")) {
        //Determines the amount of RAM that may be used for buffering added documents and deletions before they are flushed to the Directory.            
                    MAX_BUFFERED_DOCS = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("MERGE_FACTOR =")) {
        //With smaller values, less RAM is used while indexing, and searches on unoptimized indices are faster, but indexing speed is slower. 
        //With larger values, more RAM is used during indexing, and while searches on unoptimized indices are slower, indexing is faster. 
        //Thus larger values (> 10) are best for batch index creation, and smaller values (< 10) for indices that are interactively maintained. 
                    MERGE_FACTOR = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("Day =")) {
        //Day of the week to merge the index
                    String day = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
                    if(null != day)switch (day) {
                        case "Monday":
                            dayOfWeek = Calendar.MONDAY;
                            break;
                        case "Tuesday":
                            dayOfWeek = Calendar.TUESDAY;
                            break;
                        case "Wednesday":
                            dayOfWeek = Calendar.WEDNESDAY;
                            break;
                        case "Thursday":
                            dayOfWeek = Calendar.THURSDAY;
                            break;
                        case "Friday":
                            dayOfWeek = Calendar.FRIDAY;
                            break;
                        case "Saturday":
                            dayOfWeek = Calendar.SATURDAY;
                            break;
                        case "Sunday":
                            dayOfWeek = Calendar.SUNDAY;
                            break;
                    }
                }else if(line.startsWith("Hour =")) {
        //Hour of the day to merge the index
                    hourOfDay = Integer.parseInt(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }else if(line.startsWith("Merge =")) {
        //If the index is mergable this will allow the scheduled merge to be possible.
                    mergeCapable = "true".equals(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
                }
            }
        } catch (FileNotFoundException ex) {
            log.error("This is Severe : " + "Unable to locateINI file");
        }
    }

    /**
     * Directory where the files to index are.
     * 
     * @return String - Path in string form
     */
    public String getDataDirectory() {
        return dataDir;
    }

    /**
     * Directory where the index is located
     * 
     * @return String - Path in string form
     */
    public String getIndexDirectory() {
        return indexDir;
    }
    
    /**
     * Determines if a new index should be created
     * 
     * @return boolean - (true/false) 
     */
    public boolean getNewIndex() {
        return newIndex;
    }
    
    /**
     * Determined how many threads the indexer will utilize, 
     * only use as many cores available.
     * 
     * @return integer
     */
    public int getNumberOfThreads(){
        return numberOfThreads;
    }

    /**
     * Set the ram buffer size in MB, make sure the JVM is set to match 
     * 
     * @return integer - ex. 256.0, 512.0 1024.0
     */
    public double getRAM_BUFFER_SIZE() {
        return RAM_BUFFER_SIZE;
    }

    /**
     * How long the thread will sleep between each time the index runs  
     * 
     * @return integer - in milliseconds
     */
    public int getTHREAD_SLEEP() {
        return THREAD_SLEEP;
    }

    /**
     * Apache Tika: maximum number of characters to include in the string
     * or -1 to disable the write limit.  
     * 
     * @return integer
     */
    public int getWRITE_LIMIT() {
        return WRITE_LIMIT;
    }

    /**
     * Determines the amount of RAM that may be used for buffering added 
     * documents and deletions before they are flushed to the Directory.  
     * 
     * @return integer
     */
    public int getMAX_BUFFERED_DOCS() {
        return MAX_BUFFERED_DOCS;
    }

    /**
     * The amount of documents held in RAM before being written to disk
     * 
     * @return integer
     */
    public int getMERGE_FACTOR() {
        return MERGE_FACTOR;
    }

    /**
     * Day of the week to merge the index
     * 
     * @return integer - Calendar index integer for day
     */
    public int getDayOfWeek() {
        return dayOfWeek;
    }

    /**
     * Hour of the day to merge the index Uses 24hr clock
     * 
     * @return integer - 0-24
     */
    public int getHourOfDay() {
        return hourOfDay;
    }

    /**
     * If the index is able to be merged this will allow the scheduled 
     * merge to be possible.
     * 
     * @return Boolean - (true/false)
     */
    public boolean isMergeCapable() {
        return mergeCapable;
    }
    
}
