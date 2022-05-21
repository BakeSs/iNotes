package com.example.testlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private EditText loginEdit;
    private EditText passwordEdit;
    private Button regLogButton;
    private Button restorePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        database = FirebaseDatabase.getInstance("https://inotes-chad-default-rtdb.europe-west1.firebasedatabase.app/");
        myRef = database.getReference("users");

        loginEdit = findViewById(R.id.login);
        passwordEdit = findViewById(R.id.password);
        regLogButton = findViewById(R.id.login_or_register);
        restorePassword = findViewById(R.id.restore_password);

        mAuth = FirebaseAuth.getInstance();

        regLogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
                    Toast.makeText(LoginActivity.this, "Заповніть усі поля", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(password.length() < 6){
                    Toast.makeText(LoginActivity.this, "Мінімальна довжина паролю 6 символів!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(email.length() < 6 || !email.contains("@") || !email.contains(".")){
                    Toast.makeText(LoginActivity.this, "Перевірте правильність введення пошти!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password).
                        addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    mAuth.createUserWithEmailAndPassword(email, password).
                                            addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()){
                                                        MainActivity.Item new_item = new MainActivity.Item("Видалені");
                                                        myRef.child(mAuth.getUid()).child("folders").child("Видалені").setValue(new_item);
                                                        FirebaseUser user = mAuth.getCurrentUser();
                                                        updateUI(user);
                                                        Toast.makeText(LoginActivity.this, "Ваш акаунт створено",
                                                                Toast.LENGTH_SHORT).show();
                                                    } else{
                                                        Toast.makeText(LoginActivity.this, "Перевірте правильність введених даних та підключення до інтернету!",
                                                                Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

        restorePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String emailAddress = loginEdit.getText().toString();
                if(TextUtils.isEmpty(emailAddress)){
                    Toast.makeText(LoginActivity.this, "Заповніть поле з поштою!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Лист відновлення відправлено на пошту!", Toast.LENGTH_SHORT).show();
                                } else{
                                    Toast.makeText(LoginActivity.this, "Перевірте правильність пошти!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            updateUI(currentUser);
        }
    }

    private void updateUI(FirebaseUser currentUser) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putString("userName", currentUser.getUid());

        intent.putExtras(extras);

        startActivity(intent);
    }
}