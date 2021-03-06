package com.example.loginpage;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class TriggerActivity extends AppCompatActivity {

    private DrawerLayout navDrawer;
    private ActionBarDrawerToggle toggle;
    private FirebaseAuth auth;
    ListView eventslist;
    private FloatingActionButton newevent;
    ArrayList<String> eventtitlesG;
    HashMap<String, HashMap<String, String>> map;
    private DatabaseReference rootref, triggerref;
    private TextView emptyMsg;

    private View confirmationDialogView;
    private Button confirm, cancel;
    private TextView logoutMsg;
    private AlertDialog.Builder builder;
    private AlertDialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trigger);
        auth = FirebaseAuth.getInstance();
        confirmationDialogView = getLayoutInflater().inflate(R.layout.action_confirmation_dialogue, null);

        emptyMsg = findViewById(R.id.EmptyMessage);
        eventslist = findViewById(R.id.eventslistview);
        newevent = findViewById(R.id.addnewevent);
        eventtitlesG = new ArrayList<String>();
        map = new HashMap<>();

        //Dialog for logout confirmation
        builder = new AlertDialog.Builder(TriggerActivity.this);
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
        NavigationView navigationView;
        navigationView = findViewById(R.id.lisofitems);
        navDrawer = (DrawerLayout) findViewById(R.id.triggernav);
        toggle = new ActionBarDrawerToggle(this, navDrawer, R.string.open, R.string.close);
        navDrawer.addDrawerListener(toggle);
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if(id == R.id.trigger){
                    Intent i = new Intent(getApplicationContext(), TriggerActivity.class);
                    startActivity(i);
                    finish();
                }
                else if (id == R.id.dashboard){
                    Intent dashboard = new Intent(getApplicationContext(), DashBoard.class);
                    startActivity(dashboard);
                    finish();
                }
                else if (id == R.id.logout){
                    logoutDialog.show();
                }else if (id == R.id.profile){
                    Intent profile = new Intent(getApplicationContext(), ProfileActivity.class);
                    startActivity(profile);
                    finish();
                }
                return true;
            }
        });
        //Navigation bar code end

        rootref = FirebaseDatabase.getInstance().getReference();
        triggerref = rootref.child("Trigger").child(auth.getCurrentUser().getUid());
        triggerref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                eventtitlesG.clear();
                map.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    eventtitlesG.add(snap.getKey());
                    map.put(snap.getKey(), (HashMap) snap.getValue());
//                    System.out.println(snap.getKey());
                }
                if (eventtitlesG.isEmpty()) {
                    emptyMsg.setVisibility(View.VISIBLE);
                } else {
                    emptyMsg.setVisibility(View.INVISIBLE);
                }
                //ListView for events start
                AdapterForEvents adapterForEvents = new AdapterForEvents(TriggerActivity.this, R.layout.row_for_events, eventtitlesG);
                eventslist.setAdapter(adapterForEvents);
                //ListView for events end
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        newevent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), NewEventSetter.class);
                startActivity(i);
            }
        });




    }
    private class AdapterForEvents extends ArrayAdapter<String>{
        Context context;
        List<String> eventtitles;
        int layout;

        AdapterForEvents(Context context, int resource, List<String> eventtitles) {
            super(context, resource, eventtitles);
            this.context = context;
            this.eventtitles = eventtitles;
            this.layout = resource;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            HashMap<String, String> alarmdet = map.get(getItem(position));
            String time = alarmdet.get("Time");
            String date = alarmdet.get("Date");
            String des = alarmdet.get("Description");
            ViewHolderTrig mainholder = null;
            if(convertView == null){
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(layout, parent, false);
                ViewHolderTrig viewHolderTrig = new ViewHolderTrig();
                viewHolderTrig.time = (TextView) convertView.findViewById(R.id.timetrigger);
                viewHolderTrig.date = (TextView)convertView.findViewById(R.id.datetrigger);
                viewHolderTrig.description = (TextView) convertView.findViewById(R.id.descriptiontrigger);
                convertView.setTag(viewHolderTrig);
            }
            mainholder = (ViewHolderTrig) convertView.getTag();
            mainholder.time.setText(time);
            mainholder.date.setText(date);
            mainholder.description.setText(des);
            return convertView;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.repeat){
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 3);
            calendar.set(Calendar.MINUTE, 2);

//            TimePickerDialog timePickerDialog = new TimePickerDialog(TriggerActivity.this, new TimePickerDialog.OnTimeSetListener() {
//                @Override
//                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//
//                    calendar.set(Calendar.MINUTE, minute);
//                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
//                }
//            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
//
//            timePickerDialog.show();

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), Notifiction_receiver.class);
            intent.setAction("MY_NOTIFICATION_MESSAGE");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1500, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            Toast.makeText(getApplicationContext(), "Notification setup successfully!", Toast.LENGTH_LONG).show();
        }

        if(toggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_for_trigger, menu);
        return true;
    }

    class ViewHolderTrig{
        TextView date;
        TextView time;
        TextView description;
    }
}