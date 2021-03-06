package go.planner.plannergo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Handles file reading and writing and stores the values for other classes to reference and modify.
 * <p>
 * @author bdphi
 * Created by bdphi on 2/24/2018.
 */

public class FileIO {
    
    private static final String TAG = "FileIO";

    final static ArrayList<Assignment> inProgressAssignments = new ArrayList<>();
    final static ArrayList<Assignment> completedAssignments = new ArrayList<>();
    final static ArrayList<Assignment> deletedAssignments = new ArrayList<>();

    final static ArrayList<String> classNames = new ArrayList<>();
    final static ArrayList<String> types = new ArrayList<>();

    private static final String NEW_ASSIGNMENTS_FILE_NAME = "planner.assignments.all";
    private static final String DELETED_ASSIGNMENTS_FILE_NAME = "planner.assignments.deleted";
    private static final String TYPES_FILE_NAME = "planner.assignments.types";
    private static final String CLASSES_FILE_NAME = "planner.assignments.classes";


    /**
     * Safe wrapper method for readAssignments(Context)
     * This is the proper method to call from other classes.
     *
     * @param context used to access files
     */
    static void readFiles(Context context) {
        Log.v(TAG, "Starting read...");
        clearAssignments();
        readClasses(context);
        readTypes(context);
        try {
            readAssignments(context);
        } catch (IOException | ClassNotFoundException e) {
            Log.w(TAG, "caught error " + e);
        }
        readDeletedAssignments(context);
    }

    /**
     * Safe wrapper method for writeAssignment(Context)
     * This is the proper method to call from other classes.
     *
     * @param context used to access files
     */
    static void writeFiles(Context context) {
        writeTypes(context);
        writeClasses(context);

        try {
             writeAssignments(context);

        } catch (IOException e) {
            Log.w(TAG, "caught error " + e);
        }
        writeDeletedAssignments(context);
    }

    /**
     * Writes all assignments to file, plus a fileVersion and a length.
     * Uses writeAssignment(Assignment, ObjectOutputStream)
     *
     * @param context used to access files directory
     * @throws IOException caused by file writing problems
     */
    private static void writeAssignments(Context context) throws IOException {
        File file = new File(context.getFilesDir(), NEW_ASSIGNMENTS_FILE_NAME);
        if (file.createNewFile()) {
            Log.i(TAG, "writeAssignments() new file created");
        }
        Log.v(TAG, "assignments=" + inProgressAssignments + completedAssignments);
        FileOutputStream fos = new FileOutputStream(file);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        int totalThings = inProgressAssignments.size() + completedAssignments.size();
        oos.writeInt(totalThings);
        double fileVersion = 2;
        oos.writeDouble(fileVersion);
        for (Assignment assignment : inProgressAssignments) {
            writeAssignment(assignment, oos);
        }
        for (Assignment assignment : completedAssignments) {
            writeAssignment(assignment, oos);
        }

        //App was failing read on the last entered entry,
        // so this code circumvents rather than solves the problem by writing extra data.
        // (totalThings works correctly, so it doesn't even fail internally.)
        if (!inProgressAssignments.isEmpty()) {
            Log.v(TAG, "Safety write:");
            writeAssignment(inProgressAssignments.get(0), oos);
        }
        oos.close();
        fos.close();
        Log.v(TAG, "File written");
    }

    /**
     * Writes an individual assignment to file
     *
     * @param assignment Assignment to write
     * @param oos        Stream to write to
     * @throws IOException when file error occurs
     */
    private static void writeAssignment(Assignment assignment, ObjectOutputStream oos) throws IOException {
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
        Log.v(TAG, "Wrote: " + assignment);
    }

    private static void readAssignments(Context context) throws IOException, ClassNotFoundException {
        File file = new File(context.getFilesDir(), NEW_ASSIGNMENTS_FILE_NAME);
        FileInputStream fis = new FileInputStream(file);
        ObjectInputStream ois = new ObjectInputStream(fis);

        int total = ois.readInt();
        Log.v(TAG, "total=" + total);
        double fileVersion = ois.readDouble();
        Log.v(TAG, "fileVersion=" + fileVersion);
        for (int i = 0; i < total; i++) {
            Assignment a = readAssignment(ois, fileVersion);
            addAssignment(a);
            Log.v(TAG, a.toString());
        }
        ois.close();
        fis.close();
        Log.v(TAG, "readAssignments()");
    }

