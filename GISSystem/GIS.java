//    On my honor:
//   
//    - I have not discussed the Java language code in my program with
//      anyone other than my instructor or the teaching assistants
//      assigned to this course.
//   
//    - I have not used Java language code obtained from another student,
//      or any other unauthorized source, including the Internet, either
//      modified or unmodified. 
//   
//    - If any Java language code or documentation used in my program
//      was obtained from another source, such as a text book or course
//      notes, that has been clearly noted with a proper citation in
//      the comments of my program.
//   
//    - I have not designed this program in such a way as to defeat or
//      interfere with the normal operation of the supplied grading code.
//
//    Connor Mackert
//    conmac22

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * The main controller class for J4 that contains main().
 * 
 * Processes the command-line parameters and builds a database search mechanism.
 */
public class GIS 
{
    private static RandomAccessFile dbFile;
    private static RandomAccessFile cmdFile;
    private static FileWriter logFile;
    private static String dbFileName;
    private static String cmdFileName;
    private static String logFileName;
    private static HashTable<NameEntry> nameIndex;
    private static PRQuadTree<CoordinateEntry> coordinateIndex;
    private static BufferPool<GISRecord> bPool;
    private static int cmdNum;
    
    /**
     * Main function that validates command-line arguments and initializes the
     * system components
     * @param args The command-line arguments 
     *             args[0] = database file name
     *             args[1] = command script file name
     *             args[2] = log file name
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException
    {
        //Validate command-line parameters
        if (args.length != 3)
        {
            System.out.println(
                "Invocation: java GIS [database file name] [script file name] [log file name]");
            return;
        }
        
        //Set global variables (If script file is valid)
        try 
        {
            dbFileName = args[0];
            cmdFileName = args[1];
            logFileName = args[2];
            cmdNum = 1;
            
            dbFile = new RandomAccessFile(dbFileName, "rw");
            cmdFile = new RandomAccessFile(cmdFileName, "r");
            logFile = new FileWriter(logFileName);
            
            //Build system components
            nameIndex = new HashTable<NameEntry>();
            coordinateIndex = new PRQuadTree<CoordinateEntry>();
            bPool = new BufferPool<GISRecord>();
            
            //Process command file 
            process();
        }
        catch(FileNotFoundException e)
        {
            System.err.println("Could not find file: " + args[1]);
        }
    }
    
    /**
     * Process the specified command file and write appropriate output to log
     * @throws IOException 
     */
    public static void process() throws IOException 
    {
        //Stores the current line of the command file
        String cmdLine = cmdFile.readLine();
        
        //Process command file until a quit command is encountered
        while (!cmdLine.contains("quit"))
        {
            //Comment
            if (cmdLine.charAt(0) == ';')
            {
                logFile.write(cmdLine + "\n");
            }
            //"world" command
            else if (cmdLine.contains("world") && !cmdLine.contains("debug"))
            {
                processWorld(cmdLine);
            }
            //"import" command
            else if (cmdLine.contains("import"))
            {
                processImport(cmdLine);
            }
            //"debug" command
            else if (cmdLine.contains("debug"))
            {
                processDebug(cmdLine);
            }
            //"what_is_at
            else if (cmdLine.contains("what_is_at"))
            {
                processWhatIsAt(cmdLine);
            }
            else if (cmdLine.contains("what_is_in"))
            {
                processWhatIsIn(cmdLine);
            }
            //"what_is" command
            else if (cmdLine.contains("what_is"))
            {
                processWhatIs(cmdLine);
            }
            cmdLine = cmdFile.readLine();
        }
        //Echo quit and terminate program
        logFile.write("Command: " + cmdNum + "\tquit\n\nTerminating execution of commands.\n");
        logFile.write(
            "--------------------------------------------------------------------------------\n");
        cmdFile.close();
        dbFile.close();
        logFile.close();
    }
    
