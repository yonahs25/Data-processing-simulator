package bgu.spl.mics.application.objects;

import com.google.gson.annotations.Expose;

import java.util.List;

public class GsonOutput {
    @Expose List<Student> students;
    @Expose List<ConfrenceInformation> confrences;
    @Expose int cpuTimeUsed;
    @Expose int gpuTimeUsed;
    @Expose int batchesProcessed;

    public GsonOutput(List<Student> students, List<ConfrenceInformation> confrences, int cpuTimeUsed, int gpuTimeUsed, int batchesProcessed) {
        this.students = students;
        this.confrences = confrences;
        this.cpuTimeUsed = cpuTimeUsed;
        this.gpuTimeUsed = gpuTimeUsed;
        this.batchesProcessed = batchesProcessed;
    }
}