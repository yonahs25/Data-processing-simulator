package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) throws IOException, ParseException {
        Cluster cluster = new Cluster();
        List<Thread> threadsToRun = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<GPU> gpus = new ArrayList<>();
        List<CPU> cpus = new ArrayList<>();
        List<ConfrenceInformation> conferences = new ArrayList<>();
        long tickTime;
        long duration;
        int batchesProcessedByCpus;
        int cpuTimeUsed;
        int gpuTimeUsed;

        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(args[0]);
        Object input = parser.parse(reader);
        JSONObject jsonInput = (JSONObject) input;
        JSONArray studentsArray = (JSONArray) jsonInput.get("Students");

        //------------------------ Students ------------------------------------
        for (int i = 0; i < studentsArray.size(); i++) {

            JSONObject studentObject = (JSONObject) studentsArray.get(i);
            String name = (String) studentObject.get("name");
            String department = (String) studentObject.get("department");
            String status = (String) studentObject.get("status");
            JSONArray modelsArray = (JSONArray) studentObject.get("models");
            Student student = new Student(name, department, status);

            for (int j = 0; j < modelsArray.size(); j++) {
                JSONObject modelObject = (JSONObject) modelsArray.get(j);
                String modelName = (String) modelObject.get("name");
                String  modelType = (String) modelObject.get("type");
                long size = (long) modelObject.get("size");
                Data data;
                Model model;
                switch (modelType){
                    case ("Images"):
                        data = new Data(Data.Type.Images,(int)size);
                        model = new Model(modelName, data, student);
                        student.addModel(model);
                        break;
                    case ("Text"):
                        data = new Data(Data.Type.Text,(int)size);
                        model = new Model(modelName, data, student);
                        student.addModel(model);
                        break;
                    case ("Tabular"):
                        data = new Data(Data.Type.Tabular,(int)size);
                        model = new Model(modelName, data, student);
                        student.addModel(model);
                        break;
                }
            }
            threadsToRun.add( new Thread(new StudentService(student.getName(),student)));
            students.add(student);
        }

        //------------------------ GPU -----------------------
        JSONArray gpusArray = (JSONArray) jsonInput.get("GPUS");
        for(int i = 0;i < gpusArray.size(); i++){
            String gpuType = (String) gpusArray.get(i);
            GPU gpu;
            switch (gpuType){
                case ("RTX3090"):
                    gpu = new GPU(GPU.Type.RTX3090,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    threadsToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;
                case("RTX2080"):
                    gpu = new GPU(GPU.Type.RTX2080,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    threadsToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;
                case("GTX1080"):
                    gpu = new GPU(GPU.Type.GTX1080,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    threadsToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;
            }
        }

        // ---------------------------- CPU ------------------------------
        JSONArray cpusArray = (JSONArray) jsonInput.get("CPUS");
        for(int i = 0; i < cpusArray.size(); i++){
            long cores = (long) cpusArray.get(i);
            CPU cpu = new CPU((int) cores,cluster);
            cpus.add(cpu);
            cluster.registerCpu(cpu);
            threadsToRun.add(new Thread(new CPUService("cpu"+i,cpu)));
        }

        // -------------------------------------- Conference --------------------------------

        JSONArray conferencesArray = (JSONArray) jsonInput.get("Conferences");
        for (int i = 0; i < conferencesArray.size(); i++){
            JSONObject conferenceObject = (JSONObject) conferencesArray.get(i);
            String name = (String) conferenceObject.get("name");
            long date = (long) conferenceObject.get("date");
            ConfrenceInformation conference = new ConfrenceInformation(name,(int)date);
            conferences.add(conference);
            threadsToRun.add(new Thread(new ConferenceService("conference"+i,conference)));
        }
        tickTime = (long)jsonInput.get("TickTime");
        duration = (long)jsonInput.get("Duration");
//        threadsToRun.add(new Thread(new TimeService(tickTime,duration)));
        TimeService timer = new TimeService(tickTime, duration);
        timer.setHowManyToSubscribe(threadsToRun.size());
        threadsToRun.add(new Thread(timer));

        // start all the threads
        for (Thread t: threadsToRun)
        {
            t.start();
        }
        // the main thread should wait for all the threads finish their doing
        for (Thread t: threadsToRun)
        {
            try {
                t.join();
            } catch (InterruptedException e) {}
        }
        //------------------------------------ output--------------------------------------------------------
        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        Writer writer = Files.newBufferedWriter(Paths.get(".//.//.//.//.//.//.//output.json"));
        for(Student s : students)
        {
            s.setTrainedModels();
        }
        cluster.setCpuInfo();
        cluster.setGpuInfo();
        cpuTimeUsed = cluster.getCpuTimeUsed();
        gpuTimeUsed = cluster.getGpuTimeUsed();
        batchesProcessedByCpus = cluster.getBatchesProcessed();
        GsonOutput output = new GsonOutput(students,conferences,cpuTimeUsed,gpuTimeUsed,batchesProcessedByCpus);
        gson.toJson(output,writer);
        writer.close();
    }
}


