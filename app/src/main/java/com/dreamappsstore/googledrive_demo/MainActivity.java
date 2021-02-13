package com.dreamappsstore.googledrive_demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import net.steamcrafted.loadtoast.LoadToast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


//To-Do Task list
//1. Google sign in
//2. Google sign out
//3. Creation of "Example folder" in Google Drive if it does not exist, if exists then listing its content
//4. Deleting the selected file from the "Example folder" (I think radio button could be used here)
//5. Downloading the selected file from the "Example folder" onto the mobile device, into a folder called "Example Download" (I think radio button could be used here)
//        -uploading the selected file from the "Example Download" into the "Example" folder (I think radio button could be used here)
//6. Delete the file from the "Example Download" folder

//generate SHA1 for debug only
//keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int PICK_FILE_REQUEST = 100;

    static GoogleDriveServiceHelper mDriveServiceHelper;
    static String folderId="";

    private Button signInButton;
    private Button createFolderButton;
    private Button folderFilesButton;
    private Button uploadFileButton;
    private Button signOutButton;
    GoogleSignInClient googleSignInClient;
    LoadToast loadToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestForStoragePermission();
        signInButton = findViewById(R.id.id_sign_in);
        createFolderButton = findViewById(R.id.id_create_folder);
        folderFilesButton = findViewById(R.id.id_folder_files);
        uploadFileButton = findViewById(R.id.id_upload_file);
        signOutButton = findViewById(R.id.id_sign_out);

        loadToast = new LoadToast(this);
    }

    // Read/Write permission
    private void requestForStoragePermission() {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "All permissions are granted!", Toast.LENGTH_SHORT).show();
                            requestSignIn();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    /**
     * Showing Alert Dialog with Settings option
     * Navigates user to app settings
     * NOTE: Keep proper title and message depending on your app
     */
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }

    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");

        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(DriveScopes.DRIVE_FILE))
                        .build();
        googleSignInClient = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                }
                break;

            case PICK_FILE_REQUEST:
                if(resultData == null){
                    //no data present
                    return;
                }

                loadToast.setText("Uploading file...");
                loadToast.show();

                // Get the Uri of the selected file
                Uri selectedFileUri = resultData.getData();
                Log.e(TAG, "selected File Uri: "+selectedFileUri );
                // Get the path
                String selectedFilePath = FileUtils.getPath(this, selectedFileUri);
                Log.e(TAG,"Selected File Path:" + selectedFilePath);


                if(selectedFilePath != null && !selectedFilePath.equals("")){
                    if (mDriveServiceHelper != null) {
                        mDriveServiceHelper.uploadFileToGoogleDrive(selectedFilePath)
                                .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean result) {
                                        loadToast.success();
                                        showMessage("File uploaded ...!!");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        loadToast.error();
                                        showMessage("Couldn't able to upload file, error: "+e);
                                    }
                                });
                    }
                }else{
                    Toast.makeText(this,"Cannot upload file to server",Toast.LENGTH_SHORT).show();
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleAccount -> {
                    Log.d(TAG, "Signed in as " + googleAccount.getEmail());

                    // Use the authenticated account to sign in to the Drive service.
                    GoogleAccountCredential credential =
                            GoogleAccountCredential.usingOAuth2(
                                    this, Collections.singleton(DriveScopes.DRIVE_FILE));
                    credential.setSelectedAccount(googleAccount.getAccount());
                    Drive googleDriveService =
                            new Drive.Builder(
                                    AndroidHttp.newCompatibleTransport(),
                                    new GsonFactory(),
                                    credential)
                                    .setApplicationName("Drive API Migration")
                                    .build();

                    // The DriveServiceHelper encapsulates all REST API and SAF functionality.
                    // Its instantiation is required before handling any onClick actions.
                    mDriveServiceHelper = new GoogleDriveServiceHelper(googleDriveService);

                    //enable other button as sign-in complete
                    signInButton.setEnabled(false);
                    createFolderButton.setEnabled(true);
                    folderFilesButton.setEnabled(true);
                    uploadFileButton.setEnabled(true);
                    signOutButton.setEnabled(true);

                    showMessage("Sign-In done...!!");
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e(TAG, "Unable to sign in.", exception);
                        showMessage("Unable to sign in.");
                        signInButton.setEnabled(true);
                    }
                });
    }

    // This method will get call when user click on sign-in button
    public void signIn(View view) {
        requestSignIn();
    }

    // This method will get call when user click on create folder button
    public void createFolder(View view) {
        if (mDriveServiceHelper != null) {

            // check folder present or not
            mDriveServiceHelper.isFolderPresent()
                    .addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String id) {
                            if (id.isEmpty()){
                                mDriveServiceHelper.createFolder()
                                        .addOnSuccessListener(new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String fileId) {
                                                Log.e(TAG, "folder id: "+fileId );
                                                folderId=fileId;
                                                showMessage("Folder Created with id: "+fileId);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {
                                                showMessage("Couldn't create file.");
                                                Log.e(TAG, "Couldn't create file.", exception);
                                            }
                                        });
                            }else {
                                folderId=id;
                                showMessage("Folder already present");
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            showMessage("Couldn't create file..");
                            Log.e(TAG, "Couldn't create file..", exception);
                        }
                    });
        }
    }

    // This method will get call when user click on folder data button
    public void getFolderData(View view) {
        if (mDriveServiceHelper != null) {
            Intent intent = new Intent(this, ListActivity.class);

            mDriveServiceHelper.getFolderFileList()
                    .addOnSuccessListener(new OnSuccessListener<ArrayList<GoogleDriveFileHolder>>() {
                        @Override
                        public void onSuccess(ArrayList<GoogleDriveFileHolder> result) {
                            Log.e(TAG, "onSuccess: result: "+result.size() );
                            intent.putParcelableArrayListExtra("fileList", result);
                            startActivity(intent);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showMessage("Not able to access Folder data.");
                            Log.e(TAG, "Not able to access Folder data.", e);
                        }
                    });
        }
    }

    // This method will get call when user click on upload file button
    public void uploadFile(View view) {

        Intent intent;
        if (android.os.Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            intent = new Intent("com.sec.android.app.myfiles.PICK_DATA");
            intent.putExtra("CONTENT_TYPE", "*/*");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            Log.e(TAG, "uploadFile: if" );
        } else {

//            String[] mimeTypes =
//                    {"application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .doc & .docx
//                            "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .ppt & .pptx
//                            "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", // .xls & .xlsx
//                            "text/plain",
//                            "application/pdf",
//                            "application/zip", "application/vnd.android.package-archive"};

            String[] mimeTypes = {
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            intent = new Intent(Intent.ACTION_GET_CONTENT); // or ACTION_OPEN_DOCUMENT
            intent.setType("*/*");
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            Log.e(TAG, "uploadFile: else" );
        }
        startActivityForResult(Intent.createChooser(intent,"Choose File to Upload.."),PICK_FILE_REQUEST);

    }

    // This method will get call when user click on sign-out button
    public void signOut(View view) {
        if (googleSignInClient != null){
            googleSignInClient.signOut()
                    .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            signInButton.setEnabled(true);
                            createFolderButton.setEnabled(false);
                            folderFilesButton.setEnabled(false);
                            uploadFileButton.setEnabled(false);
                            signOutButton.setEnabled(false);
                            showMessage("Sign-Out is done...!!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            signInButton.setEnabled(false);
                            showMessage("Unable to sign out.");
                            Log.e(TAG, "Unable to sign out.", exception);
                        }
                    });
        }
    }

    public void showMessage(String message) {
        Log.i(TAG, message);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}