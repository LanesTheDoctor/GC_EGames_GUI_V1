/*
   Filename: GC_EGames_430_549_AT3
   Purpose: 
        1. Make a connection to the Gold Coast E-Sports SQL databse.
        2. Execute an SQL write-only query to insert a new record into the database or update an existing record.
   Author: Zac Makkinga
   Date: 07/05/2025
   Version: 1.0
   License (if applicable): N/A
   Notes, Fixes, Updates: N/A

*/
package gc_egames_gui;

// Import Statements for DB_Write
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class DB_Write 
{
    // Private data fields.
    // Database URL
    private String dbURL;
    
    // User ID to access databse.
    private String usrID;
    
    // User password to access databse.
    private String usrPWD;
    
    // Private data fields for databse connection and SQL operations.
    private Connection conn;
    private Statement stmt;
    private String errorMessage;
    
    /*
        Method name: DB_Write() constructor
        Purpose: 
                1. Initialise private data fields.
                2. Connect to the database.
                3. Run SQL write statements.
        Inputs: String sql (SQL Statement)
        Output: N?A
     */
    public DB_Write (String sql)
    {
        // Initialise private data fields.
        dbURL = "";
        usrID = "";
        usrPWD = "";
        errorMessage = "";
        
        // Read exernal config file (app.config).
        try
        {
            // File io buffered reader, this is used to read the config file.
            BufferedReader br = new BufferedReader(new FileReader("app.config"));
            
            // Get first line from external file.
            String line = br.readLine();
            
            // Set line counter (first line read in)
            int lineCounter = 1;
            
            // Loop while the line value is NOT NULL
            while (line !=null)
            {
                switch(lineCounter)
                {
                    // Read dbURL string
                    case 1:
                        dbURL = line.substring(6, line.length());
                        break;
                        
                    // Read usrID
                    case 2:
                        usrID = line.substring(6, line.length());
                        break;
                    
                    // Read usrPWD
                    case 3:
                        usrPWD = line.substring(7, line.length());
                        break;
                        
                    default:
                        break;
                }
                
                // Read further lines.
                line = br.readLine();
                
                // Increment line counter.
                lineCounter++;
            } // End of while loop.
            
            // Display read in data from the app.config file.
            // System.out.println("dbURL=" + dbURL);
            // System.out.println("usrID=" + usrID);
            // System.out.println("usrPWD=" + usrPWD);
            System.out.println(this.toString());
            
            // Close file io object after while loop.
            br.close();
        } // End of try block.
        
        // Catch any IO related exceptions.
        catch (IOException ioe)
        {
            errorMessage = ioe.getMessage() + "\n";
        }
        
        // Catch any other exceptions.
        catch (Exception e)
        {
            errorMessage = e.getMessage() + "\n";
        }
        
        // Connect to database, create the SQL statement and eecute the write statement.
        try
        {
            // Connect to the database.
            conn = java.sql.DriverManager.getConnection(dbURL, usrID, usrPWD);
            
            // Create statement object for the connection.
            stmt = conn.createStatement();
            
            // executeUpdate(sql) used for INSERT INTO, UPDATE, and DELETE FROM statements.
            stmt.executeUpdate(sql);
            
            // Close connection.
            conn.close();
        } // End of try block.
        
        // Catch SQL Exceptions.
        catch (SQLException sqle)
        {
            System.out.println(sqle.getStackTrace().toString());
            errorMessage += sqle.getMessage();
        }
        
        // Catch any other exceptions.
        catch (Exception e)
        {
            errorMessage = e.getMessage() + "\n";
        }
    } // End of constructor method.
    
    /*
        Method name: getErrorMessage()
        Purpose: Returns error message.
        Inputs: N/A
        Output: String (errorMessage)
    */
    public String getErrorMessage()
    {
        return errorMessage;
    }
    
     /*
        Method name: toString() 
        Purpose: Overide from Object class toString() method, this returns a String version of the dbURl, usrID, and usrPWD.
        Inputs: N/A
        Output: String
    */
    @Override
    public String toString()
    {
        return "Database URL = " + dbURL + " USR ID " + usrID + " USR PWD = " + usrPWD; 
    }
}
