package com.example.pelaporanbakesbangpolmalang;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.pelaporanbakesbangpolmalang.fragment.HomeFragment;
import com.example.pelaporanbakesbangpolmalang.fragment.LaporanFragment;
import com.example.pelaporanbakesbangpolmalang.fragment.PasswordFragment;
import com.example.pelaporanbakesbangpolmalang.fragment.PemberitahuanFragment;
import com.example.pelaporanbakesbangpolmalang.fragment.ProfileFragment;
import com.example.pelaporanbakesbangpolmalang.fragment.UsersFragment;
import com.example.pelaporanbakesbangpolmalang.helper.ApiHelper;
import com.example.pelaporanbakesbangpolmalang.helper.SessionHelper;
import com.example.pelaporanbakesbangpolmalang.helper.VolleyHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static java.security.AccessController.getContext;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";
    private static final String CHANNEL_ID = "101";
    SessionHelper sessionHelper;
    RequestQueue requestQueue;

    DrawerLayout drawer;
    Toolbar toolbar;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    View headerNav;
    TextView usernameNav, emailNav, menuPemberitahuanCounter;

    CircleImageView imageProfil;

    boolean doubleBackToExit;

    String idUser, email, username, level, image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // fungsi menginisialisasi menu
        init(savedInstanceState);

        getUserById(idUser);

        getPemberitahuanCount(idUser);

        getToken();
        createNotificationChannel();
        subscribeToTopic();

    }

    private void init(Bundle savedInstanceState) {
        sessionHelper = new SessionHelper(this);
        requestQueue = VolleyHelper.getInstance(this).getRequestQueue();

        idUser = sessionHelper.getIdUser();
        level = sessionHelper.getLevel();

        // setup toolbar
        toolbar = findViewById(R.id.toolbar_admin);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Home");

        // setup navigation drawer
        drawer = findViewById(R.id.drawer_layout_admin);

        navigationView = findViewById(R.id.nav_view_admin);

        // jika user login
        if (level.equals("2")) {
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.drawer_menu_user);
        }

        navigationView.setNavigationItemSelectedListener(this);

        menuPemberitahuanCounter = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().
                findItem(R.id.menu_pemberitahuan));

        headerNav = navigationView.getHeaderView(0);
        imageProfil = headerNav.findViewById(R.id.nav_image_profile);
        usernameNav = headerNav.findViewById(R.id.nav_username);
        emailNav = headerNav.findViewById(R.id.nav_email);

        // setup tombol burgerm menu
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Intent intent = getIntent();
        if (intent.hasExtra("GOTO_FRAGMENT")) {
            String activeFragment = getIntent().getStringExtra("GOTO_FRAGMENT");

            if (activeFragment.contentEquals("LAPORAN")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new LaporanFragment()).commit();
                getSupportActionBar().setTitle("Laporan");
                navigationView.setCheckedItem(R.id.menu_laporan);
            }

            if (activeFragment.contentEquals("PENGGUNA")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new UsersFragment()).commit();
                getSupportActionBar().setTitle("Pengguna");
                navigationView.setCheckedItem(R.id.menu_users);
            }

            if (activeFragment.contentEquals("PEMBERITAHUAN")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new PemberitahuanFragment()).commit();
                getSupportActionBar().setTitle("Pemberitahuan");
                navigationView.setCheckedItem(R.id.menu_pemberitahuan);
            }

            if (activeFragment.contentEquals("HOME")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new HomeFragment()).commit();
                getSupportActionBar().setTitle("Home");
                navigationView.setCheckedItem(R.id.menu_home);
            }
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new HomeFragment()).commit();
                getSupportActionBar().setTitle("Home");
                navigationView.setCheckedItem(R.id.menu_home);
            }
        }
    }

    private void initializeCountDrawer(String count) {
        //Gravity property aligns the text
        menuPemberitahuanCounter.setGravity(Gravity.CENTER_VERTICAL);
        menuPemberitahuanCounter.setTypeface(null, Typeface.BOLD);
        menuPemberitahuanCounter.setTextColor(getResources().getColor(R.color.colorDanger));

        if (count.equals("0") || count.isEmpty()) {
            menuPemberitahuanCounter.setText("");
        } else {
            menuPemberitahuanCounter.setText(count);
        }
    }

    private void getUserById(String id) {
        StringRequest getUser = new StringRequest(Request.Method.GET, ApiHelper.USER_BY_ID_USER + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.getString("status").equals("false")) {
                        Toast.makeText(HomeActivity.this, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    JSONObject data = responseObject.getJSONObject("data");

                    email = data.getString("email");
                    username = data.getString("username");
                    image = ApiHelper.ASSETS_URL + data.getString("foto");

                    Glide.with(HomeActivity.this).load(image).into(imageProfil);

                    usernameNav.setText(username);
                    emailNav.setText(email);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "Terjadi error. Coba beberapa saat lagi.";

                if (error instanceof NetworkError) {
                    message = "Tidak dapat terhubung ke internet. Harap periksa koneksi anda.";
                } else if (error instanceof AuthFailureError) {
                    message = "Gagal login. Harap periksa email dan password anda.";
                } else if (error instanceof ClientError) {
                    message = "Gagal login. Harap periksa email dan password anda.";
                } else if (error instanceof NoConnectionError) {
                    message = "Tidak ada koneksi internet. Harap periksa koneksi anda.";
                } else if (error instanceof TimeoutError) {
                    message = "Connection Time Out. Harap periksa koneksi anda.";
                }

                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(getUser);
    }

    private void getPemberitahuanCount(String id) {
        StringRequest getPemberitahuan = new StringRequest(Request.Method.GET, ApiHelper.PEMBERITAHUAN_COUNT_BY_ID_PENERIMA + id, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseObject = new JSONObject(response);

                    if (responseObject.getString("status").equals("false")) {
//                        Toast.makeText(HomeActivity.this, responseObject.getString("message"), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String count = responseObject.getString("count");

                    initializeCountDrawer(count);
                } catch (Exception e) {
                    Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String message = "Terjadi error. Coba beberapa saat lagi.";

                if (error instanceof NetworkError) {
                    message = "Tidak dapat terhubung ke internet. Harap periksa koneksi anda.";
                } else if (error instanceof AuthFailureError) {
                    message = "Gagal login. Harap periksa email dan password anda.";
                } else if (error instanceof ClientError) {
                    message = "Gagal login. Harap periksa email dan password anda.";
                } else if (error instanceof NoConnectionError) {
                    message = "Tidak ada koneksi internet. Harap periksa koneksi anda.";
                } else if (error instanceof TimeoutError) {
                    message = "Connection Time Out. Harap periksa koneksi anda.";
                }

                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });

        requestQueue.add(getPemberitahuan);
    }

    // handling tombol back
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExit) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExit = true;
            Snackbar.make(findViewById(R.id.drawer_layout_admin), "Tekan kembali sekali lagi untuk keluar", Snackbar.LENGTH_LONG).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExit = false;
                }
            }, 2000);
        }
    }

    // handling menu navigation selected
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new HomeFragment()).commit();
                getSupportActionBar().setTitle("Home");
                navigationView.setCheckedItem(R.id.menu_home);
                break;
            case R.id.menu_laporan:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new LaporanFragment()).commit();
                getSupportActionBar().setTitle("Laporan");
                navigationView.setCheckedItem(R.id.menu_laporan);
                break;
            case R.id.menu_users:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new UsersFragment()).commit();
                getSupportActionBar().setTitle("Pengguna");
                navigationView.setCheckedItem(R.id.menu_users);
                break;
            case R.id.menu_pemberitahuan:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new PemberitahuanFragment()).commit();
                getSupportActionBar().setTitle("Pemberitahuan");
                navigationView.setCheckedItem(R.id.menu_users);
                break;
            case R.id.menu_profile:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new ProfileFragment()).commit();
                getSupportActionBar().setTitle("Profil");
                navigationView.setCheckedItem(R.id.menu_profile);
                break;
            case R.id.menu_password:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container_admin, new PasswordFragment()).commit();
                getSupportActionBar().setTitle("Rubah Password");
                navigationView.setCheckedItem(R.id.menu_password);
                break;
            case R.id.menu_logout:
                if (sessionHelper.logout()) {
                    Logout();
//                    Intent toLogin = new Intent(HomeActivity.this, LoginActivity.class);
//                    startActivity(toLogin);
                    finish();
                } else {
                    Snackbar.make(findViewById(R.id.drawer_layout_admin), "Gagal memproses logout", Snackbar.LENGTH_SHORT).show();
                }
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void Logout() {

        StringRequest logoutRequest = new StringRequest(Request.Method.POST, ApiHelper.LOGOUT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    Log.e("asd", "res"+jsonObject);

                    if (jsonObject.getString("status").equals("false")) {
                        Snackbar.make(findViewById(R.id.drawer_layout_admin), jsonObject.getString("message"), Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    sessionHelper.getInstance(getApplicationContext()).logout();
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("volley", "errornya : " + error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("id_user", idUser);
                return params;
            }
        };
        requestQueue.add(logoutRequest);
    }

    private void getToken(){
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Log.e("Token", instanceIdResult.getToken());
                    }
                });
    }

    //create notif channel
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "firebaseNotifChannel";
            String description = "Receive Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void subscribeToTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("news")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(HomeActivity.this, "Selamat datang", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}