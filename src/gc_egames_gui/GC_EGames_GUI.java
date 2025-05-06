/*
   Filename: GC_EGames_430_549_AT3
   Purpose: 
        1. Set up GUI and functionalities for the Gold Coast E-Games GUI app.
        2. Constructor method reads in data from Gold Coast E-Sports SQL database.
        3. Program usess DB_Read and DB_Write classes to connect to the SQL database to run SQL statements for the purpose of sending and retrieving data. 
   Author: Zac Makkinga
   Date: 06/05/2025
   Version: 1.0
   License (if applicable): N/A
   Notes, Fixes, Updates: N/A

*/

// Import statements for GC_EGames_GUI
package gc_egames_gui;
import java.awt.event.ItemEvent;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;

public class GC_EGames_GUI extends javax.swing.JFrame 
{
    // Private data fields
    // Required for customising the 2 JTables
    private DefaultTableModel compResultsTableModel;
    private DefaultTableModel leaderboardEventsTableModel;
    
    // Database connection and SQL processing objects.
    private DB_Read dbRead;
    private DB_Write dbWrite;

    // Currently chosen event.
    private String chosenEvent;
    
    // Currently chosen team.
    private String chosenTeam;
    
    // SQL string used for creaming SQL statements with variables.
    private String sql;
    
    // Array of CSV-formatted strings for tams, games and events.
    // Used to populate JComboBoxes after getting values from SQL queries.
    // teamsCSVStrArray uses format: "name,contact,phone,email" for each team detail.
    // gamesCSVStrArray uses format: "game".
    // eventsCSVStrArray uses format: "name,date,location" for each event detail.
    private String [] teamsCSVStrArray;
    private String [] gamesCSVStrArray;
    private String [] eventsCSVStrArray;
    
    //2D Array used for population the JTables.
    private Object [] [] objArrayForTables;
    
    // Boolean that tracsk the combo box availability after initialising the GUI.
    private boolean comboBoxStatus;
    
    /*
        Method name: GC_EGames_GUI() 
        Purpose: Constructor Method for creaming the GC_EGames_GUI JFrame window application with all applicable GUI controls and with medians to read, write to, and display SQL data.
        Inputs: N/A
        Output: N/A
    */
    public GC_EGames_GUI() 
    {
        // Custom JTables using DefaultTableModel
        // Initialise and set up customised table model for comp results.
        String [] columnNames_CompResults = new String[] {"Game", "Team 1", "Pt", "Team 2", "Pt"};
        compResultsTableModel = new DefaultTableModel();
        compResultsTableModel.setColumnIdentifiers(columnNames_CompResults);
        
        // Initialise and set up customised table model for leaderboard (all events).
        String [] columnNames_EventsLeaderboard = new String[] {"Team", "Total Points"};
        leaderboardEventsTableModel = new DefaultTableModel();
        leaderboardEventsTableModel.setColumnIdentifiers(columnNames_EventsLeaderboard);
        
        // Initialise private data fields.
        dbRead = null;
        dbWrite = null;
        
        chosenEvent = "All events";
        chosenTeam = "All teams";
        sql = "";
        
        teamsCSVStrArray = null;
        gamesCSVStrArray = null;
        eventsCSVStrArray = null;
        objArrayForTables = null;
        
        comboBoxStatus = false;
        
        // Initialise all swing controls (Note: code after "initComponents()" occurs only after the GUI controls are set up).
        initComponents();
        
        // Customise table columns for all JTables.
        // Customise column sizing for compResults_JTable.
        resizeTableColumnsForCompResults();
        
        // Customise column sizing for leaderboards_JTable.
        resizeTableColumnsForLeaderboard();
        
        // Display comp results for all events.
        displayCompResults();
        
        // Event listing in JComboBox controls.
        displayEventListing();
        
        // Team listing in JComboBox controls.
        displayTeamListing();
        
        // Game listing in JComboBox controls.
        displayGameListing();
        
        // Display leaderboard for all teams.
        // "chosenEvent" is equal to "All events".
        displayEventsLeaderboard();
        
        // Display Team data.
        displayTeamData();
                
        // Set up local date details in addNewEventDate_JTextField.
        LocalDate dateObj = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todaysDate = dateObj.format(formatter);
        addNewEventDate_JTextField.setText(todaysDate);
        addNewEventLocation_JTextField.setText("Tafe Coomera");
        
        // Set comboBoxStatus variable to "true".
        comboBoxStatus = true;
    } // End of constructor method.
    
