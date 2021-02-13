package com.dreamappsstore.googledrive_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView filesListView;
    FilesAdapter filesAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ArrayList<GoogleDriveFileHolder> folderFilesList = getIntent().getParcelableArrayListExtra("fileList");

        this.filesListView = findViewById(R.id.id_file_list);
        filesAdapter = new FilesAdapter(getApplicationContext(), folderFilesList);
        filesListView.setAdapter(filesAdapter);
    }
}