    /**
     * Processes a "world" command
     * @param cmd The command line containing the "world" command
     * @throws IOException 
     */
    public static void processWorld(String cmd) throws IOException
    {
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "world" command
        cmdScan.next();
        //Extract latitude/longitude values and convert to total seconds
        String westLong = cmdScan.next();
        int westLongSec = toTotalSeconds(westLong);
        String eastLong = cmdScan.next();
        int eastLongSec = toTotalSeconds(eastLong);
        String southLat = cmdScan.next();
        int southLatSec = toTotalSeconds(southLat);
        String northLat = cmdScan.next();
        int northLatSec = toTotalSeconds(northLat);
        cmdScan.close();
        
        //Set the boundaries for the PR Quad Tree
        coordinateIndex.setWorld(westLongSec, eastLongSec, southLatSec, northLatSec);
        
        //Write output to file
        logFile.write("world\t" + westLong + "\t" + eastLong + "\t" + southLat + "\t" + northLat + "\n\n");
        logFile.write("GIS Program\n\n");
        logFile.write("dbFile:\t" + dbFileName + "\n");
        logFile.write("script:\t" + cmdFileName + "\n");
        logFile.write("log:\t" + logFileName + "\n");
        logFile.write("Quadtree childern are printed in the order SW SE NE NW\n");
        logFile.write(
            "--------------------------------------------------------------------------------\n\n");
        logFile.write(
            "Latitude/longitude values in index entries are shown as signed integers, in total seconds.\n\n");
        logFile.write("World boundaries are set to:\n");
        logFile.write("\t      " + Integer.toString(northLatSec) + "\n");
        logFile.write("   " + Integer.toString(westLongSec) + "\t\t  " + Integer.toString(eastLongSec) + "\n");
        logFile.write("\t      " + Integer.toString(southLatSec) + "\n");
        logFile.write(
            "--------------------------------------------------------------------------------\n");
    }
    
    /**
     * Processes an "import" command
     * @param cmd The command line containing the "import" command
     * @throws IOException
     */
    public static void processImport(String cmd) throws IOException
    {
        //Extract the GIS file name
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "import" command
        cmdScan.next();
        String gisFileName = cmdScan.next();
        cmdScan.close();
        RandomAccessFile gisFile = new RandomAccessFile(gisFileName, "r");
        
        //Echo command
        logFile.write("Command " + cmdNum + ":\timport\t" + gisFileName + "\n\n");
        
        //Ignore header line
        String header = gisFile.readLine();
        
        //Import file into database
        for (int i = header.length(); i < gisFile.length() - 1; i++)
        {
            dbFile.write(gisFile.read());
        }
        dbFile.seek(0);
     
        //Add to indexes
        long offset = 0;
        int numNameAdded = 0;
        int numLocAdded = 0;
        int totalNameLen = 0;
        while (dbFile.getFilePointer() < dbFile.length())
        {
            boolean missingLatLong = false;
            //Extract offset
            offset = dbFile.getFilePointer();
            
            //Add record to database
            String record = dbFile.readLine();
            
            //Extract feature name, state abbreviation, and latitude/longitude 
            Scanner rcdScan = new Scanner(record);
            rcdScan.useDelimiter("\\|");
            rcdScan.next(); //Go past the feature ID
            String featureName = rcdScan.next();
            totalNameLen += featureName.length();
            rcdScan.next(); //Go past the feature class
            String stateAbb = rcdScan.next();
            rcdScan.next(); //Go past the state numeric
            rcdScan.next(); //Go past the county name
            rcdScan.next(); //Go past the county numeric
            String lat = rcdScan.next();
            long latSec = 0;
            if (lat.isEmpty() || lat.equals("Unknown"))
            {
                missingLatLong = true;
            }
            else
            {
                latSec = (long)toTotalSeconds(lat);
            }
            String longitude = rcdScan.next();
            long longSec = 0;
            if (longitude.isEmpty() || longitude.equals("Unknown"))
            {
                missingLatLong = true;
            }
            else
            {
                longSec = (long)toTotalSeconds(longitude);
            }
            rcdScan.close();
            
            //Add to hash table
            NameEntry nameEntry = new NameEntry(featureName, stateAbb, offset);           
            if (nameIndex.insert(nameEntry))
            {
                numNameAdded++;
            }
            
            //Add to quad tree if coordinates are valid
            if (!missingLatLong)
            {
                CoordinateEntry coord = new CoordinateEntry(longSec, latSec, offset);
                if (coordinateIndex.insert(coord))
                {
                    numLocAdded++;
                }
            }
        }
        //Log relevant data
        logFile.write("Imported Features by name: " + numNameAdded + "\n");
        logFile.write("Imported Locations: " + numLocAdded + "\n");
        logFile.write("Average name length: " + totalNameLen / numNameAdded + "\n");
        logFile.write(
            "--------------------------------------------------------------------------------\n");
        gisFile.close();
        cmdNum++;
    }
    
