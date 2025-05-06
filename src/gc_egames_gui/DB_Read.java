/*
   Filename: GC_EGames_430_549_AT3
   Purpose: 
        1. Make a connection to the Gold Coast E-Sports SQL databse.
        2. Execute an SQL read-only query to retrieve a result set of row values from the databse tables.
        3. Result set is then read and vlaues set up in relevant variables that a re truend for the get() method which is called from the GUI application.
   Author: Zac Makkinga
   Date: 06/05/2025
   Version: 1.0
   License (if applicable): N/A
   Notes, Fixes, Updates: N/A

*/
package gc_egames_gui;

// Import Statements for DB_Read
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DB_Read 
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
    private ResultSet rs;
    
    // Number of records that result form the SQL read execution.
    private int recordCount;
    
    //Error messages relating to SQL.
    private String errorMessage;
    
    //Object[][] 2D Array for populating JTable rows.
    private Object [][] objDataSet;
    
    //String [] Array to contain team, event, and game details to be used by JComboBoxes.
    private String [] stringCSVData;
    
    // Used to set up a competitionID for a new competiton.
    private int maxCompID;
    
    /*
        Method name: DB_Read()
        Purpose:
                Constructor method
                1. Initialise private data fields.
                2. Connect to SQL database.
                3. Run SQL read statements.
                4. Collate resulting data sets into required data structures.
        Inputs: String sql (SQL statement)
                String qryType (determines type of SQl and desired data output).
        Output: N/A
    */
    
    public DB_Read (String sql, String qryType)
    {
        // Initialise private data fields.
        dbURL = "";
        usrID = "";
        usrPWD = "";
        
        // Number of records int hte record set when the SQL statement is executed.
        recordCount = 0;
        
        // Error message string is used to store any exception message.
        errorMessage = "";
        
        // Set both arrays to null (can only initialise an array if the size is known).
        objDataSet = null;
        stringCSVData = null;
        
        // Highest value in competition table (competitionID).
        maxCompID = 0;
        
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
        
        // Make connection to databse and run SQL statements to read and get resultset data.
        try
        {
            // Make connection to database.
            conn = DriverManager.getConnection(dbURL, usrID, usrPWD);
            
            // Set up connection satement (type of result that gets returned from SQL)
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            
            // Assign result set to the execution of the SQL statement.
            rs = stmt.executeQuery(sql);
            
            // Get record count
            if (rs != null)
            {
                rs.beforeFirst();
                rs.last();
                recordCount = rs.getRow();
            }
            
            // Display attributes of each record in a while loop.
            if (recordCount > 0)
            {
                // Declare counte rand set to zero (used for the array indexes).
                int counter = 0;
                
                // Instantiate Arrays
                objDataSet = new Object[recordCount][];
                stringCSVData = new String[recordCount];
                
                // Set maxCompID to zero.
                maxCompID = 0;
                
                // Start at the beginning of the record set.
                rs.beforeFirst();
                
                // Loop via the resultset.
                while (rs.next())
                {
                    // Get all competitions data.
                    if (qryType.equals("competition"))
                    {
                        // Create object array for each row (containing all 5 required data items for one competion).
                        Object [] obj = new Object[5];
                        obj[0] = rs.getString("gameName");
                        obj[1] = rs.getString("team1");
                        obj[2] = rs.getString("team1Points");
                        obj[3] = rs.getString("team2");
                        obj[4] = rs.getString("team2Points");
                        
                        // Add the object array to the objDataSet (2D Array)
                        objDataSet[counter] = obj;
                        counter++;
                    }
                    
                    // Get all team names and put them into a  string [] array.
                    else if (qryType.equals("team"))
                    {
                        stringCSVData[counter] = rs.getString("name") + "," + rs.getString("contact") + "," + rs.getString("phone") + "," + rs.getString("email");
                        
                        // ALSO fill objDataSet row with separated columns
                        Object[] obj = new Object[4];
                        obj[0] = rs.getString("name");
                        obj[1] = rs.getString("contact");
                        obj[2] = rs.getString("phone");
                        obj[3] = rs.getString("email");

                        objDataSet[counter] = obj;
                        
                        counter++;
                    }
                    
                    // Get all events and put them into a string [] array.
                    else if (qryType.equals("event"))
                    {
                        stringCSVData[counter] = rs.getString("name") + "," + rs.getString("date") + "," + rs.getString("location");
                        counter++;
                    }
                    
                    // Get all game names and put them into a string [] array.
                    else if (qryType.equals("game"))
                    {
                        stringCSVData[counter] = rs.getString("name") + "," + rs.getString("type");
                        counter++;
                    }
                    
                    // Events leaderboard tally of points.
                    else if (qryType.equals("leaderBoard"))
                    {
                        // Store team name & points into stringCSVData
                        stringCSVData[counter] = rs.getString("name") + "," + rs.getString("points");
                        counter++;
                    }
                    
                    // Max competition ID.
                    else if (qryType.equals("maxCompID"))
                    {
                        maxCompID = rs.getInt(1);
                    }
                } // End of while loop.
                
                // Close the connection.
                conn.close();
            } // End if (record > 0).
        } // End of try block.
        
        // Catch any specific SQL exceptions.
        catch (SQLException sqlE)
        {
            errorMessage += sqlE.getMessage();
        }
        
        // Catch any other exceptions.
        catch (Exception e)
        {
            errorMessage += e.getMessage();
        }
        
        // Test only.
        if (! errorMessage.isEmpty())
        {
            System.out.println("Error(s) encountered:\n" + errorMessage);
        }
    } // End of constructor method.
    
    /*
        Method name: getRecordCount()
        Purpose: Returns integer recordCount.
        Inputs: N/A
        Output: integer (recordCount)
    */
    public int getRecordCount()
    {
        return recordCount;
    }
    
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
        Method name: getMaxCompID()
        Purpose: Returns integer maxCompID (private data field)
        Inputs: N/A
        Output: integer (maxCompID)
    */
    public int getMaxCompID()
    {
        return maxCompID;
    }
    
    /*
        Method name: getStringCSVData()
        Purpose: Returns CSV formated string array of event infor used for comboBox.
        Inputs: N/A
        Output: String (stringCSVData)
    */
    public String[] getStringCSVData()
    {
        return stringCSVData;
    }
    
    /*
        Method name: getObjDataSet()
        Purpose: Used to fill JTables with row data.
        Inputs: N/A
        Output: Object[][] 2D Array (objDataSet)
    */
    public Object[][] getObjDataSet()
    {
        return objDataSet;
    }
    
    /*
        Method name: formatDateToString()
        Purpose: Returns String version of a formatted date.
        Inputs: Formatted date e.g "2022-03-01"
        Output: String version e.g: "01-Mar-2022"
    */
    //DB_Read.formatDateToString("2022-03-01");
    public static String formatDateToString(String inputDateString)
    {
        // Create date string version.
        String formattedDateStr = "";
        String day = inputDateString.substring(8, 10);
        String year = inputDateString.substring(0, 4);
        String month = "Jan";
        String monthNbr = inputDateString.substring(5, 7);
        switch (monthNbr)
        {
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mar";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "Jun";
                break;
            case "07":
                month = "Jul";
                break;
            case "08":
                month = "Aug";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
            default:
                break;
        }
        
        formattedDateStr = day + "-" + month + "-" + year;
        return formattedDateStr;
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
