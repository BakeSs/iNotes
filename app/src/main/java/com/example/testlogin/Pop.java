package com.example.testlogin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class Pop extends Activity {
    private ArrayList<MainActivity.Item> foldersList;
    private String userName;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private InputMethodManager imm;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pop_create_folder_window);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.9),(int)(height*.22));
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Розпакування переданих змінних
        Bundle extras = getIntent().getExtras();

        userName = extras.getString("userName");
        Serializable serializableFoldersList = extras.getSerializable("foldersList");
        foldersList = (ArrayList<MainActivity.Item>) serializableFoldersList;

        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference("users");

        Button buttonFolderAdd = findViewById(R.id.button_folder_add);
        EditText newFolderName = findViewById(R.id.new_folder_name);

        newFolderName.requestFocus();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        buttonFolderAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_folder_name = newFolderName.getText().toString().trim();
                Pattern regex = Pattern.compile("[$+,:;=\\\\?#|/'<>.^*%!-]");
                if(input_folder_name.length() == 0){
                    Toast.makeText(getBaseContext(), "Назва папки пуста!",Toast.LENGTH_LONG).show();
                    return;
                }
                if(input_folder_name.length() > 50){
                    Toast.makeText(getBaseContext(), "Назва папки перевищує 50 символів!",Toast.LENGTH_LONG).show();
                    return;
                }
                if(regex.matcher(input_folder_name).find()){
                    Toast.makeText(getBaseContext(), "Недопустима назва папки!",Toast.LENGTH_LONG).show();
                    return;
                }
                for(MainActivity.Item item : foldersList){
                    if(item.getFolderName().equals(input_folder_name)) {
                        Toast.makeText(getBaseContext(), "Папка з такою назвою вже існує!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
                MainActivity.Item new_item = new MainActivity.Item(input_folder_name);
                myRef.child(userName).child("folders").child(input_folder_name).setValue(new_item);
                Toast.makeText(getBaseContext(), "Папку створено!",Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        });
    }
}