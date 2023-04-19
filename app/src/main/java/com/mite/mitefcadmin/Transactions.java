package com.mite.mitefcadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mite.mitefcadmin.transaction.MyAdapter;
import com.mite.mitefcadmin.transaction.Trans;

import java.util.ArrayList;

public class Transactions extends AppCompatActivity {

    DatabaseReference transReference;
    RecyclerView recyclerView;
    MyAdapter myAdapter;
    ArrayList<Trans> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transactions);
        getSupportActionBar().setTitle("ALL TRANSACTIONS");

        transReference = FirebaseDatabase.getInstance().getReference().child("admin");

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
        recyclerView.setAdapter(myAdapter);
        transReference.child("alltransaction").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot data : snapshot.getChildren()) {
                        //Log.d("DATA", data.getValue().toString());
                        if (data.child("USN").getValue() != null) {
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
                            date = "Date :"+date.substring(0,10) +" "+ date.substring(11,16);
                            utr = "utr no :"+utr;
                            Trans trans = new Trans();
                            trans.setUSN(USN);
                            trans.setAmount(amount);
                            trans.setDate(date);
                            trans.setMode(mode);
                            trans.setUtr(utr);
                            list.add(trans);
                        }
                    }
                    Log.d("LIST DATA:", list.toString());
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

    @Override
    protected void onStart() {
        super.onStart();
        showTransaction();
    }
}