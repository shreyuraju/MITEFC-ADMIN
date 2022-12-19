package com.mite.mitefcadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class updateAmt extends AppCompatActivity {

    EditText amtText;
    TextView mealsAmt;
    Button updateBtn;
    String amount;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_amt);
        getSupportActionBar().setTitle("Update Meal Amount");

        amtText = findViewById(R.id.amtText);
        updateBtn = findViewById(R.id.updateBtn);
        mealsAmt = findViewById(R.id.mealsAmt);

        reference = FirebaseDatabase.getInstance().getReference();

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                amount = amtText.getText().toString();
                updateMealAmt(amount);
            }
        });


    }
    private void fetchMealsAmt() {
        reference.child("admin").child("mealsAmt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    String mealsamt = String.valueOf(map.get("amount"));
                    mealsAmt.setText("Meals : "+mealsamt);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ERROR", error.getMessage());
            }
        });
    }
    private void updateMealAmt(String amount) {
        int intNewAmt = 0;
        try {
            intNewAmt = Integer.parseInt(amount);
        } catch (NumberFormatException e){
            Log.d("ERROR PARSEING", e.getMessage());
        }
        if (intNewAmt == 0) {
            amtText.setError("please enter proper number");
        } else {
            DatabaseReference databaseReference = reference.child("admin").child("mealsAmt");

            Map map = new HashMap();
            map.put("amount", intNewAmt);

            databaseReference.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        amtText.setText(null);
                        Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                        context();
                    } else {
                        Log.d("ERROR", task.getException().getMessage());
                    }
                }
            });
        }
    }
    private void context() {
        refresh(1000);
    }
    private void refresh(int i) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                fetchMealsAmt();
                context();
            }
        };
        handler.postDelayed(runnable, i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchMealsAmt();
    }
}