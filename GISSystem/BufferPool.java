import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A buffer pool that stores 15 entire GIS records and uses LRU replacement
 */
public class BufferPool<T> 
{
    //Represents the actual buffer pool 
    private ArrayList<GISRecord> pool;
    
    private static final int cap = 15;
    
    /**
     * Initialize the buffer pool to hold 15 elements
     */
    public BufferPool()
    {
        pool = new ArrayList<GISRecord>(cap);
    }
    
    /**
     * Determines if the offset matches a record in the buffer pool
     * @param offset The offset to be queried
     * @return the GIS record corresponding to the given offset or null if not in pool
     */
    public String inPool(long offset)
    {
        for (int i = 0; i < pool.size(); i++)
        {
            if (pool.get(i).offset() == offset)
            {
                //Add to MRU
                GISRecord rcd = pool.get(i);
                pool.remove(i);
                pool.add(0, rcd);
                return rcd.record();
            }
        }
        return null;
    }
    
    /**
     * Imports a GIS record into the buffer pool
     * Pre: The GIS record is not already in the buffer pool
     * @param rcd The record to be inserted to the buffer pool
     * @param offset The offset of the record
     */
    public void insertRcd(String rcd, long offset)
    {
        GISRecord gisRcd = new GISRecord(rcd, offset);
        //Pool is full
        if (pool.size() == 15)
        {
            pool.remove(14);
        }
        pool.add(0, gisRcd);
    }
    
    /**
     * Displays the contents of the buffer pool in a human-readable format
     * 
     */
    public void display(FileWriter log) throws IOException
    {
        log.write("MRU\n");
        for (int i = 0; i < pool.size(); i++)
        {
            log.write("    " + String.valueOf(pool.get(i).offset()) + ":\t" + 
                pool.get(i).record() + "\n");
        }
        log.write("LRU\n");
        log.write(
            "--------------------------------------------------------------------------------\n");
    }

}
