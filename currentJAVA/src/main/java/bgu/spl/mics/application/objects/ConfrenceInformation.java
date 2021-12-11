package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    private Vector<Model> goodResults;

    public ConfrenceInformation(String name, int date)
    {
        this.name = name;
        this.date = date;
        goodResults = new Vector<>();
    }
    
    
    public void gotGoodResult(Model e)
    {
        goodResults.add(e);
    }

    public Vector<Model> getGoodResults()
    {
        return goodResults;
    }

    public int getDate()
    {
        return date;
    }
}
