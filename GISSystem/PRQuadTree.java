import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/** 
* Represents a bucketed PR Quadtree that stores coordinates of GIS records.
* The default bucket size is assumed to be 4
*/
public class PRQuadTree<T extends Compare2D<? super T>> 
{   
    private static final int BUCKETSIZE = 4;
    
    abstract class prQuadNode 
    {    
    }
    
    //Leaf node class
    class prQuadLeaf extends prQuadNode 
    {
        public ArrayList<CoordinateEntry> Elements;
    }
    
    //Internal node class
    class prQuadInternal extends prQuadNode 
    {
        public prQuadNode NW, SW, SE, NE;
    }
    
    prQuadNode root;
    long xMin, xMax, yMin, yMax;
    
    /**
     * Creates a empty PRQuadTree object
     */
    public PRQuadTree()
    {
    }
    
    /**
     * Initialize world bounds to the specified parameters
     */
    public void setWorld(long xMin, long xMax, long yMin, long yMax) 
    {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }
    
    /**
     * Pre:   elem != null
     * Post:  If elem lies within the tree's region, and elem is not already 
     *        present in the tree, elem has been inserted into the tree.
     *        Return true iff elem is inserted into the tree. 
     */
    public boolean insert(CoordinateEntry elem) 
    {
        // Element to insert is outside boundaries of world
        if (!elem.inBox(xMin, xMax, yMin, yMax))
        {
            return false;
        }
        //Element can be inserted
        else 
        {
            root = insertHelper(root, elem, xMin, xMax, yMin, yMax);
            return true;
        }
    }

    /**
     * Pre:  elem != null
     * Returns reference to an element x within the tree such that elem.equals(x)
     * is true, provided such a matching element occurs within the tree; returns 
     * null otherwise.
     */
    public CoordinateEntry find(CoordinateEntry Elem) 
    {
        return findHelper(root, Elem, xMin, xMax, yMin, yMax);
    }

    /**
     * Pre:  xLo, xHi, yLo and yHi define a rectangular region
     * Returns a collection of (references to) all elements x such that x is in
     * the tree and x lies at coordinates within the defined rectangular region,
     * including the boundary of the region.
     */
    public ArrayList<CoordinateEntry> find(long xLo, long xHi, long yLo, long yHi) 
    {
        ArrayList<CoordinateEntry> bucket = new ArrayList<CoordinateEntry>();
        findHelperBucket(root, bucket, xLo, xHi, yLo, yHi);
        return bucket;
    }
    
