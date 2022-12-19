package com.mite.mitefcadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mite.mitefcadmin.transaction.MyAdapter;
import com.mite.mitefcadmin.transaction.Trans;

import java.util.ArrayList;

public class Home extends AppCompatActivity {



    DatabaseReference transReference;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<Trans> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        transReference = FirebaseDatabase.getInstance().getReference().child("alltransaction");

        recyclerView = findViewById(R.id.transList);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();
        myAdapter = new MyAdapter(this,list);
        recyclerView.setAdapter(myAdapter);

    }

    private void showTransaction() {
        list.clear();
        recyclerView.setAdapter(myAdapter);
        transReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        Log.d("DATA", data.getValue().toString());
                        String USN, amount, date, mode, utr;
                        USN = data.child("USN").getValue().toString();
                        amount = data.child("amount").getValue().toString();
                        date = data.child("date").getValue().toString();
                        mode = data.child("mode").getValue().toString();
                        utr = data.child("utr").getValue().toString();
                        USN = "USN :"+USN;
                        if (mode.equals("credit")) {
                            amount = "+"+amount+" rs";
                        } else {
                            amount = "-"+amount+" rs";
                        }
                        date = "Date :"+date.substring(0,10) +", "+ date.substring(14,19);
                        utr = "utr no :"+utr;

                        Trans trans = new Trans();
                        trans.setUSN(USN);
                        trans.setAmount(amount);
                        trans.setDate(date);
                        trans.setMode(mode);
                        trans.setUtr(utr);
                        list.add(trans);
                    }
                    //Log.d("LIST DATA:", list.toString());
                    myAdapter.notifyDataSetChanged();
                } else {
                    recyclerView.setAdapter(null);
                    //  Toast.makeText(Home.this, "No Transaction Exist", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DataBase ERROR :", error.getMessage());
            }
        });
    }

    private void context() {
        refresh(1000);
    }
    private void refresh(int i) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                context();
            }
        };
        handler.postDelayed(runnable, i);
    }

    @Override
    protected void onStart() {
        super.onStart();
        showTransaction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.update: Intent i = new Intent(getBaseContext(), updateAmt.class);
            startActivity(i);
            break;

            case R.id.exit: System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}