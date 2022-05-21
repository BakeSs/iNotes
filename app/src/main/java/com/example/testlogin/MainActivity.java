package com.example.testlogin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static class Item implements Serializable {
        String folderName;

        public Item() {
        }

        Item(String folderName){
            this.folderName = folderName;
        }

        public String getFolderName() {
            return folderName;
        }
    }

    private EditText searchFolders;
    private ListView items;
    private FloatingActionButton createFolder;
    private ImageButton logOut;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private ArrayList<Item> foldersList;
    private ItemsAdapter adapter;

    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            userName = extras.getString("userName");
        } else{
            database.setPersistenceEnabled(true);
        }

        myRef = database.getReference("users");

        searchFolders = findViewById(R.id.search_folders);
        items = findViewById(R.id.items);
        createFolder = findViewById(R.id.create_folder);
        logOut = findViewById(R.id.log_out);

        adapter = new ItemsAdapter();
        items.setAdapter(adapter);

        items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openActivityNotesList(adapter.getItem(position).getFolderName());
            }
        });

        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Pop.class);

                Bundle extras = new Bundle();

                extras.putString("userName", userName);
                extras.putSerializable("foldersList", foldersList);

                intent.putExtras(extras);
                startActivity(intent);
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(MainActivity.this, "Ще побачимось:)", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);

                startActivity(intent);
            }
        });

        searchFolders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.clear();

                String search_text = searchFolders.getText().toString().trim();

                if(search_text.length() != 0){
                    for(Item item : foldersList){
                        if(item.getFolderName().toLowerCase(Locale.ROOT).contains(search_text.toLowerCase(Locale.ROOT))){
                            adapter.add(item);
                        }
                    }
                } else{
                    for(Item item : foldersList){
                        adapter.add(item);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("userName", userName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference("users");
    }

    @Override
    protected void onStart() {
        super.onStart();

        Query myQuery = myRef;
        myQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adapter.clear();

                DataSnapshot folderName = snapshot.child(userName).child("folders");
                Iterable<DataSnapshot> folderChildren = folderName.getChildren();

                foldersList = new ArrayList<Item>();
                Item deletedFolder = null;
                for(DataSnapshot folder : folderChildren){
                    Item selectedFolder = folder.getValue(Item.class);

                    foldersList.add(selectedFolder);
                    if(selectedFolder.getFolderName().equals("Видалені")){
                        deletedFolder = selectedFolder;
                    } else{
                        adapter.add(selectedFolder);
                    }
                }
                adapter.add(deletedFolder);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void openActivityNotesList(String FolderName) {
        Intent intent = new Intent(this, NotesListActivity.class);
        Bundle extras = new Bundle();
        extras.putString("folderName", FolderName);
        extras.putString("userName", userName);

        intent.putExtras(extras);

        startActivity(intent);
    }

    private class ItemsAdapter extends ArrayAdapter<Item> {
        public ItemsAdapter() {
            super(MainActivity.this, R.layout.folder_item);
        }

        @Nullable
        @Override
        public Item getItem(int position) {
            return super.getItem(position);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final LayoutInflater inflater = getLayoutInflater();
            final Item item = getItem(position);

            final View view = inflater.inflate(R.layout.folder_item_deleted, null, true);
            ((TextView) view.findViewById(R.id.item_name)).setText(item.getFolderName());

            return view;
        }
    }
}