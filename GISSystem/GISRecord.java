import java.io.FileWriter;
import java.io.IOException;

/**
 * A simple user data type that stores entire GIS records
 */
public class GISRecord 
{
    private String record;
    private long offset;
    
    /**
     * Constructor
     */
    public GISRecord(String rcd, long off)
    {
        record = rcd;
        offset = off;
    }
    
    /**
     * Accessor method for the GIS record
     */
    public String record()
    {
        return record;
    }
    
    /**
     * Accessor method for the offset
     */
    public long offset()
    {
        return offset;
    }
}
