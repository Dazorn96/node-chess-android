package com.dazorn.node_chess_android.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.models.User;
import com.dazorn.node_chess_android.utilities.ApplicationUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class LauncherActivity extends AppCompatActivity {

    private final int CODE_SIGN_IN = 101;
    private GoogleSignInClient _googleSignInClient;

    private ProgressBar _pbProgressIndeterminate;
    private static Context _context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        _context = this;

        getSupportActionBar().hide();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        _googleSignInClient = GoogleSignIn.getClient(this, gso);

        _pbProgressIndeterminate = findViewById(R.id.pbProgressIndeterminate);
    }

    @Override
    protected void onStart() {
        super.onStart();

        _googleSignInClient.silentSignIn().addOnCompleteListener(
                this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult(task);
                    }
                }
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CODE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();

            try {
                User user = User.getInstance();
                user.updateUser(getApplicationContext(), idToken, this);
            }
            catch (Exception ex) {
                onAuthenticationError();
            }
        }
        catch (ApiException ex) {
            Log.w("ERROR", "Sign in status: " + ex.getStatusCode());

            if(ex.getStatusCode() == 4) {
                Intent signInIntent = _googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, CODE_SIGN_IN);
            }
        }
    }

    private void onAuthenticationError() {
        _pbProgressIndeterminate.setVisibility(View.GONE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Authentication error!")
                .setTitle("Error")
                .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        restart();
                    }
                });
        builder.create().show();
    }

    private void restart() {
        ApplicationUtils.restartApplication(this);
    }

    public static void Start() {
        Handler handler = new Handler(_context.getMainLooper());

        Runnable runnnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(_context, MainActivity.class);
                Bundle options = ActivityOptionsCompat.makeCustomAnimation(_context,
                        R.anim.fade_in,
                        R.anim.fade_out).toBundle();
                _context.startActivity(intent, options);
            }
        };

        handler.post(runnnable);
    }
}