    /**
     * Processes a "debug" command
     * @param cmd The command line containing the "debug" command
     * @throws IOException
     */
    public static void processDebug(String cmd) throws IOException
    {
        //Determine whether we are displaying the quad tree, hash table, or buffer pool
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "debug" command
        cmdScan.next();
        String debug = cmdScan.next();
        cmdScan.close();
        
        if (debug.contains("quad"))
        {
            //Echo command
            logFile.write("Command " + cmdNum + ":\tdebug\tquad\n\n");
            
            coordinateIndex.display(logFile);
        }
        else if (debug.contains("hash"))
        {
            //Echo command
            logFile.write("Command " + cmdNum + ":\tdebug\thash\n\n");
            
            nameIndex.display(logFile);
        }
        else if (debug.contains("pool"))
        {
            //Echo command
            logFile.write("Command " + cmdNum + ":\tdebug\tpool\n\n"); 
            
            bPool.display(logFile);
        }
        cmdNum++;
    }
    
    /**
     * Processes a "what_is_at" command
     * @param cmd The command line containing the "what_is_at" command
     * @throws IOException
     */
    public static void processWhatIsAt(String cmd) throws IOException
    {
        //Extract geographic coordinates
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "what_is_at" command
        cmdScan.next();
        String latitude = cmdScan.next();
        long latSec = (long)toTotalSeconds(latitude);
        String longitude = cmdScan.next();
        long longSec = (long)toTotalSeconds(longitude);
        cmdScan.close();
        
        //Echo command
        logFile.write("Command " + cmdNum + ":\twhat_is_at\t" + latitude + 
            "\t" + longitude + "\n\n");
        
        //Query for GIS entry with the specified coordinates
        long dummyOffset = 0;
        CoordinateEntry coordQuery = new CoordinateEntry(longSec, latSec, dummyOffset);
        CoordinateEntry result = coordinateIndex.find(coordQuery);
        
        //Record(s) is/are found
        if (result != null)
        {
            logFile.write("   The following features were found at: (" + 
                parseLongitude(longitude) + ", " + parseLatitude(latitude) + ")\n");
            ArrayList<Long> offsets = result.locations();
            for (int i = 0; i < offsets.size(); i++)
            {
                String record = "";
                String bPoolRcd = bPool.inPool(offsets.get(i));
                
                //The record is in the buffer pool
                if (bPoolRcd != null)
                {
                    record = bPoolRcd;
                }                
                //The record is not in the pool
                else
                {
                    dbFile.seek(offsets.get(i));
                    record = dbFile.readLine();
                    bPool.insertRcd(record, offsets.get(i));
                }
                //Extract relevant information
                Scanner rcdScan = new Scanner(record);
                rcdScan.useDelimiter("\\|");
                rcdScan.next(); //Go past the featureID
                String featureName = rcdScan.next(); //Go past the feature name
                rcdScan.next(); //Go past the feature class
                String stateAbb = rcdScan.next();
                rcdScan.next(); //Go past the state numeric
                String countyName = rcdScan.next(); //Go past the county name
                rcdScan.close();
                
                //Log relevant information
                logFile.write("\t" + offsets.get(i) + ":\t" + featureName + "\t" + 
                    countyName + "\t" + stateAbb + "\n");
            }
        }
        
        //The queried record does not exist in the index
        else
        {
            logFile.write(
                "Nothing was found at (" + parseLongitude(longitude) + ", (" + parseLatitude(latitude) + ")\n" );
        }
        
        logFile.write(
            "--------------------------------------------------------------------------------\n");
        cmdNum++;
    }
    