    /**
     * Pre: elem can be inserted into the tree
     * Recursively inserts the element in the tree
     * Post: The tree contains one more element containing elem
     */
    @SuppressWarnings("unchecked")
    private prQuadNode insertHelper(
        prQuadNode sRoot, CoordinateEntry elem, double xLow, double xHi, double yLow, double yHi) 
    {   
        //Create leaf (works for empty case as well)
        if (sRoot == null)
        {
            prQuadLeaf leaf = new prQuadLeaf();
            leaf.Elements = new ArrayList<CoordinateEntry>();
            leaf.Elements.add(elem);
            return leaf;
        }
        
        //Current node is a leaf node 
        else if (sRoot.getClass().equals(prQuadLeaf.class))
        {
            prQuadInternal internal = new prQuadInternal();
            
            //Turn current node into a leaf
            prQuadLeaf leaf = (prQuadLeaf)sRoot;
            
            //Bucket is not full
            if (leaf.Elements.size() != BUCKETSIZE)
            {
                //Check if latitude/longitude pair is already in the bucket
                for (int i = 0; i < leaf.Elements.size(); i++)
                {
                    CoordinateEntry leafElem = leaf.Elements.get(i);
                    if (leafElem.equals(elem))
                    {
                        leafElem.addLocation(elem.locations().get(0));
                        return sRoot;
                    }
                }
                
                //Add to bucket
                leaf.Elements.add(elem);
                return sRoot;
            }
            
            //Bucket is full
            else
            {   
                for (int i = 0; i < BUCKETSIZE; i++)
                {
                    CoordinateEntry leafElem = leaf.Elements.get(i);
                    //Determine direction that the new element should be in
                    Direction dir = leafElem.inQuadrant(xLow, xHi, yLow, yHi);
                    
                    //Handle direction
                    if (dir == Direction.NE)
                    {
                        if (internal.NE == null)
                        {
                            prQuadLeaf neLeaf = new prQuadLeaf();
                            neLeaf.Elements = new ArrayList<CoordinateEntry>();
                            neLeaf.Elements.add(leafElem);
                            internal.NE = neLeaf;
                        }
                        else
                        {
                            prQuadLeaf neLeaf = (prQuadLeaf)internal.NE;
                            neLeaf.Elements.add(leafElem);
                        }
                    }
                    else if (dir == Direction.NW)
                    {
                        if (internal.NW == null)
                        {
                            prQuadLeaf nwLeaf = new prQuadLeaf();
                            nwLeaf.Elements = new ArrayList<CoordinateEntry>();
                            nwLeaf.Elements.add(leafElem);
                            internal.NW = nwLeaf;
                        }
                        else
                        {
                            prQuadLeaf nwLeaf = (prQuadLeaf)internal.NW;
                            nwLeaf.Elements.add(leafElem);
                        }
                    }
                    else if (dir == Direction.SW)
                    {
                        if (internal.SW == null)
                        {
                            prQuadLeaf swLeaf = new prQuadLeaf();
                            swLeaf.Elements = new ArrayList<CoordinateEntry>();
                            swLeaf.Elements.add(leafElem);
                            internal.SW = swLeaf;
                        }
                        else
                        {
                            prQuadLeaf swLeaf = (prQuadLeaf)internal.SW;
                            swLeaf.Elements.add(leafElem);
                        }
                    }
                    else if (dir == Direction.SE)
                    {
                        if (internal.SE == null)
                        {
                            prQuadLeaf seLeaf = new prQuadLeaf();
                            seLeaf.Elements = new ArrayList<CoordinateEntry>();
                            seLeaf.Elements.add(leafElem);
                            internal.SE = seLeaf;
                        }
                        else
                        {
                            prQuadLeaf seLeaf = (prQuadLeaf)internal.SE;
                            seLeaf.Elements.add(leafElem);
                        }
                    }
                }
            }
            
            internal = (prQuadInternal)insertHelper(internal, elem, xLow, xHi, yLow, yHi);
            return internal;
        }
        
        //Current node is an internal node
        else
        {
            //Find direction to take
            Direction quadrant = elem.inQuadrant(xLow, xHi, yLow, yHi);
            
            //Take direction
            double xAxis = (yLow + yHi) / 2.0;
            double yAxis = (xLow + xHi) / 2.0;
            
            //Take the specified direction
            prQuadInternal nodeToTake = (prQuadInternal)sRoot;
            if (quadrant == Direction.NE)
            {
                nodeToTake.NE = insertHelper(
                    nodeToTake.NE, elem, yAxis, xHi, xAxis, yHi);
            }
            else if (quadrant == Direction.NW)
            {
                nodeToTake.NW = insertHelper(
                    nodeToTake.NW, elem, xLow, yAxis, xAxis, yHi);              
            }
            else if (quadrant == Direction.SW)
            {
                nodeToTake.SW = insertHelper(
                    nodeToTake.SW, elem, xLow, yAxis, yLow, xAxis);              
            }
            //Direction.SE
            else
            {
                nodeToTake.SE = insertHelper(
                    nodeToTake.SE, elem, yAxis, xHi, yLow, xAxis);               
            }
            return nodeToTake;
        }
    }
    
    /**
     * Pre: elem != null
     * Recursively seraches for the specified element in the tree.
     * Returns the found element or null if the element is not found
     * Post: The tree is unmodified
     */
    @SuppressWarnings("unchecked")
    private CoordinateEntry findHelper(prQuadNode sRoot, CoordinateEntry elem, double xLow, double xHi, double yLow, double yHi)
    {
        
        //Empty tree or node is not in tree
        if (sRoot == null)
        {
            return null;
        }
        
        //Node is a leaf
        else if (sRoot.getClass().equals(prQuadLeaf.class)) 
        {
            prQuadLeaf leaf = (prQuadLeaf)sRoot;
            
            //Element is in the tree
            for (int i = 0; i < leaf.Elements.size(); i++)
            {
                if (leaf.Elements.get(i).equals(elem))
                {
                    return leaf.Elements.get(i);
                }
            }
            //Element is not in the tree
            return null;
        }
        
        //Node is an internal node
        else
        {
            //Find direction to take
            Direction quadrant = elem.inQuadrant(xLow, xHi, yLow, yHi);
            
            //Take direction
            double xAxis = (yLow + yHi) / 2.0;
            double yAxis = (xLow + xHi) / 2.0;
            
            //Take the specified direction
            prQuadInternal nodeToTake = (prQuadInternal)sRoot;
            if (quadrant == Direction.NE)
            {
                return findHelper(
                    nodeToTake.NE, elem, yAxis, xHi, xAxis, yHi);
            }
            else if (quadrant == Direction.NW)
            {
                return findHelper(
                    nodeToTake.NW, elem, xLow, yAxis, xAxis, yHi);              
            }
            else if (quadrant == Direction.SW)
            {
                return findHelper(
                    nodeToTake.SW, elem, xLow, yAxis, yLow, xAxis);              
            }
            else if (quadrant == Direction.SE)
            {
                return findHelper(
                    nodeToTake.SE, elem, yAxis, xHi, yLow, xAxis);               
            }
            else
            {
                return null;
            }
        }
    }
    