    /**
     * Reads in the next Assignment from the file in fields.
     *
     * @param ois         Stream from which to read
     * @param fileVersion if version is not current Version, skips some lines and fills them in with
     *                    default values
     * @return Assignment from file
     * @throws IOException            ois finishes
     * @throws ClassNotFoundException was not able to find object of the given type
     */
    @SuppressWarnings("unused")
    private static Assignment readAssignment(ObjectInputStream ois, double fileVersion) throws IOException, ClassNotFoundException {

        String mTitle = (String) ois.readObject(); //title
        String mClass = (String) ois.readObject();//className
        Calendar mDate = (Calendar) ois.readObject();//dueDate
        String mDesc = (String) ois.readObject();
        boolean mCompleted = ois.readBoolean();
        String mType = (String) ois.readObject();
        int mPriority = ois.readInt();
        long mNotificationTime = ois.readLong();
        Calendar notification1 = Calendar.getInstance();
        notification1.setTimeInMillis(mNotificationTime);
        long mNot2 = ois.readLong();
        Calendar notification2 = Calendar.getInstance();
        notification2.setTimeInMillis(mNot2);
        long mID = ois.readLong();

        return new Assignment(
                mTitle, mClass, mDate, mDesc, mCompleted, mType, mPriority, notification1, notification2, mID
        );
    }

    /**
     * The proper way to access an assignment as of v.0.12.
     * Finds the assignment with the correct ID number, and if it exists, returns it.
     * If it does not exist, returns a new instance of Assignment.
     *
     * @param uniqueID ID uniquely identifies assignment based on internal
     * @return Assignment with the given uniqueID
     */
    public static Assignment getAssignment(long uniqueID) {
        Assignment assignment = new Assignment();
        assignment.uniqueID = uniqueID;
        int check = inProgressAssignments.indexOf(assignment);
        if (check > -1) {
            return inProgressAssignments.get(check);
        } else {
            check = completedAssignments.indexOf(assignment);
            if (check > -1) {
                return completedAssignments.get(check);
            }
        }
        return new Assignment();
    }

