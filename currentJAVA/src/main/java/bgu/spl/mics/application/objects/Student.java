package bgu.spl.mics.application.objects;
import java.util.LinkedList;
import java.util.List;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    enum Degree {
        MSc, PhD
    }

    private String  name;
    private String department;
    private Degree status;
    private int publications;
    private int papersRead;
    private List<Model> models;
    private int currModel;


    public Student(String name, String department, String status) {
        this.name = name;
        this.department = department;
        switch (status){
            case("MSc"):
                this.status = Degree.MSc;
                break;
            default:
                this.status = Degree.PhD;
        }
        models = new LinkedList<>();
        currModel = 0;
        publications = 0;
        papersRead = 0;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getPublications() {
        return publications;
    }

    public int getPapersRead() {
        return papersRead;
    }

    public int getCurrModel() {
        return currModel;
    }

    public Degree getStatus() {
        return status;
    }

    public void addModel(Model model){
        models.add(model);
    }

    public List<Model> getModels() {
        return models;
    }

    public void setPublications() {
        publications++;
    }

    public void setPapersRead() {
        papersRead++;
    }
}