    /**
     * Pre: ArrayList is initalized 
     * Recursively seraches for the specified elements in the specified coordinate bounds
     */
    @SuppressWarnings("unchecked")
    private void findHelperBucket(
        prQuadNode sRoot, ArrayList<CoordinateEntry> bucket, double xLo, double xHi, double yLo, double yHi)
    {
        //Add all relevant leaf nodes
        if (sRoot.getClass().equals(prQuadLeaf.class)) 
        {
            prQuadLeaf leaf = (prQuadLeaf)sRoot;
            for (int i = 0; i < leaf.Elements.size(); i++)
            {
                if (leaf.Elements.get(i).inBox(xLo, xHi, yLo, yHi)) 
                {
                    bucket.add(leaf.Elements.get(i));
                }
            }
        }
        //Search internal nodes
        else 
        { 
            prQuadInternal internal = (prQuadInternal)sRoot;
            
            if(internal.NE != null) 
            {
                findHelperBucket(internal.NE, bucket, xLo, xHi, yLo, yHi);
            }
            
            if(internal.NW != null) 
            {
                findHelperBucket(internal.NW, bucket, xLo, xHi, yLo, yHi);
            }
            
            if(internal.SW != null) 
            {
                findHelperBucket(internal.SW, bucket, xLo, xHi, yLo, yHi);
            }
            
            if(internal.SE != null) 
            {
                findHelperBucket(internal.SE, bucket, xLo, xHi, yLo, yHi);
            }
        }
    }
    
    /**
     * Displays the contents of the PR Quadtree in a human-readable format
     */
    public void display(FileWriter Log) throws IOException 
    {       
        if (root == null) 
        {
           Log.write("Tree is empty.\n");
           return;
        }
        displayHelper(root, "", Log);
        Log.write(
            "--------------------------------------------------------------------------------\n");
     }
     
     /**
      * Recursive helper method for display()
      */
     @SuppressWarnings("unchecked")
     public void displayHelper(prQuadNode sRoot, String Padding, FileWriter Out) throws IOException 
     {
           //Check for empty leaf
           if (sRoot == null) 
           {
              Out.write(Padding + "*\n");
              return;
           }
           //Check for and process SW and SE subtrees
           if (sRoot.getClass().getName().equals("PRQuadTree$prQuadInternal")) 
           {
              prQuadInternal p = (prQuadInternal) sRoot;
              displayHelper(p.SW, Padding + "   ", Out);
              displayHelper(p.SE, Padding + "   ", Out);
           }
           //Display indentation padding for current node
           Out.write(Padding);

           //Determine if at leaf or internal and display accordingly
           if (sRoot.getClass().getName().equals("PRQuadTree$prQuadLeaf")) 
           {
              prQuadLeaf p = (prQuadLeaf) sRoot;
              for (int pos = 0; pos < p.Elements.size(); pos++) 
              {
                 Out.write( p.Elements.get(pos) + " " );
              }
              Out.write("\n");
           }
           else
           {
              Out.write( "@\n" );
           }
           
           // Check for and process NE and NW subtrees
           if (sRoot.getClass().getName().equals("PRQuadTree$prQuadInternal")) 
           {
              prQuadInternal p = (prQuadInternal) sRoot;
              displayHelper(p.NE, Padding + "   ", Out);
              displayHelper(p.NW, Padding + "   ", Out);
           }
     }
 }
