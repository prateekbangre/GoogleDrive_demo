package com.dreamappsstore.googledrive_demo;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;
import java.util.ArrayList;

import static com.dreamappsstore.googledrive_demo.MainActivity.mDriveServiceHelper;

class FilesAdapter extends ArrayAdapter<GoogleDriveFileHolder>{

    private ArrayList<GoogleDriveFileHolder> filesList;
    Context context;
    private static final String STORAGE_FOLDER_PATH = "/storage/emulated/0/Example_Download";

    public FilesAdapter(Context context, ArrayList<GoogleDriveFileHolder> filesList) {
        super(context, R.layout.file_list_layout, filesList);
        this.filesList = filesList;
        this.context=context;
        isFolderExits();
    }

    private void isFolderExits() {
        File file = new File(STORAGE_FOLDER_PATH);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        final GoogleDriveFileHolder fileInfo = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.file_list_layout, parent, false);

            viewHolder.fileName = convertView.findViewById(R.id.id_file_name);
            viewHolder.downloadFile = convertView.findViewById(R.id.id_file_download);
            viewHolder.deleteFile = convertView.findViewById(R.id.id_file_delete);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.fileName.setText(fileInfo.getName());
        viewHolder.downloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDriveServiceHelper.downloadFile(new java.io.File(STORAGE_FOLDER_PATH, fileInfo.getName()), fileInfo.getId())
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {

                                if (result)
                                    showMessage("Successfully downloaded file ...!!");
                                else
                                    showMessage("Not Able to downloaded file ...!!");
                            }
                        })
//                        .addOnFailureListener(exception -> showMessage("Got error while downloading file."));
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("TAG", "onFailure: error: "+e.getMessage());
                        showMessage("Got error while downloading file.");
                    }
                });
            }
        });
        viewHolder.deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDriveServiceHelper.deleteFolderFile(fileInfo.getId())
                        .addOnSuccessListener(new OnSuccessListener<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {

                                if (result){
                                    showMessage("Successfully deleted file ...!!");
                                    filesList.remove(position);
                                    notifyDataSetChanged();
                                }
                                else
                                    showMessage("Not Able to delete file ...!!");
                            }
                        })
                        .addOnFailureListener(exception -> showMessage("Got error while deleting file."));
            }
        });
        // Return the completed view to render on screen
        return convertView;
    }

    public void showMessage(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    // View lookup cache
    private static class ViewHolder {
        TextView fileName;
        ImageButton downloadFile;
        ImageButton deleteFile;
    }
}