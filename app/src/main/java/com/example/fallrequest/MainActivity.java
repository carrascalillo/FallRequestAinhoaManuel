package com.example.fallrequest;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crearCanalNotificacion();
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    basicReadWrite();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            ;
        };
        thread.start();
    }

    private void crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence nombre = "Nombre del canal";
            String descripcion = "Descripción del canal";
            //int importancia = NotificationManager.IMPORTANCE_DEFAULT;
            int importancia = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel canal = new NotificationChannel("canal_id", nombre, importancia);
            canal.setDescription(descripcion);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    public void basicReadWrite() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference rpi308 = database.getReference("RPi@SH308A");
        TextView timeUI = findViewById(R.id.timeUIvalue);
        TextView xUI = findViewById(R.id.xUIvalue);
        TextView yUI = findViewById(R.id.yUIValue);
        TextView zUI = findViewById(R.id.zUIvalue);
        TextView flagUI = findViewById(R.id.flagUIvalue);
        rpi308.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String time_stamp = dataSnapshot.child("timestamp").getValue(String.class);
                String x = dataSnapshot.child("X-axis").getValue(String.class);
                String y = dataSnapshot.child("Y-axis").getValue(String.class);
                String z = dataSnapshot.child("Z-axis").getValue(String.class);
                String flag = dataSnapshot.child("FLAG").getValue(String.class);
                Log.d(TAG, time_stamp + " X: " + x + " Z:" + z);
                if (flag != null && flag.equals("True")) {
                    showNotification("Señal True", "La señal es True");
                }
                runOnUiThread(() -> {
                    // Stuff that updates the UI
                    timeUI.setText(time_stamp);
                    xUI.setText(x.concat("m/s^2"));
                    yUI.setText(y.concat("m/s^2"));
                    zUI.setText(z.concat("m/s^2"));
                    flagUI.setText(flag);
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    private void showNotification(String title, String message){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "canal_id")
                .setSmallIcon(R.drawable.alert2)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(123, builder.build());
    }
}