    /**
     * Adds a new assignment to the appropriate list based on whether the assignment is marked
     * completed.
     * It is necessary to call writeFiles() after calling this method; the call is not
     * included in this method so that multiple add calls may be made before each write.
     *
     * @param assignment Assignment to be added
     */
    public static void addAssignment(Assignment assignment) {
        Log.v(TAG, "addAssignment " + assignment);
        if (assignment.dueDate == null) {
            Log.v(TAG, "null dateView");
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

    /**
     * Removes assignment from files and displays a snackBar with an undo option.
     * If the undo option is selected, it restores the item and displays the item details.
     *
     * @param context    Used to create SnackBar pop-up.
     * @param assignment Assignment to delete.
     */
    static void deleteAssignment(final Activity context, final Assignment assignment) {
        if (assignment.completed) {
            FileIO.completedAssignments.remove(assignment);
        } else {
            FileIO.inProgressAssignments.remove(assignment);
        }

        /*
         * Copy of assignment is added to "trash". The copy has a different "unique ID" because
         * the uniqueID is interpreted as a Date in the context of the trash. This is used to
         * calculate when the assignment should be permanently deleted. If the action is undone,
         * the copy ("alt" for "alternate") is removed from the trash, and the original with the
         * original unique ID is added back to list from whence it came.
         */
        final Assignment alt = new Assignment(assignment);
        alt.uniqueID = System.currentTimeMillis();
        deletedAssignments.add(alt);
        writeFiles(context);
        final Snackbar snackbar = createSnackBarPopup(context, assignment);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAssignment(assignment);
                deletedAssignments.remove(alt);
                writeFiles(context);
                if (context instanceof MainActivity) {
                    ((MainActivity) context).loadPanels((assignment.completed)
                            ? completedAssignments : inProgressAssignments);
                }
            }
        });
        snackbar.show();
    }

    /**
     * Does the muscle work for the "Clear all" button in MainActivity.
     * Since this is much faster than many calls to deleteAssignment(),
     * if the uniqueIDs were all calculated using Calendar.getInstance(), many assignments would
     * produce identical IDs. Therefore, one ID is calculated this way, and the others are
     * calculated by merely adding 1 to the number.
     *
     * @param assignments List of assignments to delete.
     * @param c           Context, used to create SnackBar pop-up;
     */
    static void deleteAll(ArrayList<Assignment> assignments, final Activity c) {
        Log.v(TAG, "IPA=" + inProgressAssignments);
        if (assignments.isEmpty()) return;
        long id = System.currentTimeMillis();
        if (assignments.get(0).completed) {
            for (Assignment a : completedAssignments) {
                a.uniqueID = id;
                id++;
                deletedAssignments.add(a);
            }
            completedAssignments.clear();
        } else {
            for (Assignment a : inProgressAssignments) {
                a.uniqueID = id;
                id++;
                deletedAssignments.add(a);
            }
            inProgressAssignments.clear();
        }
        writeFiles(c);

        Snackbar.make(c.findViewById(R.id.coordinator),
                R.string.assignments_deleted, Snackbar.LENGTH_LONG).show();
    }

    private static Snackbar createSnackBarPopup(Activity c, Assignment n) {
        String title = (n.title.equals("")) ? c.getString(R.string.untitled_assignment) : "'" + n.title + "'";
        return Snackbar.make(c.findViewById(R.id.coordinator),
                c.getString(R.string.deleted) + " " + title + ".", Snackbar.LENGTH_LONG);
    }

    /**
     * Calls handled by readFiles()
     *
     * @param context for system access
     */
    private static void readDeletedAssignments(Context context) {
        File file = new File(context.getFilesDir(), DELETED_ASSIGNMENTS_FILE_NAME);

        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            int total = ois.readInt();
            double fileVersion = ois.readDouble();
            for (int i = 0; i < total; i++) {
                deletedAssignments.add(readAssignment(ois, fileVersion));
            }
            Log.v(TAG, "readDeletedAssignments()");
        } catch (IOException | ClassNotFoundException e) {
            Log.w(TAG, "caught error " + e);
        }
    }

    /**
     * Calls handled by writeFiles()
     *
     * @param c for system access
     */
    private static void writeDeletedAssignments(Context c) {
        try {
            File file = new File(c.getFilesDir(), DELETED_ASSIGNMENTS_FILE_NAME);
            if (file.createNewFile())
                Log.v(TAG, "writeDeletedAssignments() new file created");
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(deletedAssignments.size());
            double fileVersion = 2;
            oos.writeDouble(fileVersion);
            Calendar today = Calendar.getInstance();
            Calendar aDate = new GregorianCalendar(); //assignment creation date
            for (Assignment assignment : deletedAssignments) {
                aDate.setTimeInMillis(assignment.uniqueID);
                switch (today.get(Calendar.MONTH) - aDate.get(Calendar.MONTH)) {
                    case 0:
                        writeAssignment(assignment, oos);
                        break;
                    case 1:
                        if (today.get(Calendar.DATE) + (aDate.getActualMaximum(Calendar.DATE) - aDate.get(Calendar.DATE)) <= 30)
                            writeAssignment(assignment, oos);
                        break;
                    case 2:
                        int a = today.get(Calendar.DATE) + (aDate.getActualMaximum(Calendar.DATE) - aDate.get(Calendar.DATE));
                        aDate.add(Calendar.MONTH, 1);
                        a += aDate.getActualMaximum(Calendar.DATE);
                        if (a <= 30) {
                            writeAssignment(assignment, oos);
                        }
                }
            }

            //App was failing read on the last entered entry,
            // so this code circumvents rather than solves the problem by writing extra data.
            // (totalThings works correctly, so it doesn't even fail internally.)
            // (Copied from writeAssignments())
            if (!deletedAssignments.isEmpty())
                writeAssignment(deletedAssignments.get(0), oos);
            Log.v(TAG, "File written");
        } catch (IOException e) {
            Log.w(TAG, "caught error " + e);
        }
    }


    public static void replaceAssignment(Context context, Assignment assignment) {
        int indexOf = inProgressAssignments.indexOf(assignment);
        if (indexOf > -1) {
            inProgressAssignments.set(indexOf, assignment);
        } else {
            indexOf = completedAssignments.indexOf(assignment);
            if (indexOf > -1) {
                completedAssignments.set(indexOf, assignment);
            }
        }
        writeFiles(context);
    }

    private static void clearAssignments() {
        inProgressAssignments.clear();
        completedAssignments.clear();
        deletedAssignments.clear();
        types.clear();
        classNames.clear();
    }


    private static void readTypes(Context context) {
        readArrayListFromFile(context, TYPES_FILE_NAME, types);
    }

    private static void writeTypes(Context context) {
        writeBagToFile(context, TYPES_FILE_NAME, types);
    }

    private static void readClasses(Context context) {
        readArrayListFromFile(context, CLASSES_FILE_NAME, classNames);
    }

    private static void writeClasses(Context context) {
        writeBagToFile(context, CLASSES_FILE_NAME, classNames);
    }

    private static void readArrayListFromFile(Context c, String fileName, ArrayList<String> b) {
        File file = new File(c.getFilesDir(), fileName);
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            int total = ois.readInt();
            for (int i = 0; i < total; i++) {
                String next = (String) ois.readObject();
                b.add(next);
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.w(TAG, "caught error " + e);
        }
        Log.v(TAG, "read " + fileName);
    }

    private static void writeBagToFile(Context c, String fileName, ArrayList<String> b) {
        File file = new File(c.getFilesDir(), fileName);
        try {
            if (file.createNewFile()) {
                Log.v(TAG, fileName + " created");
                return;
            }
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeInt(b.size());
            for (String type : b) {
                oos.writeObject(type);
            }
        } catch (IOException e) {
            Log.w(TAG, "caught error " + e);
        }
        Log.v(TAG, fileName + " written:" + b);
    }

}
