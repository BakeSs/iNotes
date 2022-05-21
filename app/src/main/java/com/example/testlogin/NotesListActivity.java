package com.example.testlogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class NotesListActivity extends AppCompatActivity {
    static class FolderItem implements Serializable{
        String noteName;
        String noteText;

        FolderItem(){
        }

        FolderItem(String note_name, String note_text){
            this.noteName = note_name;
            this.noteText = note_text;
        }

        public String getNoteName() {
            return noteName;
        }

        public String getNoteText() {
            return noteText;
        }
    }

    private ListView notes;
    private FloatingActionButton createNote;
    private ImageView buttonBack;
    private EditText searchNotes;
    private Button deleteNotes;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private String userName;
    private String folderName;
    private ArrayList<FolderItem> notesList;
    private ImageButton delete_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_list);

        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");

        //Розпакування переданих змінних
        Bundle extras = getIntent().getExtras();
        userName = extras.getString("userName");
        folderName = extras.getString("folderName");

        notes = findViewById(R.id.notes);
        createNote = findViewById(R.id.create_note);
        buttonBack = findViewById(R.id.back_button);
        searchNotes = findViewById(R.id.search_notes);
        deleteNotes = findViewById(R.id.delete_all_notes);

        myRef = database.getReference("users").child(userName).child("folders");

        FolderItemsAdapter adapter = new FolderItemsAdapter();
        notes.setAdapter(adapter);

        notes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openActivityCreateNote(userName, folderName, adapter.getItem(position).getNoteName(), adapter.getItem(position).getNoteText(), notesList);
            }
        });

        createNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivityCreateNote(userName, folderName,"", "", notesList);
            }
        });
        deleteNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference baseRef = database.getReference("users").child(userName).child("folders").child("Видалені").child("notes");

                if(folderName.equals("Видалені")){
                    Toast.makeText(getBaseContext(), "Нотатки видалено повністю!",Toast.LENGTH_SHORT).show();
                    myRef.child(folderName).child("notes").removeValue();
                } else{
                    for(FolderItem note : notesList){
                        baseRef.child(note.getNoteName()).setValue(new NotesListActivity.FolderItem(note.getNoteName(), note.getNoteText()));
                    }
                    myRef.child(folderName).removeValue();

                    Toast.makeText(getBaseContext(), "Нотатки перенесено у видалені!",Toast.LENGTH_SHORT).show();

                    onBackPressed();
                }

                notesList.clear();
            }
        });
        Query myQuery = database.getReference("users");
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();

                DataSnapshot currentNotes = snapshot.child(userName).child("folders").child(folderName).child("notes");
                Iterable<DataSnapshot> note_children = currentNotes.getChildren();

                notesList = new ArrayList<FolderItem>();
                for(DataSnapshot note : note_children){
                    FolderItem selectedNote = note.getValue(FolderItem.class);

                    notesList.add(selectedNote);
                    adapter.add(selectedNote);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openActivityMain(userName);
            }
        });
        searchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.clear();

                String searchText = searchNotes.getText().toString().trim();

                if(searchText.length() != 0){
                    for(FolderItem item : notesList){
                        if(item.getNoteName().toLowerCase(Locale.ROOT).contains(searchText.toLowerCase(Locale.ROOT))){
                            adapter.add(item);
                        }
                    }
                } else{
                    for(FolderItem item : notesList){
                        adapter.add(item);
                    }
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        Bundle extras = getIntent().getExtras();
        String userName = extras.getString("userName");
        openActivityMain(userName);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("userName", userName);
        outState.putString("folderName", folderName);
        super.onSaveInstanceState(outState);
    }

    public void openActivityCreateNote(String userName, String folderName, String noteName, String noteText, ArrayList<FolderItem> notesList) {
        Intent intent = new Intent(this, CreateNoteActivity.class);

        Bundle extras = new Bundle();

        extras.putString("userName", userName);
        extras.putString("folderName", folderName);
        extras.putString("noteName", noteName);
        extras.putString("noteText", noteText);
        extras.putSerializable("notesList", notesList);

        intent.putExtras(extras);
        startActivity(intent);
    }

    public void openActivityMain(String userName){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userName", userName);

        intent.putExtras(extras);

        startActivity(intent);
    }

    private class FolderItemsAdapter extends ArrayAdapter<FolderItem>{
        public FolderItemsAdapter(){ super(NotesListActivity.this, R.layout.note_item); }

        @Nullable
        @Override
        public FolderItem getItem(int position) {
            return super.getItem(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.note_item, null);
            final FolderItem folderItem = getItem(position);
            ((TextView)view.findViewById(R.id.item_name)).setText(folderItem.getNoteName());

            delete_button = (ImageButton) view.findViewById(R.id.item_delete_button);

            delete_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(folderName.equals("Видалені")){
                        Toast.makeText(getBaseContext(), "Нотатку видалено повністю!",Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(getBaseContext(), "Нотатку перенесено у видалені!",Toast.LENGTH_SHORT).show();

                        myRef.child("Видалені").child("notes").child(folderItem.getNoteName()).
                                setValue(new FolderItem(folderItem.getNoteName(), folderItem.getNoteText()));
                    }
                    myRef.child(folderName).child("notes").child(folderItem.getNoteName()).removeValue();
                }
            });
            return view;
        }
    }
}