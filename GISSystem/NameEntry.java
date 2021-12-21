import java.util.ArrayList;

/** 
* A user data type that stores a feature name and state abbreviation
* as the key and file offsets (locations)
*/
public class NameEntry implements Hashable<NameEntry> 
{
    String key; //The feature name concatenated with the state 
                //abbreviation (Ex: bburg_VA)
    ArrayList<Long> locations; //File offsets of matching records
    
    /** Initialize a new NameEntry object with the given feature name,
     *  and a single file offset.
     */
    public NameEntry(String name, String abb, Long offset) 
    {
        key = name + ":" + abb;
        locations = new ArrayList<Long>();
        locations.add(offset);
    }
    
    /** Return the key.
     */
    public String key() 
    {
        return key;
    }
    
    /** Return list of file offsets.
     */
    public ArrayList<Long> locations() 
    {
        return locations;
    }
    
    /** Append a file offset to the existing list.
     * @param offset The offset to be added
     */
    public boolean addLocation(Long offset) 
    {
        //Do not add a duplicate offset (offsets are unique)
        if (!locations.contains(offset))
        {
            locations.add(offset); 
            return true;
        }
        return false;
    }
    
    /** Donald Knuth hash function for strings. 
     */
    public int Hash() 
    {
        int hashValue = key.length();
        for (int i = 0; i < key.length(); i++) 
        {
            hashValue = ((hashValue << 5) ^ (hashValue >> 27)) ^ key.charAt(i);
        }
        return ( hashValue & 0x0FFFFFFF );
    }
    
    /** Two NameEntry objects are considered equal iff they
     * hold the same key (same feature name and state abbreviation).
     */
    public boolean equals(Object other) 
    {
        if (other == null)
        {
            return false;
        }        
        if (!this.getClass().equals(other.getClass()))
        {
            return false;            
        }
        NameEntry entry = (NameEntry)other;
        return (this.key.equals(entry.key));
    }
    
    /** Return a String representation of the NameEntry object in the
     * format needed for this assignment.
     */
    public String toString() 
    {
        return ( "[" + this.key + ", " + this.locations.toString() + "]" );
    }
}
