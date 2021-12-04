package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class Model {

    enum Status {PreTrained, Training, Trained, Tested};
    enum Results {none , Good, Bad};

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Results results;

    public Model(String name, Data data, Student student) {
        this.name = name;
        this.data = data;
        this.student = student;
        status = Status.PreTrained;
        results = Results.none;
    }

    public Data getData() {
        return data;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public Status getStatus() {
        return status;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }
}
