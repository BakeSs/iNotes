package com.example.testlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {
    private ImageView buttonBack;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private TextView noteName;
    private TextView noteText;

    private String userName;
    private String folderName;
    private String inputtedNoteName;
    private String inputtedNoteText;
    private ArrayList<NotesListActivity.FolderItem> notesList;
    private InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        //Розпакування переданих змінних
        Bundle extras = getIntent().getExtras();

        userName = extras.getString("userName");
        folderName = extras.getString("folderName");
        inputtedNoteName = extras.getString("noteName");
        inputtedNoteText = extras.getString("noteText");
        Serializable serializableNotesList = extras.getSerializable("notesList");
        notesList = (ArrayList<com.example.testlogin.NotesListActivity.FolderItem>) serializableNotesList;

        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference("users").child(userName).child("folders").child(folderName).child("notes");

        buttonBack = findViewById(R.id.back_button);
        noteName = findViewById(R.id.note_name);
        noteText = findViewById(R.id.note_text);

        noteName.requestFocus();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        if(inputtedNoteName.length() != 0) {
            noteName.setText(inputtedNoteName);
            noteText.setText(inputtedNoteText);
        }

        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivityNotesList(userName, folderName);
            }
        });
    }

    @Override
    public void onBackPressed() {
        Bundle extras = getIntent().getExtras();
        String userName = extras.getString("userName");
        String folderName = extras.getString("folderName");

        openActivityNotesList(userName, folderName);
    }

    @Override
    protected void onStop() {
        createNote();
        super.onStop();
    }

    @Override
    protected void onResume() {
        inputtedNoteName = noteName.getText().toString().trim();
        inputtedNoteText = noteText.getText().toString().trim();
        super.onResume();
    }

    @Override
    protected void onPause() {
        imm.hideSoftInputFromWindow(noteName.getWindowToken(), 0);
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("userName", userName);
        outState.putString("folderName", folderName);
        outState.putString("noteName", String.valueOf(noteName));
        outState.putString("noteText", String.valueOf(noteText));
        outState.putSerializable("notesList", notesList);
        super.onSaveInstanceState(outState);
    }

    public void openActivityNotesList(String userName, String folderName) {
        Intent intent = new Intent(this, NotesListActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userName", userName);
        extras.putString("folderName", folderName);

        intent.putExtras(extras);

        startActivity(intent);
    }
    public void createNote(){
        String name = noteName.getText().toString().trim();
        String text = noteText.getText().toString().trim();
        Pattern regex = Pattern.compile("[$+,:;=\\\\?#|/'<>.^*%!-]");
        if((inputtedNoteName.equals(name) && inputtedNoteText.equals(text)) || (name.length() == 0 && text.length() == 0)){
            return;
        }
        if(name.length() == 0){
            Toast.makeText(getBaseContext(), "Назва нотатки пуста!",Toast.LENGTH_LONG).show();
            return;
        }
        if(name.length() > 30){
            Toast.makeText(getBaseContext(), "Назва нотатки перевищує 30 символів!",Toast.LENGTH_LONG).show();
            return;
        }
        if(regex.matcher(name).find()){
            Toast.makeText(CreateNoteActivity.this, "Недопустима назва нотатки!",Toast.LENGTH_LONG).show();
            return;
        }
        if(inputtedNoteName.equals(name)){
            myRef.child(name).setValue(new NotesListActivity.FolderItem(name, text));
            Toast.makeText(getBaseContext(), "Нотатку редаговано!",Toast.LENGTH_SHORT).show();
            return;
        }
        if(inputtedNoteName.length() != 0){
            for(NotesListActivity.FolderItem note : notesList){
                if(note.getNoteName().equals(name)){
                    Toast.makeText(CreateNoteActivity.this, "Нотатка з такою назвою вже існує!",Toast.LENGTH_LONG).show();
                    return;
                }
            }
            myRef.child(inputtedNoteName).removeValue();
            myRef.child(name).setValue(new NotesListActivity.FolderItem(name, text));
            Toast.makeText(getBaseContext(), "Нотатку редаговано!",Toast.LENGTH_SHORT).show();
            return;
        }

        for(NotesListActivity.FolderItem note : notesList){
            if(note.getNoteName().equals(name)){
                Toast.makeText(CreateNoteActivity.this, "Нотатка з такою назвою вже існує!",Toast.LENGTH_LONG).show();
                return;
            }
        }
        myRef.child(name).setValue(new NotesListActivity.FolderItem(name, text));
        Toast.makeText(getBaseContext(), "Нотатку створено!",Toast.LENGTH_SHORT).show();
    }
}