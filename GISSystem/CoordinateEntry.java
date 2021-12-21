import java.util.ArrayList;

/**
 * A user data type that represents a longitude-latitude pair and also
 * contains file offsets
 */
public class CoordinateEntry implements Compare2D<CoordinateEntry> 
{
    private long longitude;
    private long latitude;
    private ArrayList<Long> locations;
    
    /**
     * Default constructor
     */
    public CoordinateEntry() 
    {
        longitude = 0;
        latitude = 0;
        locations = new ArrayList<Long>();
    }
    /**
     * Parameterized constructor
     */
    public CoordinateEntry(long x, long y, long offset) 
    {
        longitude = x;
        latitude = y;
        locations = new ArrayList<Long>();
        locations.add(offset);
    }
    
    // For the following methods, let P designate the Point object on which
    // the method is invoked (e.g., P.getX()).

    /**
     * Reporter methods for the coordinates of P.
     */
    public long getX() 
    {
        return longitude;
    }
    public long getY() 
    {
        return latitude;
    }
    
    /** Return list of file offsets.
     */
    public ArrayList<Long> locations() 
    {
        return locations;
    }
    
    /** Append a file offset to the existing list.
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
    
    /**
     * Determines which quadrant of the region centered at P the point (X, Y),
     * consistent with the relevant diagram in the project specification;
     * returns NODQUADRANT if P and (X, Y) are the same point.
     */
    public Direction directionFrom(long X, long Y) 
    {
        // Along positive X-axis
        if (this.longitude > X && this.latitude == Y) 
        {
            return Direction.NE;
        }
        // Along positive Y-axis
        else if (this.longitude == X && this.latitude > Y) 
        {
            return Direction.NW;
        }
        // Along negative X-axis
        else if (this.longitude < X && this.latitude == Y) 
        {
            return Direction.SW;
        }
        // Along negative Y-axis
        else if (this.longitude == X && this.latitude < Y) 
        {
            return Direction.SE;
        }
        // NE Quadrant
        else if (this.longitude > X && this.latitude > Y) 
        {
            return Direction.NE;
        }
        // NW Quadrant
        else if (this.longitude < X && this.latitude > Y) 
        {
            return Direction.NW;
        }
        // SW Quadrant
        else if (this.longitude < X && this.latitude < Y) 
        {
            return Direction.SW;
        }
        // SE Quadrant
        else if (this.longitude > X && this.latitude < Y) 
        {
            return Direction.SW;
        }
        // P and (X, Y) are the same point
        return Direction.NOQUADRANT; 
    }
    
    /**
     * Determines which quadrant of the specified region P lies in,
     * consistent with the relevent diagram in the project specification;
     * returns NOQUADRANT if P does not lie in the region. 
     */
    public Direction inQuadrant(double xLo, double xHi, double yLo, double yHi) 
    {
        double newYAxis = (xLo + xHi) / 2.0;
        double newXAxis = (yLo + yHi) / 2.0;
        
        if (!inBox(xLo, xHi, yLo, yHi)) 
        {
            return Direction.NOQUADRANT;
        } 
        //NE
        else if (longitude > newYAxis && latitude >= newXAxis) 
        {
            return Direction.NE;
        } 
        //NW
        else if (longitude <= newYAxis && latitude > newXAxis) 
        {
            return Direction.NW;
        } 
        //SW
        else if (longitude < newYAxis && latitude <= newXAxis) 
        {
            return Direction.SW;
        } 
        //SE
        else if (longitude >= newYAxis && latitude < newXAxis) 
        {
            return Direction.SE;
        }

        return Direction.NOQUADRANT;
    }
    
    /**
     * Returns true iff P lies in the specified region.
     */
    public boolean inBox(double xLo, double xHi, double yLo, double yHi) 
    {       
        if (this.longitude >= xLo && this.longitude <= xHi &&
            this.latitude >= yLo && this.latitude <= yHi) 
        {
            return true;
        }
        return false;
    }
    
    /**
     * Returns a String representation of P.
     */
    public String toString() 
    {   
        String returnStr = "[(" + longitude + ", " + latitude + "), ";
        for (int i = 0; i < locations.size(); i++)
        {
            if (i == locations.size() - 1)
            {
                returnStr = returnStr + locations.get(i) + "]";
            }
            else
            {
                returnStr = returnStr + locations.get(i) + ", ";
            }
        }
        return returnStr;
    }
    
    /**
     * Returns true iff P and o specify the same point.
     */
    public boolean equals(Object o) 
    {
        if (o == null) 
        {
            return false;
        }       
        else if (!this.getClass().equals(o.getClass())) 
        {
            return false;
        }
        
        CoordinateEntry p = (CoordinateEntry)o;
        //Two Points are equal if they have the same X and Y coordinates
        return (this.longitude == p.longitude && this.latitude == p.latitude);
    }
}