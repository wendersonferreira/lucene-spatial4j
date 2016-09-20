package br.com.trustsystems;

import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;

public class FileOpener {
    
    private String fileToIndex = "";

    private FileOpener() {
    }

    public FileOpener(String fileToIndex) {
        this.fileToIndex = fileToIndex;
    }
    
    public InputStreamReader getFileForReading(){
        
        InputStreamReader iStreamReader = null;
        
        try {
            iStreamReader = new InputStreamReader(new FileInputStream(new File(Thread.currentThread().getContextClassLoader().getResource(fileToIndex).toURI())));
        } catch (Exception e){
            System.out.println(" Yo - something went wrong trying to set up the file to read: " +  e.getClass() + " :: " + e.getMessage());
        }
        
        return iStreamReader;
        
        
    }
    
    
    
    
}