    /**
     * Processes a "what_is_in" command
     * @param cmd The command line containing the "what_is_in" command
     * @throws IOException
     */
    public static void processWhatIsIn(String cmd) throws IOException
    {
        //Extract the geographic coordinates and rectangle bounds
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "what_is_in" command
        cmdScan.next();
        String latitude = cmdScan.next();
        long latSec = (long)toTotalSeconds(latitude);
        String longitude = cmdScan.next();
        long longSec = (long)toTotalSeconds(longitude);
        long halfHeight = Long.parseLong(cmdScan.next());
        long halfWidth = Long.parseLong(cmdScan.next());       
        cmdScan.close();
        
        //Echo command
        logFile.write("Command " + cmdNum + ":\twhat_is_in\t" + latitude + 
            "\t" + longitude + "\t" + halfHeight + "\t" + halfWidth + "\n\n");
        
        //Query for GIS entries within the specified bounds
        ArrayList<CoordinateEntry> result = coordinateIndex.find(
            longSec - halfWidth, longSec + halfWidth, latSec - halfHeight, latSec + halfHeight);
        
        //Log output message if there were entries found
        if (!result.isEmpty())
        {
            //Find number of offsets
            int numOffsets = 0;
            for (int i = 0; i < result.size(); i++)
            {
                for (int j = 0; j < result.get(i).locations().size(); j++)
                {
                    numOffsets++;
                }
            }
            logFile.write("   The following " + numOffsets + " features were found in: (" + 
                parseLongitude(longitude) + " +/- " + halfWidth + ", " + parseLatitude(latitude) + 
                " +/- " + halfHeight + ")\n");
        }
        
        //Display relevant information for each found location
        boolean found = false;
        for (int i = 0; i < result.size(); i++)
        {
            if (result != null)
            {
                found = true;
                ArrayList<Long> offsets = result.get(i).locations();
                for (int j = 0; j < offsets.size(); j++)
                {
                    String record = "";
                    String bPoolRcd = bPool.inPool(offsets.get(j));
                    
                    //The record is in the buffer pool
                    if (bPoolRcd != null)
                    {
                        record = bPoolRcd;
                    }                
                    //The record is not in the pool
                    else
                    {
                        dbFile.seek(offsets.get(j));
                        record = dbFile.readLine();
                        bPool.insertRcd(record, offsets.get(j));
                    }
                    //Extract relevant information
                    Scanner rcdScan = new Scanner(record);
                    rcdScan.useDelimiter("\\|");
                    rcdScan.next(); //Go past the featureID
                    String featureName = rcdScan.next(); //Go past the feature name
                    rcdScan.next(); //Go past the feature class
                    String stateAbb = rcdScan.next();
                    rcdScan.next(); //Go past the state numeric
                    rcdScan.next(); //Go past the county name
                    rcdScan.next(); //Go past the county numeric
                    String resultLat = rcdScan.next();
                    String resultLong = rcdScan.next();
                    rcdScan.close();
                    
                    //Log relevant information
                    logFile.write("\t" + offsets.get(j) + ":\t" + featureName + "\t" + stateAbb + 
                        "\t(" + parseLongitude(resultLong) + ", " + parseLatitude(resultLat) + ")\n");
                }
            }
        }
        
        //Nothing was found in the region
        if (!found)
        {
            logFile.write(
                "Nothing was found at (" + parseLongitude(longitude) + " +/- " + halfWidth + 
                ", " + parseLatitude(latitude) + " +/- " + halfHeight + ")\n");
        }
        
        logFile.write(
            "--------------------------------------------------------------------------------\n");
        cmdNum++;
    }
    
