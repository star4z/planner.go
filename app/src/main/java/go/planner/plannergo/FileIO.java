package go.planner.plannergo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

    final static ArrayList<NewAssignment> inProgressAssignments = new ArrayList<>();
    final static ArrayList<NewAssignment> completedAssignments = new ArrayList<>();
    final static ArrayList<NewAssignment> deletedAssignments = new ArrayList<>();

    final static ArrayList<String> classNames = new ArrayList<>();
    final static ArrayList<String> types = new ArrayList<>();

    private static final String NEW_ASSIGNMENTS_FILE_NAME = "planner.assignments.all";
    private static final String TYPES_FILE_NAME = "planner.assignments.types";
    private static final String CLASSES_FILE_NAME = "planner.assignments.classes";


    /**
     * Safe wrapper method for readAssignments(Context)
     * This is the proper method to call from other classes.

     * @param context used to access files
     */
    static void readAssignmentsFromFile(Context context) {
        Log.v("FileIO","reading Assignments");
        clearAssignments();
        try {
            readAssignments(context);
            readTypes(context);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Safe wrapper method for writeAssignment(Context)
     * This is the proper method to call from other classes.
     *
     * @param context used to access files
     */
    static void writeAssignmentsToFile(Context context) {
        try {
            writeTypes(context);
            writeAssignments(context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes all assignments to file, plus a fileVersion and a length.
     * Uses writeAssignment(NewAssignment, ObjectOutputStream)
     *
     * @param context used to access files directory
     * @throws IOException caused by file writing problems
     */
    private static void writeAssignments(Context context) throws IOException {
        File file = new File(context.getFilesDir(), NEW_ASSIGNMENTS_FILE_NAME);
        if (file.createNewFile())
            Log.v("FileIO", "writeAssignments() new file created");
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int totalThings = inProgressAssignments.size() + completedAssignments.size();
        oos.writeInt(totalThings);
        double fileVersion = 2;
        oos.writeDouble(fileVersion);
        for (NewAssignment assignment : inProgressAssignments) {
            writeAssignment(assignment, oos);
        }
        for (NewAssignment assignment : completedAssignments) {
            writeAssignment(assignment, oos);
        }

        //App was failing read on the last entered entry,
        // so this code circumvents rather than solves the problem by writing extra data.
        // (totalThings works correctly, so it doesn't even fail internally.)
        if (!inProgressAssignments.isEmpty())
            writeAssignment(inProgressAssignments.get(0), oos);
        Log.v("FileIO", "File written");
    }

    /**
     * Writes an individual assignment to file
     * @param assignment Assignment to write
     * @param oos Stream to write to
     * @throws IOException
     */
    private static void writeAssignment(NewAssignment assignment, ObjectOutputStream oos) throws IOException {
        oos.writeObject(assignment.title);
        oos.writeObject(assignment.className);
        oos.writeObject(assignment.dueDate);
        oos.writeObject(assignment.description);
        oos.writeBoolean(assignment.completed);
        oos.writeObject(assignment.type);
        oos.writeInt(assignment.priority);
        oos.writeLong(assignment.notificationDate1 == null ?
                -1L : assignment.notificationDate1.getTimeInMillis());
        oos.writeLong(assignment.notificationDate2 == null ?
                -1L : assignment.notificationDate2.getTimeInMillis());
        oos.writeLong(assignment.uniqueID);
        Log.v("FileIO", "Wrote: " + assignment);
    }

    private static void readAssignments(Context context) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), NEW_ASSIGNMENTS_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        Log.v("FileIO", "total=" + total);
        double fileVersion = ois.readDouble();
        Log.v("FileIO", "fileVersion=" + fileVersion);
        for (int i = 0; i < total; i++) {
            addAssignment(readAssignment(ois, fileVersion));
        }
        Log.v("FileIO", "readAssignments()");
    }

    /**
     * Reads in the next Assignment from the file in fields.
     *
     * @param ois         Stream from which to read
     * @param fileVersion if version is not current Version, skips some lines and fills them in with
     *                    default values
     * @return NewAssignment from file
     * @throws IOException ois finishes
     * @throws ClassNotFoundException was not able to find object of the given type
     */
    private static NewAssignment readAssignment(ObjectInputStream ois, double fileVersion) throws IOException, ClassNotFoundException {

        String mTitl = (String) ois.readObject(); //title
        String mClass = (String) ois.readObject();//className
        Calendar mDate = (Calendar) ois.readObject();//dueDate
        String mDesc = (String) ois.readObject();
        boolean mCompl = ois.readBoolean();
        String mType = (String) ois.readObject();
        int mPrio = ois.readInt();
        long mNoti = ois.readLong();
        Calendar notification1 = Calendar.getInstance();
        notification1.setTimeInMillis(mNoti);
        long mNot2 = ois.readLong();
        Calendar notification2 = Calendar.getInstance();
        notification2.setTimeInMillis(mNot2);
        long mID = ois.readLong();

        return new NewAssignment(
                mTitl, mClass, mDate, mDesc, mCompl, mType, mPrio, notification1, notification2, mID
        );
    }

    /**
     * The proper way to access an assignment as of v.0.12.
     * Finds the assignment with the correct ID number, and if it exists, returns it.
     * If it does not exist, returns a new instance of NewAssignment.
     *
     * @param uniqueID ID uniquely identifies assignment based on internal
     * @return
     */
    public static NewAssignment getAssignment(long uniqueID) {
        NewAssignment newAssignment = new NewAssignment();
        newAssignment.uniqueID = uniqueID;
        int check = inProgressAssignments.indexOf(newAssignment);
        if (check > -1) {
            return inProgressAssignments.get(check);
        } else {
            check = completedAssignments.indexOf(newAssignment);
            if (check > -1) {
                return completedAssignments.get(check);
            }
        }
        return new NewAssignment();
    }

    public static void addAssignment(Assignment assignment) {
        addAssignment((NewAssignment) assignment);
    }

    public static void addAssignment(NewAssignment assignment) {
        if (assignment.dueDate == null) {
            Log.v("FileIO", "null dateView");
        } else {
            if (assignment.completed)
                completedAssignments.add(assignment);
            else
                inProgressAssignments.add(assignment);
            if (!classNames.contains(assignment.className))
                classNames.add(assignment.className);
            if (!types.contains(assignment.type))
                types.add(assignment.type);
        }
    }


    @Deprecated
    public static void deleteAssignment(Context context, Assignment assignment) {
        if (assignment.completed)
            FileIO.completedAssignments.remove(assignment);
        else
            FileIO.inProgressAssignments.remove(assignment);
        writeAssignmentsToFile(context);
    }


    /**
     * Removes assignment from files and displays a snackbar with an undo option.
     * If the undo option is selected, it restores the item and displays the item details.
     *
     * @param context
     * @param assignment
     */
    public static void deleteAssignment(final Activity context, final NewAssignment assignment) {
        if (assignment.completed)
            FileIO.completedAssignments.remove(assignment);
        else
            FileIO.inProgressAssignments.remove(assignment);
        writeAssignmentsToFile(context);
        final Snackbar snackbar = createSnackBarPopup(context, assignment);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAssignment(assignment);
                writeAssignmentsToFile(context);
                Intent intent = new Intent(context, AssignmentDetailsActivity.class);
                intent.putExtra("uniqueID", assignment.uniqueID);
                context.startActivity(intent);
            }
        });
        snackbar.show();


    }

    private static Snackbar createSnackBarPopup(Activity c, NewAssignment n) {
        String title = (n.title.equals("")) ? "Untitled assignment" : "'" + n.title + "'";
        return Snackbar.make(c.findViewById(R.id.coordinator),
                "Deleted " + title + ".", Snackbar.LENGTH_LONG);
    }


    public static void replaceAssignment(Context context, NewAssignment newAssignment) {
        int check = inProgressAssignments.indexOf(newAssignment);
        if (check > -1) {
            inProgressAssignments.set(check, newAssignment);
        } else {
            check = completedAssignments.indexOf(newAssignment);
            if (check > -1) {
                completedAssignments.set(check, newAssignment);
            }
        }
        writeAssignmentsToFile(context);
    }

    private static void clearAssignments() {
        inProgressAssignments.clear();
        completedAssignments.clear();
    }

    public static void readTypes(Context context) throws IOException, ClassNotFoundException {
        types.clear();
        File file = new File(context.getFilesDir(), TYPES_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        Log.v("FileIO", "total=" + total);
        for (int i = 0; i < total; i++) {
            String next = (String) ois.readObject();
            types.add(next);
        }
        Log.v("FileIO", "readTypes()");
    }

    public static void writeTypes(Context context) throws IOException {
        File file = new File(context.getFilesDir(), TYPES_FILE_NAME);
        if (file.createNewFile()) {
            Log.v("FileIO", "writeTypes() new file created");
            return;
        }
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeInt(types.size());
        for (String type : types) {
            oos.writeObject(type);
        }
//        oos.writeObject(""); //Safety
        Log.v("FileIO", "Types written");
    }

    public static void readClasses(Context context) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), CLASSES_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        Log.v("FileIO", "total=" + total);
        for (int i = 0; i < total; i++) {
            String next = (String) ois.readObject();
            types.add(next);
        }
        Log.v("FileIO", "readClasses()");
    }

}
