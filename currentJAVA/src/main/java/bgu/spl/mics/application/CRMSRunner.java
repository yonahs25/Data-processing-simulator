package bgu.spl.mics.application;

import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
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


    private static FileWriter file;
    public static void main(String[] args) throws IOException, ParseException {
        MessageBusImpl bus = MessageBusImpl.getInstance();
        Cluster cluster = new Cluster();
        List<Thread> aboutToRun = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        List<GPU> gpus = new ArrayList<>();
        List<CPU> cpus = new ArrayList<>();
        List<ConfrenceInformation> confrences = new ArrayList<>();
        long tickTime;
        long duration;
        int batchesProcessedByCpus=0;
        int cpuTimeUsed = 0;
        int gpuTimeUsed = 0;




        JSONParser jsonparser = new JSONParser();
        FileReader reader = new FileReader(".//.//.//.//.//.//.//example_input.json");
        Object obj = jsonparser.parse(reader);
        JSONObject empjsonobg = (JSONObject) obj;
        JSONArray studentsArray = (JSONArray) empjsonobg.get("Students");

        //------------------------ Students ------------------------------------
        for (int i = 0; i < studentsArray.size(); i++) {

            JSONObject jsstudent = (JSONObject) studentsArray.get(i);
            String name = (String) jsstudent.get("name");
            String department = (String) jsstudent.get("department");
            String status = (String) jsstudent.get("status");
            JSONArray modelsArray = (JSONArray) jsstudent.get("models");
            Student student = new Student(name, department, status);

            for (int j = 0; j < modelsArray.size(); j++) {
                JSONObject jsmodel = (JSONObject) modelsArray.get(j);
                String modelname = (String) jsmodel.get("name");
                String  modelType = (String) jsmodel.get("type");
                long size = (long) jsmodel.get("size");
                Data data;
                Model model;
                switch (modelType){
                    case ("Images"):
                        data = new Data(Data.Type.Images,(int)size);
                        model = new Model(modelname, data, student);
                        student.addModel(model);
                        break;
                    case ("Text"):
                        data = new Data(Data.Type.Text,(int)size);
                        model = new Model(modelname, data, student);
                        student.addModel(model);
                        break;
                    case ("Tabular"):
                        data = new Data(Data.Type.Tabular,(int)size);
                        model = new Model(modelname, data, student);
                        student.addModel(model);
                        break;
                }
            }
            aboutToRun.add( new Thread(new StudentService(student.getName(),student)));
            students.add(student);
        }

        //------------------------ GPU -----------------------
        JSONArray gpusArray = (JSONArray) empjsonobg.get("GPUS");
        for(int i = 0;i < gpusArray.size(); i++){
            String gpuType = (String) gpusArray.get(i);
            GPU gpu;
            switch (gpuType){
                case ("RTX3090"):
                    gpu = new GPU(GPU.Type.RTX3090,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    aboutToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;
                case("RTX2080"):
                    gpu = new GPU(GPU.Type.RTX2080,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    aboutToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;
                case("GTX1080"):
                    gpu = new GPU(GPU.Type.GTX1080,cluster);
                    gpus.add(gpu);
                    cluster.registerGpu(gpu);
                    aboutToRun.add(new Thread(new GPUService("gpu"+i,gpu)));
                    break;

            }
        }

        // ---------------------------- CPU ------------------------------
        JSONArray cpusArray = (JSONArray) empjsonobg.get("CPUS");
        for(int i = 0; i < cpusArray.size(); i++){
            long cores = (long) cpusArray.get(i);
            CPU cpu = new CPU((int) cores,cluster);
            cpus.add(cpu);
            cluster.registerCpu(cpu);
            aboutToRun.add(new Thread(new CPUService("cpu"+i,cpu)));
        }

        // -------------------------------------- Confrense ------------------------

        JSONArray confrenceArray = (JSONArray) empjsonobg.get("Conferences");
        for (int i = 0; i < confrenceArray.size(); i++){
            JSONObject jsConfrence = (JSONObject) confrenceArray.get(i);
            String name = (String) jsConfrence.get("name");
            long date = (long) jsConfrence.get("date");
            ConfrenceInformation confrence = new ConfrenceInformation(name,(int)date);
            confrences.add(confrence);
            aboutToRun.add(new Thread(new ConferenceService("conference"+i,confrence)));
        }
        tickTime = (long)empjsonobg.get("TickTime");
        duration = (long)empjsonobg.get("Duration");
        aboutToRun.add(new Thread(new TimeService(tickTime,duration)));



        for (Thread t: aboutToRun){
            t.start();
        }
        for (Thread t: aboutToRun){
            try {
                t.join();
            } catch (InterruptedException e) {}
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();
        Writer writer = Files.newBufferedWriter(Paths.get(".//.//.//.//.//.//.//output.json"));
        for(Student s : students) {
            s.setTrainedModels();
        }
        for (CPU cpu:cpus){
            batchesProcessedByCpus+= cpu.getBatchesProcessed();
            cpuTimeUsed += cpu.getWorkTime();
        }
        for(GPU gpu:gpus){
            gpuTimeUsed += gpu.getWorkTime();
        }
        for (ConfrenceInformation c :confrences) {
            System.out.println(c.getGoodResults().size());
        }
        GsonOutput output = new GsonOutput(students,confrences,cpuTimeUsed,gpuTimeUsed,batchesProcessedByCpus);
        gson.toJson(output,writer);
        writer.close();







//            for (int i = 0; i < students.size(); i++){
//                System.out.println(students.get(i).getName());
//                List<Model> models =(students.get(i).getModels());
//                for(Model model : models){
//                    System.out.println(model.getData().getType());
//                    System.out.println(model.getData().getSize());
//                }
//
//            }
//
//            for(GPU gpu : gpus){
//                System.out.println(gpu.getType());
//            }
//            for(CPU cpu : cpus){
//            System.out.println(cpu.getCores());
//            }
//            for (ConfrenceInformation confrence : confrences){
//                System.out.println(confrence.getDate());
//            }

    }



}