    /**
     * Processes a "what_is" command
     * @param cmd The command line containing the "what_is" command
     * @throws IOException
     */
    public static void processWhatIs(String cmd) throws IOException
    {
        //Extract feature name and state abbreviation
        Scanner cmdScan = new Scanner(cmd);
        cmdScan.useDelimiter("\t");
        //Go past the "what_is" command
        cmdScan.next();
        String featureName = cmdScan.next();
        String stateAbb = cmdScan.next();
        cmdScan.close();
        
        //Echo command
        logFile.write("Command " + cmdNum + ":\twhat_is\t" + featureName + "\t" + stateAbb + "\n\n");
        
        long dummyOffset = 0;
        NameEntry query = new NameEntry(featureName, stateAbb, dummyOffset);
        NameEntry result = nameIndex.find(query);
        
        //Record(s) is/are found
        if (result != null)
        {
            ArrayList<Long> offsets = result.locations();
            for (int i = 0; i < offsets.size(); i++)
            {
                String record = "";
                String bPoolRcd = bPool.inPool(offsets.get(i));
                
                //The record is in the buffer pool
                if (bPoolRcd != null)
                {
                    record = bPoolRcd;
                }                
                //The record is not in the pool
                else
                {
                    dbFile.seek(offsets.get(i));
                    record = dbFile.readLine();
                    bPool.insertRcd(record, offsets.get(i));
                }
                
                //Extract relevant information
                Scanner rcdScan = new Scanner(record);
                rcdScan.useDelimiter("\\|");
                rcdScan.next(); //Go past the featureID
                rcdScan.next(); //Go past the feature name
                rcdScan.next(); //Go past the feature class
                rcdScan.next(); //Go past the state alpha
                rcdScan.next(); //Go past the state numeric
                String countyName = rcdScan.next(); //Go past the county name
                rcdScan.next(); //Go past the county numeric
                String latitude = rcdScan.next();
                String longitude = rcdScan.next();
                rcdScan.close();
                
                //Turn latitude and longitude into a more readable form
                String latitudeReadable = parseLatitude(latitude);
                String longitudeReadable = parseLongitude(longitude);
                
                //Log relevant information
                logFile.write("\t" + offsets.get(i) + ":\t" + countyName + "  (" + 
                    longitudeReadable + ", " + latitudeReadable + ")\n");
            }
        }
        
        //Nothing was found in the index
        else
        {
            logFile.write("No records match " + featureName + "and " + stateAbb + "\n");
        }
        
        logFile.write(
            "--------------------------------------------------------------------------------\n");
        cmdNum++;
    }
    