    /*
        Method name: resizeTableColumnsForCompResults()
        Purpose: Resize table coluns for competition results JTable.
        Inputs: N/A
        Output: N/A
    */
    private void resizeTableColumnsForCompResults()
    {
        // "Game", "Team 1", "Points", "Team 2", "Points".
        float[] columnWidthPercentage = {0.3f, 0.3f, 0.05f, 0.3f, 0.05f};
        
        //Use TableColumnModel.getTotalColumnWidth() if the table is included in a JScrollPane
        int tW = compResults_JTable.getWidth();
        javax.swing.table.TableColumn column;
        javax.swing.table.TableColumnModel jTableColumnModel = compResults_JTable.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++)
        {
            column = jTableColumnModel.getColumn(i);
            int pWidth = Math.round(columnWidthPercentage[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }
    
     /*
        Method name: resizeTableColumnsForLeaderboard()
        Purpose: Resize table coluns for the leaderboard JTable.
        Inputs: N/A
        Output: N/A
    */
    private void resizeTableColumnsForLeaderboard()
    {
        // "Game", "Team 1", "Points", "Team 2", "Points".
        float[] columnWidthPercentage = {0.4f, 0.6f};
        
        //Use TableColumnModel.getTotalColumnWidth() if the table is included in a JScrollPane
        int tW = leaderboard_JTable.getWidth();
        javax.swing.table.TableColumn column;
        javax.swing.table.TableColumnModel jTableColumnModel = leaderboard_JTable.getColumnModel();
        int cantCols = jTableColumnModel.getColumnCount();
        for (int i = 0; i < cantCols; i++)
        {
            column = jTableColumnModel.getColumn(i);
            int pWidth = Math.round(columnWidthPercentage[i] * tW);
            column.setPreferredWidth(pWidth);
        }
    }

    /*
        Method name: displayCompResults()
        Purpose: Display comp results according to JComboBox selections.
        Inputs: N/A
        Output: N/A
        Note: event_JComboBox contains the event name + data/location in brackets, the date is formatted as dd-MM-yyyy.
    */
    private void displayCompResults()
    {
        // Start with the SQL string to retrieve all competition results.
        sql = "SELECT gameName, team1, team1Points, team2, team2Points FROM goldcoast_esports.competition";
        
        // Customisd String for JLabel above comp table.
        String compResultLabelText = "Competition results";
        
        // Check the string value of chosenEvent, this can change when a particular even is chosen.
        // Check if it is NOT "All events", if it is, then add a WHERE clause.
        if (! chosenEvent.equals("All events"))
        {
            sql += " WHERE eventName = '" + chosenEvent + "'";
            compResultLabelText += " for " + chosenEvent;
            
            // Check chosenTeam value is NOT "All teams", if it is then add an AND clause.
            if (! chosenTeam.equals("All teams"))
            {
                sql += " AND (team1 + '" + chosenTeam + "' OR team2 = '" + chosenTeam + "')";
                compResultLabelText += " (" + chosenTeam + ")";
            }
            
            else
            {
                compResultLabelText += " (all teams)";
            }
        }
        
        else
        {
            // At this point, the chosenEvent is "All events".
            compResultLabelText += " for all events";
            
            // Next, check the chosenTeam value is NOT "All teams", if it is, then add a WHERE clause.
            if (! chosenTeam.equals("All teams"))
            {
                sql += " WHERE (team1 = '" + chosenTeam + "' OR team2 = '" + chosenTeam + "')";
                compResultLabelText += " (" + chosenTeam + ")";
            }
            
            else
            {
                compResultLabelText += " (all teams)";
            }
        }
        
        compResults_JLabel.setText(compResultLabelText);
        
        // Test only: display full SQL string for testing/confirmation.
        System.out.println("SQL used for competion display: " + sql);
        
        // Create new instance for dbRead.
        dbRead = new DB_Read(sql, "competition");
        
        // Test only: display sql error if any exists (if it does, get out of the method)
        if (dbRead.getErrorMessage().isEmpty() == false)
        {
            System.out.println("Error" + dbRead.getErrorMessage());
            return;
        }
        
        // Test only: display the number of rows that result from an SQl execution.
        System.out.println("Number of comp results from SQL: " + dbRead.getRecordCount());
        
        // Test only: display row data.
        /*
        if (dbRead.getRecordCount() > 0)
        {
            for (int row = 0; row < dbRead.getObjDataSet().length; row++)
            {
                for (int col = 0; col < 5; col++)
                {
                    System.out.println(dbRead.getObjDataSet()[row][col] + " - ");
                }
                System.out.println();
            }
        }
        */
        
        // Check if there are rows that result form the SQL execution for Competitions.
        if (dbRead.getRecordCount() > 0)
        {
            // Remove all existing rows from compResults_JTable
            if (compResultsTableModel.getRowCount() > 0)
            {
                for (int i = compResultsTableModel.getRowCount() - 1; i > -1; i --)
                {
                    compResultsTableModel.removeRow(i);
                }
            }
            
            // Populate the rows of the competition resulst table.
            if (dbRead.getObjDataSet() != null)
            {
                // Add data to tableModel
                for (int row = 0; row < dbRead.getObjDataSet().length; row++)
                {
                    compResultsTableModel.addRow(dbRead.getObjDataSet()[row]);
                }
                
                // Update
                compResultsTableModel.fireTableDataChanged();
            }
        }
        
        else
        {
            // At this stage, there are no rows resulting for the SQl execution
            // Remove all existing rows from compResuls_JTable.
            if (compResultsTableModel.getRowCount() > 0)
            {
                for (int i = compResultsTableModel.getRowCount() - 1; i> -1; i--)
                {
                    compResultsTableModel.removeRow(i);
                }
            }
        }
        
        // Display number of records found.
        nbrRecordsFound_JTextField.setText(dbRead.getRecordCount() + " records found.");
    }
    
    /*
        Method name: displayEventsLeaderboard()
        Purpose: Displays competition results for all events.
        Inputs: N/A
        Output: N/A
        Note: This method uses a UNION SQL statement to dynamically retrieve tam and point results from the database.
    */
    private void displayEventsLeaderboard()
    {   
        // "All events" selected.
        if (chosenEvent.equals("All events"))
        {
            // SQL query for leaderboard results
            sql = "SELECT name, SUM(totalPoints) AS points "
                + "FROM (SELECT team.name, SUM(team1Points) AS totalPoints "
                + "FROM competition INNER JOIN team ON team.name = competition.team1 "
                + "GROUP BY team.name UNION SELECT team.name, SUM(team2Points) AS totalPoints "
                + "FROM competition INNER JOIN team ON team.name = competition.team2 "
                + "GROUP BY team.name ORDER BY totalPoints DESC) AS derivedTable "
                + "GROUP BY name ORDER BY points DESC;";
        }
        
        // A specific event is chosen.
        else
        {
            sql = "SELECT name, SUM(totalPoints) AS points " 
            + "FROM (SELECT team.name, SUM(team1Points) AS totalPoints "
            + "FROM competition INNER JOIN team ON team.name = competition.team1 "
            + "WHERE eventName = '" + chosenEvent + "' "
            + "GROUP BY team.name UNION SELECT team.name, SUM(team2Points) AS totalPoints "
            + "FROM competition INNER JOIN team ON team.name = competition.team2 "
            + "WHERE eventName = '" + chosenEvent + "' "
            + "GROUP BY team.name ORDER BY totalPoints DESC) AS derivedTable "
            + "GROUP BY name ORDER BY points DESC;";
        }
        dbRead = new DB_Read(sql, "leaderBoard");

        // Always clear the table first
        String[] columnNames = {"Team Name", "Total Points"};
        leaderboardEventsTableModel.setDataVector(new Object[0][2], columnNames);

        // If there is data, populate it
        if (dbRead.getRecordCount() > 0)
        {
            String[] csvData = dbRead.getStringCSVData();
            Object[][] tableData = new Object[csvData.length][2];

            for (int i = 0; i < csvData.length; i++)
            {
                String[] parts = csvData[i].split(",");
                tableData[i][0] = parts[0]; // Team name
                tableData[i][1] = parts[1]; // Total points
            }

            leaderboardEventsTableModel.setDataVector(tableData, columnNames);
        }

        // Force table UI to refresh cleanly
        leaderboard_JTable.revalidate();
        leaderboard_JTable.repaint();
    }

    /*
        Method name: displayEventListing()
        Purpose: Display event results according to JComboBox selections.
        Inputs: N/A
        Output: N/A
        Note: event_JComboBox contains the event name + data/location in brackets, the date is formatted as dd-MM-yyyy.
    */
    private void displayEventListing()
    {
        /*
        1. Get a String Array containing String values for each event, it will get the data from the team table in the SQL database.
        
        2. Remove any existing items from JComboBox controls that contain team details.
        
        3. Add "All Items" first to the JComboBoxes in the Event Comp Results tab panel.
        
        4. Add team details in the team-related JComboBox controls. A for loop will be used for the teamsCSVStrArray String [] Array
        */
        sql = "SELECT name, date, location FROM goldcoast_esports.event ORDER BY location;";

        dbRead = new DB_Read(sql, "event");
        
        // Test only: display SQL error if any are applicable, close the method it this occurs.
        if (dbRead.getErrorMessage().isEmpty() == false)
        {
            System.out.println("ERROR: " + dbRead.getErrorMessage());
            return;
        }
        
        if (dbRead.getRecordCount() > 0)
        {
            // Assign teamsCSVStrArray
            eventsCSVStrArray = dbRead.getStringCSVData();
            
            // Remove any existing items in the JComboBox controls.
            event_JComboBox.removeAllItems();
            addNewCompResultEvent_JComboBox.removeAllItems();
                        
            // Add "All events" to first team_JComboBox.
            event_JComboBox.addItem("All events");
            
            // Add event details in the event-related JComboBox controls using a for loop.
            for (int i = 0; i < eventsCSVStrArray.length; i++) 
            {
                String[] splitEventStr = eventsCSVStrArray[i].split(",");
                String eventName = splitEventStr[0];
                String eventDate = splitEventStr[1];
                String eventLocation = splitEventStr[2];

                // Add event name to the main Event JComboBox (used for filtering comp results)
                event_JComboBox.addItem(eventName + " (" + eventDate + " - " + eventLocation + ")");

                // Add event name to the Add New Comp Result tab panel JComboBox
                addNewCompResultEvent_JComboBox.addItem(eventName);

                // Display event details in the Add New Event tab only for the first record
                if (i == 0) 
                {
                    addNewEventName_JTextField.setText(eventName);
                    addNewEventDate_JTextField.setText(eventDate);
                    addNewEventLocation_JTextField.setText(eventLocation);
                }
            }
        }
    }
    
    /*
        Method name: displayTeamListing()
        Purpose: Displays team results in 4 JComboBox controls.
        Inputs: N/A
        Output: N/A
    */
    private void displayTeamListing()
    {
        /*
        1. Get a String Array containing String values for each team, it will get the data from the team table in the SQL database.
        It should look like: teamsCSVStrArray[0]: "BioHazards","Zheng Lee","0418999888","zhenglee99@geemail.com"
                             teamsCSVStrArray[1]: "Buttercups","Eric Stratton","040765123","eric_stratto@eltaomni.com"
                             teamsCSVStrArray[2]: "Coomera Bombers","James Taylor","0433948765","jamestaylor123@coomail.com"
                             teamsCSVStrArray[3]: "Nerang Necros","Sophie Jamieson","0440888222","sophie_jamieson@geemail.com"
        
        2. Remove any existing items from JComboBox controls that contain team details.
        
        3. Add "All Items" first to the JComboBoxes in the Event Comp Results tab panel.
        
        4. Add team details in the team-related JComboBox controls. A for loop will be used for the teamsCSVStrArray String [] Array
        */
        sql = "SELECT name, contact, phone, email FROM goldcoast_esports.team ORDER BY name;";
        dbRead = new DB_Read(sql, "team");
        
        // Test only: display SQL error if any are applicable, close the method it this occurs.
        if (dbRead.getErrorMessage().isEmpty() == false)
        {
            System.out.println("ERROR: " + dbRead.getErrorMessage());
            return;
        }
        
        if (dbRead.getRecordCount() > 0)
        {
            // Assign teamsCSVStrArray
            teamsCSVStrArray = dbRead.getStringCSVData();
            
            // Remove any existing items in the JComboBox controls.
            team_JComboBox.removeAllItems();
            addNewCompResultTeam1_JComboBox.removeAllItems();
            addNewCompResultTeam2_JComboBox.removeAllItems();
            updateExistingTeam_JComboBox.removeAllItems();
            
            // Add "All Items" to first team_JComboBox.
            team_JComboBox.addItem("All teams");
            
            // Add team details in the team-related JComboBoxes using a for loop.
            for (int i = 0; i < teamsCSVStrArray.length; i++)
            {
                String[] splitTeamStr = teamsCSVStrArray[i].split(",");
                team_JComboBox.addItem(splitTeamStr[0]);
                addNewCompResultTeam1_JComboBox.addItem(splitTeamStr[0]);
                addNewCompResultTeam2_JComboBox.addItem(splitTeamStr[0]);
                updateExistingTeam_JComboBox.addItem(splitTeamStr[0]);
                
                // Add team details to the Update Existing Team tab panel.
                if (i == 0)
                {
                    updateExistingTeamContactName_JTextField.setText(splitTeamStr[1]);
                    updateExistingTeamPhoneNumber_JTextField.setText(splitTeamStr[2]);
                    updateExistingTeamEmailAddress_JTextField.setText(splitTeamStr[3]);
                }
            } // End of for loop.
        }
    }
    
    /*
        Method name: displayGameListing()
        Purpose: Displays games in addNewCompResultGame_JComboBox.
        Inputs: N/A
        Output: N/A
    */
    private void displayGameListing()
    {
        // Build the SQL query to get game data
        sql = "SELECT name, type FROM goldcoast_esports.game ORDER BY name;";

        // Create new dbRead instance with the query and "game" query type
        dbRead = new DB_Read(sql, "game");

        // Check for errors
        if (!dbRead.getErrorMessage().isEmpty())
        {
            System.out.println("ERROR: " + dbRead.getErrorMessage());
            return;
        }

        // If records are found, populate the JComboBox
        if (dbRead.getRecordCount() > 0)
        {
            // Assign to gamesCSVStrArray
            gamesCSVStrArray = dbRead.getStringCSVData();

            // Clear existing items in the JComboBox
            addNewCompResultGame_JComboBox.removeAllItems();

            // Add each game's name to the combo box
            for (String gameCSV : gamesCSVStrArray)
            {
                String[] parts = gameCSV.split(",");
                if (parts.length > 0)
                {
                    addNewCompResultGame_JComboBox.addItem(parts[0]); // Only display the game name
                }
            }
        }
    }
    
    /*
        Method name: displayTeamData()
        Purpose: Displays relevant team details in fields whenever the team option is changed in a comboBox.
        Inputs: N/A
        Output: N/A
    */
    private void displayTeamData()
    {
        // Get the selected team from the combo box
        String selectedTeam = team_JComboBox.getSelectedItem().toString();

        // If the selection is "All teams", clear the fields or handle accordingly
        if (selectedTeam.equals("All teams")) {
            // Optionally, clear the fields
            updateExistingTeamContactName_JTextField.setText("");
            updateExistingTeamPhoneNumber_JTextField.setText("");
            updateExistingTeamEmailAddress_JTextField.setText("");
            return;
        }

        // Query the database for the selected team's details
        sql = "SELECT contact, phone, email FROM goldcoast_esports.team WHERE name = '" + selectedTeam + "';";
        dbRead = new DB_Read(sql, "team");

        if (!dbRead.getErrorMessage().isEmpty()) {
            System.out.println("ERROR: " + dbRead.getErrorMessage());
            return;
        }

        if (dbRead.getRecordCount() > 0) {
            // Assuming the team data is returned as a CSV string (e.g., "John Doe, 1234567890, johndoe@email.com")
            String[] teamDetails = dbRead.getStringCSVData()[0].split(",");
            updateExistingTeamContactName_JTextField.setText(teamDetails[0]);
            updateExistingTeamPhoneNumber_JTextField.setText(teamDetails[1]);
            updateExistingTeamEmailAddress_JTextField.setText(teamDetails[2]);
        }
    }
    
    /*
        Method name: exportCSVData()
        Purpose: Writes CSV formatted strings into external files for summarised leadrboard data.
        Inputs: String [] strArray: Array data of the team name and points.
                String csvExtFile: External file name.
        Output: N/A
    */
    private void exportCSVData(String [] strArray, String csvExtFile)
    {
        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;

        try 
        {
            fileWriter = new FileWriter(csvExtFile);
            bufferedWriter = new BufferedWriter(fileWriter);

            // Write headers (you can customize this later if different tables have different headers)
            bufferedWriter.write("Game,Date,Location,Team,Points");
            bufferedWriter.newLine();

            // Ensure data is valid before exporting
            if (strArray == null || strArray.length == 0) 
            {
                System.out.println("ERROR: No data available to export.");
                return;
            }

            // Write data rows
            for (String row : strArray) 
            {
                bufferedWriter.write(row);
                bufferedWriter.newLine();
            }

            System.out.println(csvExtFile + " successfully exported.");
        } 
        
        catch (IOException e)
        {
            JOptionPane.showMessageDialog(null, "Error saving data to " + csvExtFile + ": " + e.getMessage());
        } 
        
        finally 
        {
            try 
            {
                if (bufferedWriter != null) bufferedWriter.close();
                if (fileWriter != null) fileWriter.close();
            } 
            
            catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Error closing file writer: " + ex.getMessage());
            }
        }
    }
    
    /*
        Method name: validateTeamData()
        Purpose: Checks all fields are used when updating Team data.
        Inputs: Team contact data via JTextFields.
        Output: N/A
    */
    public boolean validateTeamData(String newTeamName, String[] teamsCSVStrArray) 
    {
        boolean status = true;
        String errorMessage = "";

        // Basic input validation
        if (updateExistingTeamContactName_JTextField.getText().trim().isEmpty()) 
        {
            status = false;
            errorMessage += "Contact's name cannot be empty.\n";
        }
        
        if (updateExistingTeamPhoneNumber_JTextField.getText().trim().isEmpty()) 
        {
            status = false;
            errorMessage += "Contact's phone number cannot be empty.\n";
        }
        
        if (updateExistingTeamEmailAddress_JTextField.getText().trim().isEmpty()) 
        {
            status = false;
            errorMessage += "Contact's email cannot be empty.\n";
        }

        // Display error message if validation fails
        if (!status) 
        {
            JOptionPane.showMessageDialog(null, errorMessage, "Validation Error", JOptionPane.ERROR_MESSAGE);
        }

        return status;
    }
    
    private String[] getTableModelAsCSV(DefaultTableModel tableModel) 
    {
        int rowCount = tableModel.getRowCount();
        int columnCount = tableModel.getColumnCount();

        String[] csvData = new String[rowCount];

        // Loop through each row
        for (int i = 0; i < rowCount; i++) 
        {
            StringBuilder row = new StringBuilder();

            // Loop through each column in the row
            for (int j = 0; j < columnCount; j++) 
            {
                row.append(tableModel.getValueAt(i, j));
                if (j < columnCount - 1) 
                {
                    row.append(","); // Separate columns with commas
                }
            }

            csvData[i] = row.toString();
        }

        return csvData;
    }

    /*
        This method is called from within the constructor to initialise the form.
        DO NOT MODIFY!
    */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        header_JPannel = new javax.swing.JPanel();
        img_JLabel = new javax.swing.JLabel();
        body_JPanel = new javax.swing.JPanel();
        body_JTabbedPane = new javax.swing.JTabbedPane();
        eventCompResults_JPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        compResults_JTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        leaderboard_JTable = new javax.swing.JTable();
        exportCompResults_JButton = new javax.swing.JButton();
        exportLeaderboard_JButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        event_JComboBox = new javax.swing.JComboBox<>();
        jLabel2 = new javax.swing.JLabel();
        team_JComboBox = new javax.swing.JComboBox<>();
        nbrRecordsFound_JTextField = new javax.swing.JTextField();
        compResults_JLabel = new javax.swing.JLabel();
        addNewComp_JPanel = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        addNewCompResultGame_JComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        addNewCompResultEvent_JComboBox = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        addNewCompResultTeam2_JComboBox = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        addNewCompResultTeam1_JComboBox = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        addNewCompResult_JButton = new javax.swing.JButton();
        addNewCompResultTeam1Points_JTextField = new javax.swing.JTextField();
        addNewCompResultTeam2Points_JTextField = new javax.swing.JTextField();
        addNewTeam_JPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        addNewTeamName_JTextField = new javax.swing.JTextField();
        addNewTeamContactName_JTextField = new javax.swing.JTextField();
        addNewTeamEmailAddress_JTextField = new javax.swing.JTextField();
        addNewTeamPhoneNumber_JTextField = new javax.swing.JTextField();
        addNewTeam_JButton = new javax.swing.JButton();
        updateTeam_JPanel = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        updateExistingTeam_JComboBox = new javax.swing.JComboBox<>();
        updateExistingTeamContactName_JTextField = new javax.swing.JTextField();
        updateExistingTeamEmailAddress_JTextField = new javax.swing.JTextField();
        updateExistingTeamPhoneNumber_JTextField = new javax.swing.JTextField();
        updateExistingTeam_JButton = new javax.swing.JButton();
        addNewEvent_JPanel = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        addNewEventName_JTextField = new javax.swing.JTextField();
        addNewEventDate_JTextField = new javax.swing.JTextField();
        addNewEventLocation_JTextField = new javax.swing.JTextField();
        addNewEvent_JButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Gold Coast E-Sports");

        img_JLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image_package/goldcoast_esports_v2.jpg"))); // NOI18N

        javax.swing.GroupLayout header_JPannelLayout = new javax.swing.GroupLayout(header_JPannel);
        header_JPannel.setLayout(header_JPannelLayout);
        header_JPannelLayout.setHorizontalGroup(
            header_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(img_JLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        header_JPannelLayout.setVerticalGroup(
            header_JPannelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(img_JLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        body_JPanel.setBackground(new java.awt.Color(255, 255, 255));
        body_JPanel.setPreferredSize(new java.awt.Dimension(800, 484));

        eventCompResults_JPanel.setBackground(new java.awt.Color(255, 255, 255));

        compResults_JTable.setModel(compResultsTableModel);
        jScrollPane1.setViewportView(compResults_JTable);

        leaderboard_JTable.setModel(leaderboardEventsTableModel);
        jScrollPane2.setViewportView(leaderboard_JTable);

        exportCompResults_JButton.setText("Export Competition Results as CSV File");
        exportCompResults_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportCompResults_JButtonActionPerformed(evt);
            }
        });

        exportLeaderboard_JButton.setText("Export Leaderboard as CSV File");
        exportLeaderboard_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportLeaderboard_JButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Event:");

        event_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        event_JComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                event_JComboBoxItemStateChanged(evt);
            }
        });

        jLabel2.setText("Team:");

        team_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        team_JComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                team_JComboBoxItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout eventCompResults_JPanelLayout = new javax.swing.GroupLayout(eventCompResults_JPanel);
        eventCompResults_JPanel.setLayout(eventCompResults_JPanelLayout);
        eventCompResults_JPanelLayout.setHorizontalGroup(
            eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(team_JComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(event_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 523, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(199, 199, 199))
            .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                        .addComponent(compResults_JLabel)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                        .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                                .addComponent(nbrRecordsFound_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(exportCompResults_JButton))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 467, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(eventCompResults_JPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(exportLeaderboard_JButton)))
                        .addGap(12, 12, 12))))
        );
        eventCompResults_JPanelLayout.setVerticalGroup(
            eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, eventCompResults_JPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(event_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(team_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(compResults_JLabel)
                .addGap(11, 11, 11)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 251, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(eventCompResults_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(exportLeaderboard_JButton)
                    .addComponent(exportCompResults_JButton)
                    .addComponent(nbrRecordsFound_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        body_JTabbedPane.addTab("Event Competition Results", eventCompResults_JPanel);

        addNewComp_JPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel3.setText("Event:");

        addNewCompResultGame_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel4.setText("Game");

        addNewCompResultEvent_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel5.setText("Team 1:");

        addNewCompResultTeam2_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel6.setText("Team 2:");

        addNewCompResultTeam1_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel7.setText("Team 1 Points:");

        jLabel8.setText("Team 2 Points:");

        addNewCompResult_JButton.setText("Add New Competition Result");
        addNewCompResult_JButton.setActionCommand("");
        addNewCompResult_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewCompResult_JButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addNewComp_JPanelLayout = new javax.swing.GroupLayout(addNewComp_JPanel);
        addNewComp_JPanel.setLayout(addNewComp_JPanelLayout);
        addNewComp_JPanelLayout.setHorizontalGroup(
            addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addNewComp_JPanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(addNewComp_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(addNewCompResultGame_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addNewComp_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(addNewCompResultEvent_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addNewComp_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewCompResultTeam1_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addNewComp_JPanelLayout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addNewCompResultTeam2_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addNewComp_JPanelLayout.createSequentialGroup()
                            .addComponent(jLabel8)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(addNewCompResultTeam2Points_JTextField))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, addNewComp_JPanelLayout.createSequentialGroup()
                            .addComponent(jLabel7)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(addNewCompResultTeam1Points_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(392, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addNewComp_JPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addNewCompResult_JButton)
                .addGap(14, 14, 14))
        );
        addNewComp_JPanelLayout.setVerticalGroup(
            addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addNewComp_JPanelLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(addNewCompResultEvent_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(addNewCompResultGame_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(addNewCompResultTeam1_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(addNewCompResultTeam2_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(addNewCompResultTeam1Points_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewComp_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(addNewCompResultTeam2Points_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 156, Short.MAX_VALUE)
                .addComponent(addNewCompResult_JButton)
                .addGap(35, 35, 35))
        );

        body_JTabbedPane.addTab("Add New Competition Result", addNewComp_JPanel);

        addNewTeam_JPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setText("New Team Name:");

        jLabel10.setText("Contact Name:");

        jLabel11.setText("Phone Number:");

        jLabel12.setText("Email Address:");

        addNewTeam_JButton.setText("Add New Team");
        addNewTeam_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewTeam_JButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addNewTeam_JPanelLayout = new javax.swing.GroupLayout(addNewTeam_JPanel);
        addNewTeam_JPanel.setLayout(addNewTeam_JPanelLayout);
        addNewTeam_JPanelLayout.setHorizontalGroup(
            addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addNewTeam_JPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addGap(18, 18, 18)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(addNewTeamEmailAddress_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewTeamContactName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewTeamName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewTeamPhoneNumber_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 328, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(327, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addNewTeam_JPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addNewTeam_JButton)
                .addGap(14, 14, 14))
        );
        addNewTeam_JPanelLayout.setVerticalGroup(
            addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addNewTeam_JPanelLayout.createSequentialGroup()
                .addGap(27, 27, 27)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(addNewTeamName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(addNewTeamContactName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addComponent(addNewTeamPhoneNumber_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(addNewTeamEmailAddress_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 246, Short.MAX_VALUE)
                .addComponent(addNewTeam_JButton)
                .addGap(35, 35, 35))
        );

        body_JTabbedPane.addTab("Add New Team", addNewTeam_JPanel);

        updateTeam_JPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel13.setText("Team Name:");

        jLabel14.setText("Contact Name:");

        jLabel15.setText("Phone Number:");

        jLabel16.setText("Email Address:");

        updateExistingTeam_JComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        updateExistingTeam_JComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                updateExistingTeam_JComboBoxItemStateChanged(evt);
            }
        });

        updateExistingTeam_JButton.setText("Update Existing Team");
        updateExistingTeam_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateExistingTeam_JButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout updateTeam_JPanelLayout = new javax.swing.GroupLayout(updateTeam_JPanel);
        updateTeam_JPanel.setLayout(updateTeam_JPanelLayout);
        updateTeam_JPanelLayout.setHorizontalGroup(
            updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateTeam_JPanelLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jLabel14)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(updateExistingTeam_JComboBox, 0, 332, Short.MAX_VALUE)
                    .addComponent(updateExistingTeamContactName_JTextField)
                    .addComponent(updateExistingTeamPhoneNumber_JTextField)
                    .addComponent(updateExistingTeamEmailAddress_JTextField))
                .addContainerGap(337, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, updateTeam_JPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(updateExistingTeam_JButton)
                .addGap(19, 19, 19))
        );
        updateTeam_JPanelLayout.setVerticalGroup(
            updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(updateTeam_JPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(updateExistingTeam_JComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateExistingTeamContactName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel14))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(updateExistingTeamPhoneNumber_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(6, 6, 6)
                .addGroup(updateTeam_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(updateExistingTeamEmailAddress_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 241, Short.MAX_VALUE)
                .addComponent(updateExistingTeam_JButton)
                .addGap(36, 36, 36))
        );

        body_JTabbedPane.addTab("Update Existing Team", updateTeam_JPanel);

        addNewEvent_JPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel17.setText("New Event Name:");

        jLabel18.setText("Date:");

        jLabel19.setText("Location:");

        addNewEvent_JButton.setText("Add New Event");
        addNewEvent_JButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addNewEvent_JButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout addNewEvent_JPanelLayout = new javax.swing.GroupLayout(addNewEvent_JPanel);
        addNewEvent_JPanel.setLayout(addNewEvent_JPanelLayout);
        addNewEvent_JPanelLayout.setHorizontalGroup(
            addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addNewEvent_JPanelLayout.createSequentialGroup()
                .addGap(40, 40, 40)
                .addGroup(addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel17)
                    .addComponent(jLabel18, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.LEADING))
                .addGap(18, 18, 18)
                .addGroup(addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addNewEventName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewEventDate_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addNewEventLocation_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 307, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(336, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, addNewEvent_JPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(addNewEvent_JButton)
                .addGap(17, 17, 17))
        );
        addNewEvent_JPanelLayout.setVerticalGroup(
            addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(addNewEvent_JPanelLayout.createSequentialGroup()
                .addGap(34, 34, 34)
                .addGroup(addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(addNewEventName_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel18)
                    .addComponent(addNewEventDate_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(addNewEvent_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel19)
                    .addComponent(addNewEventLocation_JTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 272, Short.MAX_VALUE)
                .addComponent(addNewEvent_JButton)
                .addGap(30, 30, 30))
        );

        body_JTabbedPane.addTab("Add New Event", addNewEvent_JPanel);

        javax.swing.GroupLayout body_JPanelLayout = new javax.swing.GroupLayout(body_JPanel);
        body_JPanel.setLayout(body_JPanelLayout);
        body_JPanelLayout.setHorizontalGroup(
            body_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, body_JPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(body_JTabbedPane))
        );
        body_JPanelLayout.setVerticalGroup(
            body_JPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, body_JPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(body_JTabbedPane)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(header_JPannel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(body_JPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(header_JPannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(body_JPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void event_JComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_event_JComboBoxItemStateChanged
        // TODO add your handling code here:
        // Only care about selections, not deselections
        if (evt.getStateChange() == ItemEvent.SELECTED) 
        {
            String selectedEvent = event_JComboBox.getSelectedItem().toString();
            if (selectedEvent == null) 
            {
                // Nothing selected → safely exit
                return;
            }

            chosenEvent = selectedEvent;
            
            if (!chosenEvent.equals("All events"))
            {
                chosenEvent = chosenEvent.substring(0, chosenEvent.indexOf(" ("));
            }

            // Call whatever updates you need
            displayCompResults();
            displayEventsLeaderboard();
        }
    }//GEN-LAST:event_event_JComboBoxItemStateChanged

    private void team_JComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_team_JComboBoxItemStateChanged
        // TODO add your handling code here:
        if (comboBoxStatus)
        {
            // Get selected chosen team from JComboBox
            chosenTeam = team_JComboBox.getSelectedItem().toString();
            
            // Get and display the competiton results based on the team JComboBox selection.
            displayCompResults();
        }
    }//GEN-LAST:event_team_JComboBoxItemStateChanged

    private void updateExistingTeam_JComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_updateExistingTeam_JComboBoxItemStateChanged
        if (comboBoxStatus) 
        {
            if (evt.getStateChange() == ItemEvent.SELECTED) 
            {
                String selectedTeamName = (String) updateExistingTeam_JComboBox.getSelectedItem();

                // Convert teamsCSVStrArray from String[] to ArrayList for correct usage
                ArrayList<String> teamsList = new ArrayList<>(Arrays.asList(teamsCSVStrArray));

                for (int i = 0; i < teamsList.size(); i++) 
                {
                    String[] teamData = teamsList.get(i).split(","); // Assuming CSV-style data

                    if (teamData.length >= 4 && teamData[0].equals(selectedTeamName)) 
                    {
                        updateExistingTeamContactName_JTextField.setText(teamData[1]);  // Contact Name
                        updateExistingTeamPhoneNumber_JTextField.setText(teamData[2]);  // Phone Number
                        updateExistingTeamEmailAddress_JTextField.setText(teamData[3]); // Email Address
                        break; // Exit loop after finding a match
                    }
                }
            }
        }
    }//GEN-LAST:event_updateExistingTeam_JComboBoxItemStateChanged

    private void exportCompResults_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportCompResults_JButtonActionPerformed
            // TODO add your handling code here:
            String[] tableData = getTableModelAsCSV(compResultsTableModel);
            exportCSVData(tableData, "compResults.csv");
    }//GEN-LAST:event_exportCompResults_JButtonActionPerformed

    private void exportLeaderboard_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportLeaderboard_JButtonActionPerformed
        // TODO add your handling code here:
        // Get the CSV data from the table model
        String[] csvData = getTableModelAsCSV(leaderboardEventsTableModel);

        // Now export it to CSV file
        exportCSVData(csvData, "leaderboard.csv");
    }//GEN-LAST:event_exportLeaderboard_JButtonActionPerformed

    private void addNewCompResult_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewCompResult_JButtonActionPerformed
        // Get data from JComboBoxes
        String newCompResultEvent = addNewCompResultEvent_JComboBox.getSelectedItem().toString();
        String newCompResultGame = addNewCompResultGame_JComboBox.getSelectedItem().toString();
        String newCompResultTeam1 = addNewCompResultTeam1_JComboBox.getSelectedItem().toString();
        String newCompResultTeam2 = addNewCompResultTeam2_JComboBox.getSelectedItem().toString();
        String newCompResultTeam1Points = addNewCompResultTeam1Points_JTextField.getText();
        String newCompResultTeam2Points = addNewCompResultTeam2Points_JTextField.getText();

        // Validation
        boolean errorStatus = false;
        StringBuilder errorMessage = new StringBuilder("ERROR(S) DETECTED:\n");

        if (newCompResultEvent == null) 
        {
            errorStatus = true;
            errorMessage.append("- Event selection is missing.\n");
        }

        if (newCompResultGame == null) 
        {
            errorStatus = true;
            errorMessage.append("- Game selection is missing.\n");
        }

        if (newCompResultTeam1 == null) 
        {
            errorStatus = true;
            errorMessage.append("- Team 1 selection is missing.\n");
        }

        if (newCompResultTeam2 == null) 
        {
            errorStatus = true;
            errorMessage.append("- Team 2 selection is missing.\n");
        }

        if (newCompResultTeam1Points == null || newCompResultTeam1Points.toString().trim().isEmpty())
        {
            errorStatus = true;
            errorMessage.append("- Team 1 points are missing.\n");
        }

        if (newCompResultTeam2Points == null || newCompResultTeam2Points.toString().trim().isEmpty())
        {
            errorStatus = true;
            errorMessage.append("- Team 2 points are missing.\n");
        }
        
        int points1 = 0;
        int points2 = 0;
        
        try
        {
            points1 = Integer.parseInt(newCompResultTeam1Points);
            points2 = Integer.parseInt(newCompResultTeam2Points);
            if (points1 + points2 != 2)
            {
                errorStatus = true;
                errorMessage.append("- Team 1/2 points must add to 2.\n");
            }
        }
        catch (Exception e)
        {
            errorStatus = true;
            errorMessage.append("- Team 1/2 points must be numeric.\n");
        }
        
        if (newCompResultTeam1.equals(newCompResultTeam2))
        {
            errorStatus = true;
            errorMessage.append("- Team 1/2 cannot be the same names.\n");
        }

        // If errors exist, show error message and return
        if (errorStatus)
        {
            javax.swing.JOptionPane.showMessageDialog(null, errorMessage.toString(), "ERROR(S) DETECTED!", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirmation message
        int yesOrNo = javax.swing.JOptionPane.showConfirmDialog(null, "You are about to add a new competition result. Do you wish to continue?");
        if (yesOrNo == javax.swing.JOptionPane.NO_OPTION)
        {
            System.out.println("Operation cancelled.");
            return;
        }

        // NEW CODE: Get max competitionID first
        String getMaxIDSQL = "SELECT MAX(competitionID) FROM competition";
        DB_Read dbReadMaxID = new DB_Read(getMaxIDSQL, "maxCompID");

        int newCompetitionID = 1;  // default to 1 if table is empty
        if (dbReadMaxID.getRecordCount() > 0) 
        {
            int maxID = dbReadMaxID.getMaxCompID();
            if (maxID > 0) {
                newCompetitionID = maxID + 1;
            }
        }

        // SQL query fix: Now includes competitionID explicitly
        String sql = "INSERT INTO competition (competitionID, eventName, gameName, team1, team2, team1Points, team2Points) VALUES ("
                     + newCompetitionID + ", '" + newCompResultEvent + "', '" + newCompResultGame + "', '" + newCompResultTeam1 + "', '"
                     + newCompResultTeam2 + "', '" + newCompResultTeam1Points + "', '" + newCompResultTeam2Points + "')";

        System.out.println(sql);

        // Execute database write operation
        DB_Write dbWrite = new DB_Write(sql);

        // Error checking after the database write operation
        if (dbWrite.getErrorMessage() == null || dbWrite.getErrorMessage().isEmpty())
        {
            System.out.println("New competition result successfully added to the database!");

            // Update the events array
            ArrayList<String> arrayListCompetition = new ArrayList<>(Arrays.asList(gamesCSVStrArray));
            String newCompResultStr = newCompResultEvent + "," + newCompResultGame + "," + newCompResultTeam1 + ","
                                      + newCompResultTeam2 + "," + newCompResultTeam1Points + "," + newCompResultTeam2Points;
            arrayListCompetition.add(newCompResultStr);
            gamesCSVStrArray = arrayListCompetition.toArray(new String[0]);

            // Display updated leaderboard
            displayEventsLeaderboard();
        }
        else 
        {
            System.out.println(dbWrite.getErrorMessage());
        }
    }//GEN-LAST:event_addNewCompResult_JButtonActionPerformed

    private void addNewTeam_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewTeam_JButtonActionPerformed
        // TODO add your handling code here:
        // Get all data needed.
        String newTeamName = addNewTeamName_JTextField.getText();
        String newContactPerson = addNewTeamContactName_JTextField.getText();
        String newContactPhone = addNewTeamPhoneNumber_JTextField.getText();
        String newContactEmail = addNewTeamEmailAddress_JTextField.getText();
        
        // Check if all the new data is present and accurate, basically validate it.
        boolean errorStatus = false;
        String errorMessage = "ERROR(S) DETECTED:\n";
        
        // Check if newTeamName is empty.
        if (newTeamName.isEmpty())
        {
            errorStatus = true;
            errorMessage += "A unique team name is required!\n";
        }
        
        // If it's not empty check if the newTeamName already exists.
        else
        {
            for (int i = 0; i < teamsCSVStrArray.length; i++)
            {
                String [] splitTeamsStr = teamsCSVStrArray[i].split(",");
                if (newTeamName.equals(splitTeamsStr[0]))
                {
                    errorStatus = true;
                    errorMessage += "This team name already exists, it must be unique!\n";
                    break;
                }
            }
        }
        
        // Check if the newContactPerson is empty.
        if (newContactPerson.isEmpty())
        {
            errorStatus = true;
            errorMessage = "A contact's name is required!\n";
        }
        
        // Check if the newContactPhone is empty.
        if (newContactPhone.isEmpty())
        {
            errorStatus = true;
            errorMessage = "A contact's phone number is required!\n";
        }
        
        // Check if the newContactEmail is empty.
        if (newContactEmail.isEmpty())
        {
            errorStatus = true;
            errorMessage = "A contact's email is required!\n";
        }
        
        // Final error cheeck.
        if (errorStatus == true)
        {
            // Display this message if it is true.
            javax.swing.JOptionPane.showMessageDialog(null, errorMessage, "ERROR(S) DETECTED!", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confrim to proceed after the error check is passed.
        int yesOrNo = javax.swing.JOptionPane.showConfirmDialog(null, "You are about to add " + newTeamName + " as a team. Do you wish to continue?");
        if (yesOrNo == javax.swing.JOptionPane.NO_OPTION)
        {
            // Exit
            System.out.println("Operation cancelled.");
        }
        
        else
        {
            // Continue
            System.out.println("Adding new team " + newTeamName + ".");
            
            sql = "INSERT INTO team (name, contact, phone, email) VALUES ('" + newTeamName + "', '" + newContactPerson + "', '" + newContactPhone + "', '" + newContactEmail + "')";
            System.out.println(sql);
            
            dbWrite = new DB_Write(sql);
            
            // Check for any error messages, if there's none then it was a success.
            if (dbWrite.getErrorMessage() == null || dbWrite.getErrorMessage().isEmpty())
            {
                System.out.println("New Team successfully added to database!");
                
                // Add new team to teamsStrArray String[] array (via interim ArrayList<String>).
                ArrayList<String> arrayListTeams = new ArrayList<String>(Arrays.asList(teamsCSVStrArray));
                String newTeamStr = newTeamName + "," + newContactPerson + "," + newContactPhone + "," + newContactEmail;
                arrayListTeams.add(newTeamStr);
                teamsCSVStrArray = arrayListTeams.toArray(new String[arrayListTeams.size()]);
                
                // Add the new team name to the 4 JComboBoxes.
                team_JComboBox.addItem(newTeamName);
                addNewCompResultTeam1_JComboBox.addItem(newTeamName);
                addNewCompResultTeam2_JComboBox.addItem(newTeamName);
                updateExistingTeam_JComboBox.addItem(newTeamName);
                
                // Display updated leaderboard for event or total.
                displayEventsLeaderboard();
            }
            
            else
            {
                System.out.println(dbWrite.getErrorMessage());
            }
        }
    }//GEN-LAST:event_addNewTeam_JButtonActionPerformed

    private void updateExistingTeam_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateExistingTeam_JButtonActionPerformed
        // Validate input
        String newTeamName = updateExistingTeam_JComboBox.getSelectedItem().toString();

        // Validate the selected team
        if (teamsCSVStrArray.length > 0 && validateTeamData(newTeamName, teamsCSVStrArray))
        {

            // Retrieve updated contact details
            String updateContactPerson = updateExistingTeamContactName_JTextField.getText();
            String updateContactEmail = updateExistingTeamEmailAddress_JTextField.getText();
            String updateContactPhone = updateExistingTeamPhoneNumber_JTextField.getText();

            // Escape single quotes to prevent SQL errors
            updateContactPerson = updateContactPerson.replace("'", "''");
            updateContactEmail = updateContactEmail.replace("'", "''");
            updateContactPhone = updateContactPhone.replace("'", "''");
            newTeamName = newTeamName.replace("'", "''");

            // Create SQL statement
            String sql = "UPDATE team SET contact = '" + updateContactPerson + "', " +
                         "phone = '" + updateContactPhone + "', " +
                         "email = '" + updateContactEmail + "' " +
                         "WHERE name = '" + newTeamName + "'";

            // Confirm with user before executing
            int yesOrNo = javax.swing.JOptionPane.showConfirmDialog(
                null, "You are about to update team: " + newTeamName + "\nDo you wish to continue?",
                "Team Update", javax.swing.JOptionPane.YES_NO_OPTION
            );

            if (yesOrNo == javax.swing.JOptionPane.YES_OPTION) 
            {
                System.out.println("Update proceeding");

                try 
                {
                    DB_Write dbWrite = new DB_Write(sql);
                    System.out.println("Update executed successfully!");
                } 

                catch (Exception e) 
                {
                    System.out.println("Error during update: " + dbRead.getErrorMessage());
                }
                
                String refreshSQL = "SELECT name, contact, phone, email FROM team WHERE name = '" + newTeamName + "'";
                DB_Read refreshRead = new DB_Read(refreshSQL, "team");

                Object[][] data = refreshRead.getObjDataSet();

                if (data != null && data.length > 0 && data[0] != null) 
                {
                    updateExistingTeamContactName_JTextField.setText(data[0][1].toString()); // contact
                    updateExistingTeamPhoneNumber_JTextField.setText(data[0][2].toString()); // phone
                    updateExistingTeamEmailAddress_JTextField.setText(data[0][3].toString()); // email
                    
                    String refreshAllTeamsSQL = "SELECT name, contact, phone, email FROM team";
                    DB_Read refreshAllTeamsRead = new DB_Read(refreshAllTeamsSQL, "team");
                    teamsCSVStrArray = refreshAllTeamsRead.getStringCSVData();

                    updateExistingTeam_JComboBox.removeAllItems();
                    
                    for (String teamCSV : teamsCSVStrArray) 
                    {
                        String teamName = teamCSV.split(",")[0];
                        updateExistingTeam_JComboBox.addItem(teamName);
                    }

                    String unescapedTeamName = newTeamName.replace("''", "'");
                    updateExistingTeam_JComboBox.setSelectedItem(unescapedTeamName);
                }
                
                else 
                {
                    System.out.println("Refresh failed — team not found or no data returned.");
                    System.out.println("Error: " + refreshRead.getErrorMessage());
                }
            }

            else
            {
                System.out.println("Update cancelled");
            }
        }

        else
        {
            javax.swing.JOptionPane.showMessageDialog(
                null, "ERROR: Must select a valid team to update first!", "ERROR found!", 
                javax.swing.JOptionPane.ERROR_MESSAGE
            );
        }
    }//GEN-LAST:event_updateExistingTeam_JButtonActionPerformed

    private void addNewEvent_JButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addNewEvent_JButtonActionPerformed
         // TODO add your handling code here:
        // Get all data needed.
        String newEventName = addNewEventName_JTextField.getText();
        String newEventDate = addNewEventDate_JTextField.getText();
        String newEventLocation = addNewEventLocation_JTextField.getText();
        
        // Check if all the new data is present and accurate, basically validate it.
        boolean errorStatus = false;
        String errorMessage = "ERROR(S) DETECTED:\n";
        
        // Check if newTeamName is empty.
        if (newEventName.isEmpty())
        {
            errorStatus = true;
            errorMessage += "A unique event name is required!\n";
        }
        
        // If it's not empty check if the newTeamName already exists.
        else
        {
            for (int i = 0; i < eventsCSVStrArray.length; i++)
            {
                String [] splitTeamsStr = eventsCSVStrArray[i].split(",");
                if (newEventName.equals(splitTeamsStr[0]))
                {
                    errorStatus = true;
                    errorMessage += "This team name already exists, it must be unique!\n";
                    break;
                }
            }
        }
        
        // Check if the newEventDate is empty.
        if (newEventDate.isEmpty())
        {
            errorStatus = true;
            errorMessage = "An event date is required!\n";
        }
        
        // Check if the newEventLocation is empty.
        if (newEventLocation.isEmpty())
        {
            errorStatus = true;
            errorMessage = "An event location is required!\n";
        }
 
        // Final error cheeck.
        if (errorStatus == true)
        {
            // Display this message if it is true.
            javax.swing.JOptionPane.showMessageDialog(null, errorMessage, "ERROR(S) DETECTED!", javax.swing.JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confrim to proceed after the error check is passed.
        int yesOrNo = javax.swing.JOptionPane.showConfirmDialog(null, "You are about to add " + newEventName + " as an event. Do you wish to continue?");
        if (yesOrNo == javax.swing.JOptionPane.NO_OPTION)
        {
            // Exit
            System.out.println("Operation cancelled.");
        }
        
        else
        {
            // Continue
            System.out.println("Adding new team " + newEventName + ".");
            
            sql = "INSERT INTO event (name, date, location) VALUES ('" + newEventName + "', '" + newEventDate + "', '" + newEventLocation+ "')";
            System.out.println(sql);
            
            dbWrite = new DB_Write(sql);
            
            // Check for any error messages, if there's none then it was a success.
            if (dbWrite.getErrorMessage().isBlank())
            {
                System.out.println("New event successfully added to database!");
                
                // Add new team to teamsStrArray String[] array (via interim ArrayList<String>).
                ArrayList<String> arrayListEvents = new ArrayList<String>(Arrays.asList(eventsCSVStrArray));
                String newEventStr = newEventName + "," + newEventDate + "," + newEventLocation;
                arrayListEvents.add(newEventStr);
                eventsCSVStrArray = arrayListEvents.toArray(new String[arrayListEvents.size()]);
                
                // Add the new team name to the event JComboBoxes.
                event_JComboBox.addItem(newEventName);
                
                // Updating events in the 2 JComboBoxes
                displayEventListing();
            }
            
            else
            {
                System.out.println(dbWrite.getErrorMessage());
            }
        }
    }//GEN-LAST:event_addNewEvent_JButtonActionPerformed

    public static void main(String args[]) 
    {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(GC_EGames_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GC_EGames_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GC_EGames_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GC_EGames_GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() 
        {
            public void run() 
            {
                new GC_EGames_GUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> addNewCompResultEvent_JComboBox;
    private javax.swing.JComboBox<String> addNewCompResultGame_JComboBox;
    private javax.swing.JTextField addNewCompResultTeam1Points_JTextField;
    private javax.swing.JComboBox<String> addNewCompResultTeam1_JComboBox;
    private javax.swing.JTextField addNewCompResultTeam2Points_JTextField;
    private javax.swing.JComboBox<String> addNewCompResultTeam2_JComboBox;
    private javax.swing.JButton addNewCompResult_JButton;
    private javax.swing.JPanel addNewComp_JPanel;
    private javax.swing.JTextField addNewEventDate_JTextField;
    private javax.swing.JTextField addNewEventLocation_JTextField;
    private javax.swing.JTextField addNewEventName_JTextField;
    private javax.swing.JButton addNewEvent_JButton;
    private javax.swing.JPanel addNewEvent_JPanel;
    private javax.swing.JTextField addNewTeamContactName_JTextField;
    private javax.swing.JTextField addNewTeamEmailAddress_JTextField;
    private javax.swing.JTextField addNewTeamName_JTextField;
    private javax.swing.JTextField addNewTeamPhoneNumber_JTextField;
    private javax.swing.JButton addNewTeam_JButton;
    private javax.swing.JPanel addNewTeam_JPanel;
    private javax.swing.JPanel body_JPanel;
    private javax.swing.JTabbedPane body_JTabbedPane;
    private javax.swing.JLabel compResults_JLabel;
    private javax.swing.JTable compResults_JTable;
    private javax.swing.JPanel eventCompResults_JPanel;
    private javax.swing.JComboBox<String> event_JComboBox;
    private javax.swing.JButton exportCompResults_JButton;
    private javax.swing.JButton exportLeaderboard_JButton;
    private javax.swing.JPanel header_JPannel;
    private javax.swing.JLabel img_JLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable leaderboard_JTable;
    private javax.swing.JTextField nbrRecordsFound_JTextField;
    private javax.swing.JComboBox<String> team_JComboBox;
    private javax.swing.JTextField updateExistingTeamContactName_JTextField;
    private javax.swing.JTextField updateExistingTeamEmailAddress_JTextField;
    private javax.swing.JTextField updateExistingTeamPhoneNumber_JTextField;
    private javax.swing.JButton updateExistingTeam_JButton;
    private javax.swing.JComboBox<String> updateExistingTeam_JComboBox;
    private javax.swing.JPanel updateTeam_JPanel;
    // End of variables declaration//GEN-END:variables
}
