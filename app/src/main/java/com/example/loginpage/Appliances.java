package com.example.loginpage;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Appliances extends AppCompatActivity {
    private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> arraylist;
    private HashMap<String, AppliancesData> hashMap;
    private int lastExpandedPosition = -1;
    private FloatingActionButton addnewappliance;
    private DatabaseReference rootref, appliancesref;
    private FirebaseAuth auth;
    private DrawerLayout navDrawer;
    private ActionBarDrawerToggle toggle;
    private TextView emptyMessage;
    private View confirmationDialogView;
    private Button confirm, cancel;
    private TextView logoutMsg;
    private AlertDialog.Builder builder;
    private AlertDialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliances);
        addnewappliance = (FloatingActionButton) findViewById(R.id.newappliancebutton);
        expandableListView = findViewById(R.id.applianceslistexpandable);
        emptyMessage = findViewById(R.id.EmptyMessage);
        confirmationDialogView = getLayoutInflater().inflate(R.layout.action_confirmation_dialogue, null);

        init();

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1 && groupPosition != lastExpandedPosition) {
                    expandableListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;

            }
        });

        addnewappliance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), newApplicanceActivity.class);
                startActivity(i);
            }
        });

        //Dialog for logout confirmation
        builder = new AlertDialog.Builder(Appliances.this);
        builder.setCancelable(false);
        builder.setView(confirmationDialogView);
        confirm = confirmationDialogView.findViewById(R.id.confirmAction);
        cancel = confirmationDialogView.findViewById(R.id.cancelAction);
        logoutMsg = confirmationDialogView.findViewById(R.id.confirmMsg);
        logoutDialog = builder.create();
        logoutDialog.setCanceledOnTouchOutside(false);

        logoutMsg.setText("Do you really want to logout?");
        confirm.setText("Confirm Logout");

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                logoutDialog.cancel();
                Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutDialog.cancel();
            }
        });
        //dialog for logout confirmation

        //Navigation Bar code start
        NavigationView navigationView = findViewById(R.id.lisofitems);
        navDrawer = (DrawerLayout) findViewById(R.id.ApplianceDrawer);
        toggle = new ActionBarDrawerToggle(this, navDrawer, R.string.open, R.string.close);
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.trigger) {
                    Intent i = new Intent(getApplicationContext(), TriggerActivity.class);
                    startActivity(i);
                    finish();
                } else if (id == R.id.dashboard) {
                    Intent dashboard = new Intent(getApplicationContext(), DashBoard.class);
                    startActivity(dashboard);
                    finish();
                } else if (id == R.id.logout) {
                    logoutDialog.show();
                } else if (id == R.id.profile) {
                    Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(profile);
                    finish();
                }
                return true;
            }
        });
        //Navigation bar code end


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void init() {
        arraylist = new ArrayList<>();
        hashMap = new HashMap<>();
        auth = FirebaseAuth.getInstance();
        rootref = FirebaseDatabase.getInstance().getReference();
        appliancesref = rootref.child("Appliances").child(auth.getCurrentUser().getUid());

        appliancesref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hashMap.clear();
                arraylist.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    HashMap<String, String> temp = new HashMap<>();
                    arraylist.add(snap.getKey());
                    System.out.println(((HashMap<String, String>) snap.getValue()).get("title"));
                    temp = (HashMap<String, String>) snap.getValue();
                    AppliancesData appliancesData = new AppliancesData(
                            ((HashMap<String, String>) snap.getValue()).get("title"),
                            ((HashMap<String, String>) snap.getValue()).get("status"),
                            ((HashMap<String, String>) snap.getValue()).get("category"),
                            ((HashMap<String, String>) snap.getValue()).get("imageUri")
                    );
                    hashMap.put(((HashMap<String, String>) snap.getValue()).get("title"), appliancesData);
                    //System.out.println(hashMap.get("pic"));
                }
                if (arraylist.isEmpty()) {
                    emptyMessage.setVisibility(View.VISIBLE);
                } else {
                    emptyMessage.setVisibility(View.INVISIBLE);
                }
                adapter = new CustomAdapterForExpandable(Appliances.this, arraylist, hashMap, confirmationDialogView);
                expandableListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}