package com.dreamappsstore.googledrive_demo;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static com.dreamappsstore.googledrive_demo.MainActivity.folderId;

/**
 * A utility for performing creating folder if not present, get the file, upload the file, download the file and
 * delete the file from google drive
 */
public class GoogleDriveServiceHelper {

    private static final String TAG = "GoogleDriveService";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    private final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
    private final String SHEET_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private final String FOLDER_NAME = "Example_Folder";

    public GoogleDriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }


    /**
     * Check Folder present or not in the user's My Drive.
     */
    public Task<String> isFolderPresent() {
        return Tasks.call(mExecutor, () -> {
            FileList result = mDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false").execute();
            for (File file : result.getFiles()) {
                if (file.getName().equals(FOLDER_NAME))
                    return file.getId();
            }
            return "";
        });
    }

    /**
     * Creates a Folder in the user's My Drive.
     */
    public Task<String> createFolder() {
        return Tasks.call(mExecutor, () -> {
            File metadata = new File()
                    .setParents(Collections.singletonList("root"))
                    .setMimeType(FOLDER_MIME_TYPE)
                    .setName(FOLDER_NAME);

            File googleFolder = mDriveService.files().create(metadata).execute();
            if (googleFolder == null) {
                throw new IOException("Null result when requesting Folder creation.");
            }

            return googleFolder.getId();
        });
    }

    /**
     * Get all the file present in the user's My Drive Folder.
     */
    public Task<ArrayList<GoogleDriveFileHolder>> getFolderFileList() {

        ArrayList<GoogleDriveFileHolder> fileList = new ArrayList<>();

        if (folderId.isEmpty()){
            Log.e(TAG, "getFolderFileList: folder id not present" );
            isFolderPresent().addOnSuccessListener(id -> folderId=id)
                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create file.", exception));
        }

        return Tasks.call(mExecutor, () -> {
            FileList result = mDriveService.files().list()
                    .setQ("mimeType = '" + SHEET_MIME_TYPE + "' and trashed=false and parents = '" + folderId + "' ")
                    .setSpaces("drive")
                    .execute();

            for (int i = 0; i < result.getFiles().size(); i++) {
                GoogleDriveFileHolder googleDriveFileHolder = new GoogleDriveFileHolder();
                googleDriveFileHolder.setId(result.getFiles().get(i).getId());
                googleDriveFileHolder.setName(result.getFiles().get(i).getName());

                fileList.add(googleDriveFileHolder);
            }
            Log.e(TAG, "getFolderFileList: folderFiles: "+fileList );
            return fileList;
        });
    }


    /**
     * Upload the file to the user's My Drive Folder.
     */
    public Task<Boolean> uploadFileToGoogleDrive(String path) {

        if (folderId.isEmpty()){
            Log.e(TAG, "uploadFileToGoogleDrive: folder id not present" );
            isFolderPresent().addOnSuccessListener(id -> folderId=id)
                    .addOnFailureListener(exception -> Log.e(TAG, "Couldn't create file.", exception));
        }

        return Tasks.call(mExecutor, () -> {

            Log.e(TAG, "uploadFileToGoogleDrive: path: "+path );
            java.io.File filePath = new java.io.File(path);

            File fileMetadata = new File();
            fileMetadata.setName(filePath.getName());
            fileMetadata.setParents(Collections.singletonList(folderId));
            fileMetadata.setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", filePath);
            File file = mDriveService.files().create(fileMetadata, mediaContent)
                    .setFields("id")
                    .execute();
            System.out.println("File ID: " + file.getId());

            return false;
        });
    }

    /**
     * Download file from the user's My Drive Folder.
     */
    public Task<Boolean> downloadFile(final java.io.File fileSaveLocation, final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                OutputStream outputStream = new FileOutputStream(fileSaveLocation);
                mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
                return true;
            }
        });
    }

    /**
     * delete file from the user's My Drive Folder.
     */
    public Task<Boolean> deleteFolderFile(final String fileId) {
        return Tasks.call(mExecutor, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                // Retrieve the metadata as a File object.
                if (fileId != null) {
                    mDriveService.files().delete(fileId).execute();
                    return true;
                }
                return false;
            }
        });
    }

}
