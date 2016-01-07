/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andrew
 */
public class ParseTime {
    
    private Date lastIndexTime;
    private File timeFile = new File("time.txt");
    
    
    /**
     * Grabs the information in time format for the last time the index was ran
     */
    public ParseTime() {
        try {
            Scanner reader = new Scanner(new FileInputStream(timeFile));
            
            String line;
            while(reader.hasNextLine()) {
                line = reader.nextLine().trim();
                if(line.startsWith("time =")) {
        // Time in long format
                    lastIndexTime = new Date(Long.parseLong(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""))));
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ParseINI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Grabs the time in long format for when the index was last ran.
     * 
     * @return Long - time
     */
    public Date getLastIndexTime() {
        return lastIndexTime;
    }

}
