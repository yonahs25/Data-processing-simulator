package bgu.spl.mics.application;

import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

/** This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */
public class CRMSRunner {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        try {
            JSONParser jsonparser = new JSONParser();
            FileReader reader = new FileReader("/home/tovbinv/Desktop/spl2/currentJAVA/example_input.json");
            Object obj = jsonparser.parse(reader);
            JSONObject empjsonobg = (JSONObject) obj;
            JSONArray studentsArray = (JSONArray) empjsonobg.get("Students");
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
                    Data.Type type = (Data.Type) jsmodel.get("type");
                    int size = (int) jsmodel.get("size");
                    Data data = new Data(type, size);
                    Model model = new Model(modelname, data, student);
                    student.addModel(model);

                }

            }

        } catch (IOException | ParseException e) {

        }
    }

}
