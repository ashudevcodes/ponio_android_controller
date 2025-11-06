package com.example.ponio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ControllerActivity extends AppCompatActivity {

    private GamepadManager gamepadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controllor_screen);

        gamepadManager = GamepadManager.getInstance();

        setupFullscreen();
        
        initializeControllerView();
        setupGamepadControls();
        
        if (gamepadManager.isConnected) {
            gamepadManager.enableGamepadControls(true);
        } else {
            Toast.makeText(this, "Not connected to server", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupFullscreen() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void initializeControllerView() {
        gamepadManager.btnA = findViewById(R.id.btnA);
        gamepadManager.btnB = findViewById(R.id.btnB);
        gamepadManager.btnX = findViewById(R.id.btnX);
        gamepadManager.btnY = findViewById(R.id.btnY);

        gamepadManager.btnL1 = findViewById(R.id.btnL1);
        gamepadManager.btnR1 = findViewById(R.id.btnR1);
        gamepadManager.btnL2 = findViewById(R.id.btnL2);
        gamepadManager.btnR2 = findViewById(R.id.btnR2);

        gamepadManager.btnStart = findViewById(R.id.btnStart);
        gamepadManager.btnSelect = findViewById(R.id.btnSelect);

        gamepadManager.btnDpadUp = findViewById(R.id.btnDpadUp);
        gamepadManager.btnDpadDown = findViewById(R.id.btnDpadDown);
        gamepadManager.btnDpadLeft = findViewById(R.id.btnDpadLeft);
        gamepadManager.btnDpadRight = findViewById(R.id.btnDpadRight);

        gamepadManager.leftJoystick = findViewById(R.id.leftJoystick);
        gamepadManager.rightJoystick = findViewById(R.id.rightJoystick);
    }

    private void setupGamepadControls() {
        setupButton(gamepadManager.btnA, "BTN_A");
        setupButton(gamepadManager.btnB, "BTN_B");
        setupButton(gamepadManager.btnX, "BTN_X");
        setupButton(gamepadManager.btnY, "BTN_Y");

        setupButton(gamepadManager.btnL1, "BTN_L1");
        setupButton(gamepadManager.btnR1, "BTN_R1");
        setupButton(gamepadManager.btnL2, "BTN_L2");
        setupButton(gamepadManager.btnR2, "BTN_R2");

        setupButton(gamepadManager.btnStart, "BTN_START");
        setupButton(gamepadManager.btnSelect, "BTN_SELECT");

        setupButton(gamepadManager.btnDpadUp, "DPAD_UP");
        setupButton(gamepadManager.btnDpadDown, "DPAD_DOWN");
        setupButton(gamepadManager.btnDpadLeft, "DPAD_LEFT");
        setupButton(gamepadManager.btnDpadRight, "DPAD_RIGHT");

        setupJoystick(gamepadManager.leftJoystick, true);
        setupJoystick(gamepadManager.rightJoystick, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupButton(MaterialButton button, String buttonName) {
        button.setOnTouchListener((v, event) -> {
            if (!gamepadManager.isConnected) {
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    gamepadManager.sendCommand(buttonName + ":1");
                    button.setPressed(true);
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    gamepadManager.sendCommand(buttonName + ":0");
                    button.setPressed(false);
                    return true;
            }
            return false;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupJoystick(View joystick, boolean isLeft) {
        joystick.setOnTouchListener((v, event) -> {
            if (!gamepadManager.isConnected) {
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                return false;
            }

            float x = event.getX() / v.getWidth();
            float y = event.getY() / v.getHeight();

            x = Math.max(0, Math.min(1, x));
            y = Math.max(0, Math.min(1, y));

            float joyX = (x - 0.5f) * 2.0f;
            float joyY = (y - 0.5f) * 2.0f;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    if (isLeft) {
                        gamepadManager.leftJoyX = x;
                        gamepadManager.leftJoyY = y;
                        gamepadManager.sendCommand(String.format("LJOY:%.3f,%.3f", joyX, joyY));
                    } else {
                        gamepadManager.rightJoyX = x;
                        gamepadManager.rightJoyY = y;
                        gamepadManager.sendCommand(String.format("RJOY:%.3f,%.3f", joyX, joyY));
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Reset to center
                    if (isLeft) {
                        gamepadManager.sendCommand("LJOY:0.000,0.000");
                        gamepadManager.leftJoyX = 0.5f;
                        gamepadManager.leftJoyY = 0.5f;
                    } else {
                        gamepadManager.sendCommand("RJOY:0.000,0.000");
                        gamepadManager.rightJoyX = 0.5f;
                        gamepadManager.rightJoyY = 0.5f;
                    }
                    return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFullscreen();
        
        if (!gamepadManager.isConnected) {
            Toast.makeText(this, "Connection lost. Returning to main screen.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