    /**
     * Converts the parameter from a DMS format to a "total seconds" format
     * @param coord The latitude/longitude in DMS format
     * @return The latitude/longitude in a "total seconds" format
     */
    public static int toTotalSeconds(String coord)
    {
        //Latitude
        if (coord.length() == 7)
        {
            String degrees = coord.substring(0, 2);
            if (degrees.charAt(0) == '0') //Trim 0 if necessary
            {
                degrees = String.valueOf(degrees.charAt(1));
            }          
            String minutes = coord.substring(2, 4);
            if (minutes.charAt(0) == '0') //Trim 0 if necessary
            {
                minutes = String.valueOf(minutes.charAt(1));
            }
            String seconds = coord.substring(4, 6);
            if (seconds.charAt(0) == '0') //Trim 0 if necessary
            {
                seconds = String.valueOf(seconds.charAt(1));
            }
            
            int degreesSec = Integer.parseInt(degrees) * 3600;
            int minutesSec = Integer.parseInt(minutes) * 60;
            int secondsSec = Integer.parseInt(seconds);
            int total = degreesSec + minutesSec + secondsSec;
            
            //West is negative
            if (coord.charAt(6) == 'W')
            {
                total *= -1;
            }
            return total;
        }
        
        //Longitude 
        String degrees = coord.substring(0, 3);
        if (degrees.charAt(0) == '0') //Trim 0 if necessary
        {
            degrees = String.valueOf(degrees.charAt(1) + String.valueOf(degrees.charAt(2)));
        }
        else if (degrees.charAt(0) == '0' && degrees.charAt(1) == '0') //Trim 0 if necessary
        {
            degrees = degrees.substring(2);        
        }       
        String minutes = coord.substring(3, 5);
        if (minutes.charAt(0) == '0') //Trim 0 if necessary
        {
            minutes = String.valueOf(minutes.charAt(1));
        }       
        String seconds = coord.substring(5, 7);
        if (seconds.charAt(0) == '0') //Trim 0 if necessary
        {
            seconds = String.valueOf(seconds.charAt(1));
        }
        
        int degreesSec = Integer.parseInt(degrees) * 3600;
        int minutesSec = Integer.parseInt(minutes) * 60;
        int secondsSec = Integer.parseInt(seconds);
        int total = degreesSec + minutesSec + secondsSec;
        
        //South is negative
        if (coord.charAt(7) == 'W')
        {
            total *= -1;
        }
        return total;
    }
    
    // @param lat: The latitude to be made into a more readable format
    // @return The provided latitude in a more human-readable format
    // Pre: The provided latitude is of the form xxxxxxA where x is a digit and A is 'N' or 'S'
    // Post: Returns the provided latitude in a more human-readable format
    public static String parseLatitude(String lat)
    {
        if (lat.equals("Unknown"))
        {
            return "Unknown";
        }
        String degrees = lat.substring(0, 2);
        if (degrees.charAt(0) == '0') //Trim 0 if necessary
        {
            degrees = String.valueOf(degrees.charAt(1));
        }
        
        String minutes = lat.substring(2, 4);
        if (minutes.charAt(0) == '0') //Trim 0 if necessary
        {
            minutes = String.valueOf(minutes.charAt(1));
        }
        
        String seconds = lat.substring(4, 6);
        if (seconds.charAt(0) == '0') //Trim 0 if necessary
        {
            seconds = String.valueOf(seconds.charAt(1));
        }
        
        //The more human-readable string
        String result = degrees + "d " + minutes + "m " + seconds + "s ";
            
        if (lat.charAt(6) == 'N') //North
        {
            result += "North";
        }
        else //South
        {
            result += "South";            
        }
        
        return result;
    }
    
    // @param longitude: The longitude to be made into a more readable format
    // @return The provided longitude in a more human-readable format
    // Pre: The provided longitude is of the form xxxxxxxA where x is a digit and A is 'W' or 'E'
    // Post: Returns the provided longitude in a more human-readable format
    public static String parseLongitude(String longitude)
    {
        if (longitude.equals("Unknown"))
        {
            return "Unknown";
        }
        
        String degrees = longitude.substring(0, 3);
        if (degrees.charAt(0) == '0') //Trim 0 if necessary
        {
            degrees = String.valueOf(degrees.charAt(1) + String.valueOf(degrees.charAt(2)));
        }
        else if (degrees.charAt(0) == '0' && degrees.charAt(1) == '0') //Trim 0 if necessary
        {
            degrees = degrees.substring(2);        
        }
        
        String minutes = longitude.substring(3, 5);
        if (minutes.charAt(0) == '0') //Trim 0 if necessary
        {
            minutes = String.valueOf(minutes.charAt(1));
        }
        
        String seconds = longitude.substring(5, 7);
        if (seconds.charAt(0) == '0') //Trim 0 if necessary
        {
            seconds = String.valueOf(seconds.charAt(1));
        }
        
        //The more human-readable string
        String result = degrees + "d " + minutes + "m " + seconds + "s ";
            
        if (longitude.charAt(7) == 'W') //West
        {
            result += "West";
        }
        else //East
        {
            result += "East";            
        }
        
        return result;
    }
}