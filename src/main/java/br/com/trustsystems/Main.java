package br.com.trustsystems;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;

public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        System.out.println("Starting to create the index");
        
        //Open the file of JSON for reading
        FileOpener fOpener = new FileOpener("parks.txt");
        
        //Create the object for writing
        LuceneWriter luceneWriter = new LuceneWriter("indexDir");
        
        //This is from Jackson which allows for binding the JSON to the Park.java class
        ObjectMapper objectMapper = new ObjectMapper();
        
        
        try {
            
            //first see if we can open a directory for writing
            if (luceneWriter.openIndex()){
                //get a buffered reader handle to the file
                BufferedReader breader = new BufferedReader(fOpener.getFileForReading());
                String value = null;
                //loop through the file line by line and parse 
                while((value = breader.readLine()) != null){
                    Park park  = objectMapper.readValue(value, Park.class);
                    
                    //now submit each park to the lucene writer to add to the index
                    luceneWriter.addPark(park);
                    
                }
                
            } else {
                System.out.println("We had a problem opening the directory for writing");
            }
             
            
        } catch (Exception e) {
            System.out.println("Threw exception " + e.getClass() + " :: " + e.getMessage());
        } finally {
            luceneWriter.finish();
        }
        
        System.out.println("Finished created the index");
    }
    
}
