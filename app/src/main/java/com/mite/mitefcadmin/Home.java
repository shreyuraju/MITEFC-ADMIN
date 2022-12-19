package com.mite.mitefcadmin;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Home extends AppCompatActivity {

    private NfcAdapter nfcAdapter;

    TextView mealsAmtView;
    int mealsAmt = 0;
    DatabaseReference reference, userReference;
    ProgressDialog progressDialog;
    String studentUSN=null, studentBal=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setTitle("FC ADMIN");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        reference = FirebaseDatabase.getInstance().getReference();
        userReference = FirebaseDatabase.getInstance().getReference().child("users");
        progressDialog = new ProgressDialog(this);
        mealsAmtView = findViewById(R.id.mealsAmt);

    }

    private void fetchMealsAmt() {
        reference.child("admin").child("mealsAmt").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();

                    String mealsamt = String.valueOf(map.get("amount"));
                    try {
                        mealsAmt = Integer.parseInt(mealsamt);
                    } catch (Exception e) {
                        Log.d("ERROR PARSING :", e.getMessage());
                    }
                    mealsAmtView.setText("Meals : "+mealsamt);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("ERROR", error.getMessage());
            }
        });
    }

    //checking balance data from database
    private void checkUser(String text) {
        progressDialog.setTitle("Fetching");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        userReference.child(text).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), "User Rec Found", Toast.LENGTH_SHORT).show();
                    Map<String, Object> map = (Map<String, Object>) snapshot.getValue();
                    Object balance = map.get("balance");
                    String USN1 = (String) map.get("USN");
                    studentUSN=USN1;
                    studentBal= balance.toString();
                    progressDialog.dismiss();
                    updateNewBalance(studentUSN, studentBal,mealsAmt);
                } else {
                    studentUSN=null;
                    studentBal=null;
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "User Not Found\nPlease do Register", Toast.LENGTH_SHORT).show();

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DataBase ERROR :", error.getMessage());
            }
        });
    }

    public void updateNewBalance(String studentUSN, String studentBal, int mealsAmt) {
        int newStudBal=0;
        reference = FirebaseDatabase.getInstance().getReference().child("users").child(studentUSN);
        try {
            newStudBal = Integer.parseInt(studentBal);
        } catch (Exception e) {
            Log.d("ERROR PARSING :", e.getMessage());
        }
        if (newStudBal>=mealsAmt){
            newStudBal = newStudBal - mealsAmt;
            Map map = new HashMap();
            map.put("balance", newStudBal);

            reference.updateChildren(map).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Home.this, "Token issued", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.d("BALANCE UPDATION FAILED", task.getException().getMessage());
                    }
                }
            });
        } else {
            Toast.makeText(this, "Balance is less than meals amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void readFromNFC(Tag tag, Intent intent) {
        progressDialog.setTitle("Reading");
        progressDialog.setMessage("Please Wait");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                progressDialog.dismiss();
                Toast.makeText(this, "NO Data Found Please Register", Toast.LENGTH_SHORT).show();
            } else if (ndef != null) {
                ndef.connect();
                NdefMessage ndefMessage = ndef.getNdefMessage();
                if (ndefMessage != null) {
                    Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                    if (messages != null) {
                        NdefMessage[] ndefMessages = new NdefMessage[messages.length];
                        for (int i = 0; i < messages.length; i++) {
                            ndefMessages[i] = (NdefMessage) messages[i];
                        }
                        NdefRecord record = ndefMessages[0].getRecords()[0];
                        byte[] payload = record.getPayload();
                        String text = new String(payload);
                        Log.d("DATA : ", text);
                        // NFCText.setText(text);
                        //studentUSN = text;
                        Log.e("tag", "vahid  -->  " + text);
                        progressDialog.dismiss();
                        checkUser(text);
                        ndef.close();
                    }
                } else {
                    Toast.makeText(this, "Not able to read from NFC, Please try again...", Toast.LENGTH_LONG).show();
                }
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        NdefMessage ndefMessage = ndef.getNdefMessage();
                        if (ndefMessage != null) {
                            String message = new String(ndefMessage.getRecords()[0].getPayload());
                            Log.d(TAG, "NFC found.. " + "readFromNFC: " + message);
                            //   NFCText.setText(message);
                           // studentUSN = message;

                            Log.d("DATA : ", message);
                            progressDialog.dismiss();
                            checkUser(message);
                            ndef.close();
                        } else {
                            Toast.makeText(this, "Not able to read from NFC, Please try again...", Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "NFC is not readable", Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Tag patchTag(Tag oTag) {
        if (oTag == null)
            return null;
        String[] sTechList = oTag.getTechList();

        Parcel oParcel, nParcel;

        oParcel = Parcel.obtain();
        oTag.writeToParcel(oParcel, 0);
        oParcel.setDataPosition(0);

        int len = oParcel.readInt();
        byte[] id = null;
        if (len >= 0) {
            id = new byte[len];
            oParcel.readByteArray(id);
        }
        int[] oTechList = new int[oParcel.readInt()];
        oParcel.readIntArray(oTechList);
        Bundle[] oTechExtras = oParcel.createTypedArray(Bundle.CREATOR);
        int serviceHandle = oParcel.readInt();
        int isMock = oParcel.readInt();
        IBinder tagService;
        if (isMock == 0) {
            tagService = oParcel.readStrongBinder();
        } else {
            tagService = null;
        }
        oParcel.recycle();

        int nfca_idx = -1;
        int mc_idx = -1;

        for (int idx = 0; idx < sTechList.length; idx++) {
            if (sTechList[idx] == NfcA.class.getName()) {
                nfca_idx = idx;
            } else if (sTechList[idx] == MifareClassic.class.getName()) {
                mc_idx = idx;
            }
        }

        if (nfca_idx >= 0 && mc_idx >= 0 && oTechExtras[mc_idx] == null) {
            oTechExtras[mc_idx] = oTechExtras[nfca_idx];
        } else {
            return oTag;
        }

        nParcel = Parcel.obtain();
        nParcel.writeInt(id.length);
        nParcel.writeByteArray(id);
        nParcel.writeInt(oTechList.length);
        nParcel.writeIntArray(oTechList);
        nParcel.writeTypedArray(oTechExtras, 0);
        nParcel.writeInt(serviceHandle);
        nParcel.writeInt(isMock);
        if (isMock == 0) {
            nParcel.writeStrongBinder(tagService);
        }
        nParcel.setDataPosition(0);
        Tag nTag = Tag.CREATOR.createFromParcel(nParcel);
        nParcel.recycle();
        return nTag;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        patchTag(tag);
        if (tag != null) {
            readFromNFC(tag, intent);
        }

    }

    //on pause and on resume of NFC Activity
    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdapter != null) nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchMealsAmt();
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

            case R.id.transaction: startActivity(new Intent(this, Transactions.class)); break;

            case R.id.exit: System.exit(0);
        }
        return super.onOptionsItemSelected(item);
    }
}