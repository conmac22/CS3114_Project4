import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

/** 
* Implements a NameEntry chained hash table, using an ArrayList of LinkedLists
* for the physical table.
*
* The ArrayList has a default size of 1024 slots
*
* The size of the ArrayList is doubled when the load factor exceeds the
* load limit (1.0).
*/
public class HashTable<T extends Hashable<T>> 
{
    private ArrayList<LinkedList<NameEntry>> table; //Physical basis for the hash table
    private Integer numElements = 0; //Number of elements in the table
    private Double loadLimit = 1.0; //Table resize trigger
    private Integer defaultTableSize = 256; //Default number of table slots
    
    /** Constructs an empty hash table
     * Post:
     * - table is an ArrayList of size LinkedList objects
     */
    public HashTable()
    {   
        table = new ArrayList<LinkedList<NameEntry>>(defaultTableSize);
        for (int i = 0; i < defaultTableSize; i++)
        {
            LinkedList<NameEntry> emptyList = new LinkedList<NameEntry>();
            table.add(emptyList);
        }
    }
    
    /** Inserts elem at the front of the elem's home slot, unless that
     * slot already contains a matching element (according to the equals()
     * method for the user's data type (NameEntry).
     * Pre:
     * - elem is a valid NameEntry object
     * Post:
     * - elem is inserted unless it is a duplicate
     * - if the resulting load factor exceeds the load limit, the
     * table is rehashed with the size doubled
     * Returns:
     * true iff elem has been inserted
     */
    public boolean insert(NameEntry elem) 
    {
        int slotIdx = elem.Hash() % defaultTableSize;
        LinkedList<NameEntry> slot = table.get(slotIdx);
        
        //If slot is empty, add element to slot
        if (slot.isEmpty())
        {
            slot.add(elem);
            numElements++;
            if ((numElements / defaultTableSize) >= loadLimit)
            {
                rehashTable();
            }
            return true;
        }
        
        //If not empty, walk down the chain and see if a NameEntry already exists in the chain
        for (int chainIdx = 0; chainIdx < slot.size(); chainIdx++)
        {
            //If nameEntry exists, add location if applicable
            if (slot.get(chainIdx).equals(elem))
            {
                if (slot.get(chainIdx).addLocation(elem.locations.get(0)))
                {
                    return true;
                }
                return false;
            }
        }
        
        //If not empty and not in the chain, add to chain
        slot.add(elem);
        numElements++;
        
        //Rehash if load factor is met or exceeded
        if ((numElements / defaultTableSize) >= loadLimit)
        {
            rehashTable();
        }
        return true;    
    }
    
    /** Searches the table for an element that matches elem (according to
     * the equals() method for the user's data type (NameEntry)).
     * Pre:
     * - elem is a valid NameEntry object
     * Returns:
     * reference to the matching element; null if no match is found
     */
    public NameEntry find(NameEntry elem) 
    {
        //Simple linear search
        for (int slotIdx = 0; slotIdx < table.size(); slotIdx++)
        {
            LinkedList<NameEntry> slot = table.get(slotIdx);
            if (!slot.isEmpty())
            {
                for (int chainIdx = 0; chainIdx < slot.size(); chainIdx++)
                {
                    if (slot.get(chainIdx).equals(elem))
                    {
                        return slot.get(chainIdx);
                    }
                }
            }
        }
        //No match
        return null;
    }
    
    
    /** Writes a formatted display of the hash table contents.
    * Pre:
    * - fw is open on an output file
    */
    public void display(FileWriter fw) throws IOException 
    {
        fw.write("Number of elements: " + numElements + "\n");
        fw.write("Number of slots: " + defaultTableSize + "\n");
        fw.write("Maximum elements in a slot: " + getMaxSlotSize() + "\n");
        fw.write("Load limit: " + loadLimit + "\n");
        fw.write("\n");

        fw.write("Slot Contents\n");
        for (int idx = 0; idx < table.size(); idx++) 
        {
            LinkedList<NameEntry> curr = table.get(idx);

            if ( curr != null && !curr.isEmpty() ) 
            {
                fw.write(String.format("%5d: %s\n", idx, curr.toString()));
            }
        }
    }
    
    /** Rehashes the hash table with double the amount of available slots
    * Pre:
    * - The load factor is met or exceeded
    * Post:
    * - None of the nameEntries in the old table are changed
    */
    public void rehashTable()
    {
        defaultTableSize *= 2;
        ArrayList<LinkedList<NameEntry>> newTable = new ArrayList<LinkedList<NameEntry>>(defaultTableSize);
        for (int i = 0; i < defaultTableSize; i++)
        {
            LinkedList<NameEntry> emptyList = new LinkedList<NameEntry>();
            newTable.add(emptyList);
        }
        
        //Iterate through old table and rehash if slot is not empty
        for (int tableIdx = 0; tableIdx < defaultTableSize / 2; tableIdx++)
        {
            LinkedList<NameEntry> oldSlot = table.get(tableIdx);
            if (!oldSlot.isEmpty())
            {
                //Rehash
                for (int chainIdx = 0; chainIdx < oldSlot.size(); chainIdx++)
                {
                    NameEntry oldEntry = oldSlot.get(chainIdx);
                    
                    int newSlotIdx = oldEntry.Hash() % defaultTableSize;
                    LinkedList<NameEntry> newSlot = newTable.get(newSlotIdx);
                    newSlot.add(oldEntry);
                }
            }
        }
        
        //Set table to be the new rehashed table
        table = newTable;
    }
    
    /** 
    * Returns the maximum number of elements in a single slot
    */
    public int getMaxSlotSize()
    {
        int maxSlotSize = 0;
        
        for (int slotIdx = 0; slotIdx < table.size(); slotIdx++)
        {
            LinkedList<NameEntry> slot = table.get(slotIdx);
            if (!slot.isEmpty())
            {
                if (slot.size() > maxSlotSize)
                {
                    maxSlotSize = slot.size();
                }
            }
        }

        return maxSlotSize;
    }
}
