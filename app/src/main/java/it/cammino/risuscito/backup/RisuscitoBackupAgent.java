package it.cammino.risuscito.backup;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

/**
 * Created by marcello.battain on 18/03/2015.
 */
public class RisuscitoBackupAgent extends BackupAgent {

    private static final String DATBASE_BACKUP_KEY = "DBCanti";

    @Override
    public void onBackup(ParcelFileDescriptor oldState
            , BackupDataOutput backupDataOutput
            , ParcelFileDescriptor newState) throws IOException {

        // Get the oldState input stream
        FileInputStream instream = new FileInputStream(oldState.getFileDescriptor());
        DataInputStream in = new DataInputStream(instream);

        try {
            // Get the last modified timestamp from the state file and data file
            // Object for intrinsic lock
            final Object sDataLock = new Object();
            synchronized (sDataLock) {
                long stateModified = in.readLong();
                String currentDBPath = "//data//it.cammino.risuscito//databases//DBCanti";
                File currentDB = new File(Environment.getDataDirectory(), currentDBPath);
                long fileModified = currentDB.lastModified();

                if (stateModified != fileModified) {
                    // The file has been modified, so do a backup
                    // Or the time on the device changed, so be safe and do a backup
                    // Create buffer stream and data output stream for our data
//                ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
//                DataOutputStream outWriter = new DataOutputStream(bufStream);

                    byte[] buffer = readBytesFromFile(currentDB);

                    // Write structured data
//                outWriter.write(bytes);
                    // Send the data to the Backup Manager via the BackupDataOutput
//                byte[] buffer = bufStream.toByteArray();
                    int len = buffer.length;
                    backupDataOutput.writeEntityHeader(DATBASE_BACKUP_KEY, len);
                    backupDataOutput.writeEntityData(buffer, len);

                    FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
                    DataOutputStream out = new DataOutputStream(outstream);

                    long modified = currentDB.lastModified();
                    out.writeLong(modified);
                } else
                    // Don't back up because the file hasn't changed
                    newState = oldState;
            }
        } catch (IOException e) {
            // Unable to read state file... be safe and do a backup
            Log.e(getClass().toString(), e.getMessage());
        }

    }

    @Override
    public void onRestore(BackupDataInput backupDataInput
            , int appVersionCode
            , ParcelFileDescriptor newState) throws IOException {

        File newDatabase = null;

        // There should be only one entity, but the safest
        // way to consume it is using a while loop
        while (backupDataInput.readNextHeader()) {
            String key = backupDataInput.getKey();
            int dataSize = backupDataInput.getDataSize();

            // If the key is ours (for saving top score). Note this key was used when
            // we wrote the backup entity header
            if (DATBASE_BACKUP_KEY.equals(key)) {
                final Object sDataLock = new Object();
                synchronized (sDataLock) {
                    // Create an input stream for the BackupDataInput
                    byte[] dataBuf = new byte[dataSize];
                    backupDataInput.readEntityData(dataBuf, 0, dataSize);
//                ByteArrayInputStream baStream = new ByteArrayInputStream(dataBuf);
//                DataInputStream in = new DataInputStream(baStream);

                    // Read the player name and score from the backup data
                    writeBytesToFile(newDatabase, dataBuf);

                    // Record the score on the device (to a file or something)
                    String oldDBPath = "//data//it.cammino.risuscito//databases//DBCanti";
                    File oldDatabase = new File(Environment.getDataDirectory(), oldDBPath);
                    copyFile(newDatabase, oldDatabase);
                }
            } else {
                // We don't know this entity key. Skip it. (Shouldn't happen.)
                backupDataInput.skipEntityData();
            }
        }

        // Finally, write to the state blob (newState) that describes the restored data
        FileOutputStream outstream = new FileOutputStream(newState.getFileDescriptor());
        DataOutputStream out = new DataOutputStream(outstream);
        long modified = newDatabase.lastModified();
        out.writeLong(modified);

    }

    /**
     * Read bytes from a File into a byte[].
     *
     * @param file The File to read.
     * @return A byte[] containing the contents of the File.
     * @throws IOException Thrown if the File is too long to read or couldn't be
     * read fully.
     */
    public static byte[] readBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            throw new IOException("Could not completely read file " + file.getName() + " as it is too long (" + length + " bytes, max supported " + Integer.MAX_VALUE + ")");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }


    /**
     * Writes the specified byte[] to the specified File path.
     *
     * @param theFile File Object representing the path to write to.
     * @param bytes The byte[] of data to write to the File.
     * @throws IOException Thrown if there is problem creating or writing the
     * File.
     */
    public static void writeBytesToFile(File theFile, byte[] bytes) throws IOException {
        BufferedOutputStream bos = null;

        try {
            FileOutputStream fos = new FileOutputStream(theFile);
            bos = new BufferedOutputStream(fos);
            bos.write(bytes);
        }finally {
            if(bos != null) {
                try  {
                    //flush and close the BufferedOutputStream
                    bos.flush();
                    bos.close();
                } catch(Exception e){}
            }
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

}
