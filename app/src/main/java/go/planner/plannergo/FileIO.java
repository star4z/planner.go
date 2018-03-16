package go.planner.plannergo;

import android.content.Context;
import android.util.Log;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Handles file reading and writing and stores the values
 * <p>
 * Created by bdphi on 2/24/2018.
 */

public class FileIO {

    final static ArrayList<Assignment> inProgressAssignments = new ArrayList<>();
    final static ArrayList<Assignment> completedAssignments = new ArrayList<>();

    static ArrayList<String> classNames = new ArrayList<>();

    private static final String ASSIGNMENTS_FILE_NAME = "assignmentsFile";
    private static final String SETTINGS_FILE_NAME = "planner.settings";

    static String[] keys = {"defaultSortIndex", "overdueLast", "timeEnabled", "notificationDate",
            "daysBeforeDueDate", "alarmHour", "alarmMinute"};


    //TODO: enable new read implementation
    static void readAssignmentsFromFile(Context context) {
//        try {
//            readAssignments();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }

        ObjectInputStream inputStream;

        boolean cont = true;
        try {
            File file = new File(context.getFilesDir(), ASSIGNMENTS_FILE_NAME);
            inputStream = new ObjectInputStream(new FileInputStream(file));
            while (cont) {
                Assignment obj = (Assignment) inputStream.readObject();
                if (obj != null)
                    addAssignment(obj);
                else
                    cont = false;
            }
            inputStream.close();
        } catch (EOFException e) {
            Log.v("FileIO", "readAssignmentsFromFile; End of stream reached");
        } catch (ClassNotFoundException e) {
            Log.v("FileIO", "readAssignmentsFromFile; No more objects to be had in the file.");
        } catch (IOException e) {
            Log.v("FileIO", "readAssignmentsFromFile; The file was not to be found.");
            e.printStackTrace();
        }
    }

    static void writeAssignmentsToFile(Context context) {
        //TODO: enable new read write implementation
//        try {
//            writeAssignments();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            File file = new File(context.getFilesDir(), ASSIGNMENTS_FILE_NAME);
            boolean fileCreated = file.createNewFile();
            if (fileCreated)
                Log.v("FileIO", "Assignments file did not exist and was created.");

            FileOutputStream fos = new FileOutputStream(file, false);
            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                ArrayList<Assignment> assignments = new ArrayList<>();
                assignments.addAll(inProgressAssignments);
                assignments.addAll(completedAssignments);
                for (Assignment o : assignments) {
                    try {
                        oos.writeObject(o);
                    } catch (NotSerializableException e) {
                        Log.v("MA", "An object was not serializable, it has not been saved.");
                        e.printStackTrace();
                    }
                }

                oos.close();
                fos.close();
            } catch (IOException e) {
                Log.v("MA", "File did not process");
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            Log.v("MA", "File not found to write");
            e.printStackTrace();
        } catch (IOException e) {
            Log.v("MA", "Could not create file for some reason");
            e.printStackTrace();
        }
    }


    //New implementation?
    public static void writeAssignments(Context context) throws IOException {
        File file = new File(context.getFilesDir(), ASSIGNMENTS_FILE_NAME);
        if (file.createNewFile())
            Log.v("FileIO", "writeAssignments() new file created");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int totalThings = inProgressAssignments.size() + completedAssignments.size();
        oos.writeInt(totalThings);
        for (Assignment assignment : inProgressAssignments) {
            writeAssignment(assignment, oos);
        }
        Log.v("FileIO", "File written");
    }

    private static void writeAssignment(Assignment assignment, ObjectOutputStream oos) throws IOException {
        oos.writeObject(assignment.title);
        oos.writeObject(assignment.className);
        oos.writeObject(assignment.dueDate);
        oos.writeObject(assignment.description);
        oos.writeBoolean(assignment.completed);
        oos.writeObject(assignment.type);
    }

    public static void readAssignments(Context context) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), ASSIGNMENTS_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        for (int i = 0; i < total; i++) {
            addAssignment(readAssignment(ois));
        }
        Log.v("FileIO", "readAssignments()");
    }

    private static Assignment readAssignment(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        return new Assignment(
                (String) ois.readObject(), //title
                (String) ois.readObject(), //className
                (Calendar) ois.readObject(), //dueDate
                (String) ois.readObject(), //description
                ois.readBoolean(), //completed
                (String) ois.readObject() //type
        );
    }

    public static void addAssignment(Assignment assignment) {
        if (assignment.dueDate == null) {
            Log.v("FileIO", "null dateView");
        } else {
            if (assignment.completed)
                completedAssignments.add(assignment);
            else
                inProgressAssignments.add(assignment);
        }
    }

    public static void deleteAssignment(Context context, Assignment assignment) {
        if (assignment.completed)
            FileIO.completedAssignments.remove(assignment);
        else
            FileIO.inProgressAssignments.remove(assignment);
        writeAssignmentsToFile(context);
    }
}
