package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {


    @Expose private String name;
    @Expose private int date;
    @SerializedName("publications")
    @Expose private Vector<Model> goodResults;

    public ConfrenceInformation(String name, int date)
    {
        this.name = name;
        this.date = date;
        goodResults = new Vector<>();
    }

    public void addGoodResult(Model e)
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

    public String getName() {
        return name;
    }
}
