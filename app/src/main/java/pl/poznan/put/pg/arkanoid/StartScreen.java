package pl.poznan.put.pg.arkanoid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartScreen extends Activity {

    private Button startButton;
    private Button bestResultsButton;
    private Button quitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.startButton);
        bestResultsButton = (Button) findViewById(R.id.bestResultsButton);
        quitButton = (Button) findViewById(R.id.quitButton);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreen.this, ArkanoidGame.class);
                startActivity(intent);
                finish();
            }
        });

        bestResultsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartScreen.this, BestResults.class);
                startActivity(intent);
            }
        });

        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(StartScreen.this);
                builder.setMessage("Are you sure?")
                        .setPositiveButton("Yes", quitDialogClickListener)
                        .setNegativeButton("No", quitDialogClickListener)
                        .show();
            }
        });
    }

    private DialogInterface.OnClickListener quitDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    finishAffinity();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
}
