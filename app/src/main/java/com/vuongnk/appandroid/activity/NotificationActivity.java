package com.vuongnk.appandroid.activity;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vuongnk.appandroid.R;
import com.vuongnk.appandroid.adapter.NotificationAdapter;
import com.vuongnk.appandroid.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class NotificationActivity extends AppCompatActivity {
    private RecyclerView rcvNotifications;
    private NotificationAdapter adapter;
    private DatabaseReference notificationsRef;
    private ValueEventListener notificationsListener;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        initUI();
        loadNotifications();
    }

    private void initUI() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        rcvNotifications = findViewById(R.id.rcv_notifications);
        rcvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(this);
        rcvNotifications.setAdapter(adapter);
    }

    private void loadNotifications() {
        notificationsRef = FirebaseDatabase.getInstance()
                .getReference("user_notifications")
                .child(userId);

        notificationsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Notification> notifications = new ArrayList<>();

                for (DataSnapshot notifSnapshot : dataSnapshot.getChildren()) {
                    String notifId = notifSnapshot.getKey();
                    if (Boolean.TRUE.equals(notifSnapshot.getValue(Boolean.class))) {
                        FirebaseDatabase.getInstance()
                                .getReference("notifications")
                                .child(notifId)
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    Notification notification = snapshot.getValue(Notification.class);
                                    if (notification != null) {
                                        notification.setId(snapshot.getKey());
                                        notifications.add(notification);

                                        notifications.sort((n1, n2) ->
                                                Long.compare(n2.getTimestamp(), n1.getTimestamp()));
                                        adapter.setNotifications(notifications);
                                    }
                                });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NotificationActivity.this,
                        "Lỗi: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        };

        notificationsRef.addValueEventListener(notificationsListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationsListener != null) {
            notificationsRef.removeEventListener(notificationsListener);
        }
    }
}
