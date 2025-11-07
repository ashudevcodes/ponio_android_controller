package com.example.ponio;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ControllerActivity extends AppCompatActivity {

    private GamepadManager gamepadManager;
    private FloatingJoystickView leftJoystick;
    private FloatingJoystickView rightJoystick;
    private FrameLayout touchAreaLeft;
    private FrameLayout touchAreaRight;
    
    private int activeLeftPointerId = -1;
    private int activeRightPointerId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.controllor_screen);

        gamepadManager = GamepadManager.getInstance();

        setupFullscreen();
        
        initializeControllerView();
        setupGamepadControls();
        setupFloatingJoysticks();
        
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
        
        touchAreaLeft = findViewById(R.id.touchAreaLeft);
        touchAreaRight = findViewById(R.id.touchAreaRight);
        leftJoystick = findViewById(R.id.leftJoystick);
        rightJoystick = findViewById(R.id.rightJoystick);
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
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupFloatingJoysticks() {
        leftJoystick.setIsLeft(true);
        rightJoystick.setIsLeft(false);
        
        leftJoystick.setJoystickListener(new FloatingJoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float x, float y, boolean isLeft) {
                if (!gamepadManager.isConnected) return;
                gamepadManager.sendCommand(String.format("LJOY:%.3f,%.3f", x, y));
            }

            @Override
            public void onJoystickReleased(boolean isLeft) {
                if (!gamepadManager.isConnected) return;
                gamepadManager.sendCommand("LJOY:0.000,0.000");
            }
        });
        
        rightJoystick.setJoystickListener(new FloatingJoystickView.JoystickListener() {
            @Override
            public void onJoystickMoved(float x, float y, boolean isLeft) {
                if (!gamepadManager.isConnected) return;
                gamepadManager.sendCommand(String.format("RJOY:%.3f,%.3f", x, y));
            }

            @Override
            public void onJoystickReleased(boolean isLeft) {
                if (!gamepadManager.isConnected) return;
                gamepadManager.sendCommand("RJOY:0.000,0.000");
            }
        });
        
        // Setup touch area for left joystick
        touchAreaLeft.setOnTouchListener((v, event) -> {
            if (!gamepadManager.isConnected) {
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (activeLeftPointerId == -1) {
                        activeLeftPointerId = pointerId;
                        leftJoystick.show(event.getX(pointerIndex), event.getY(pointerIndex));
                    }
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    if (activeLeftPointerId != -1) {
                        int index = event.findPointerIndex(activeLeftPointerId);
                        if (index != -1) {
                            leftJoystick.updateStickPosition(event.getX(index), event.getY(index));
                        }
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (pointerId == activeLeftPointerId) {
                        activeLeftPointerId = -1;
                        leftJoystick.resetStick();
                        leftJoystick.hide();
                    }
                    return true;
                    
                case MotionEvent.ACTION_CANCEL:
                    if (activeLeftPointerId != -1) {
                        activeLeftPointerId = -1;
                        leftJoystick.resetStick();
                        leftJoystick.hide();
                    }
                    return true;
            }
            return false;
        });
        
        // Setup touch area for right joystick
        touchAreaRight.setOnTouchListener((v, event) -> {
            if (!gamepadManager.isConnected) {
                Toast.makeText(this, "Connection lost", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            int pointerIndex = event.getActionIndex();
            int pointerId = event.getPointerId(pointerIndex);
            
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (activeRightPointerId == -1) {
                        activeRightPointerId = pointerId;
                        rightJoystick.show(event.getX(pointerIndex), event.getY(pointerIndex));
                    }
                    return true;
                    
                case MotionEvent.ACTION_MOVE:
                    if (activeRightPointerId != -1) {
                        int index = event.findPointerIndex(activeRightPointerId);
                        if (index != -1) {
                            rightJoystick.updateStickPosition(event.getX(index), event.getY(index));
                        }
                    }
                    return true;
                    
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (pointerId == activeRightPointerId) {
                        activeRightPointerId = -1;
                        rightJoystick.resetStick();
                        rightJoystick.hide();
                    }
                    return true;
                    
                case MotionEvent.ACTION_CANCEL:
                    if (activeRightPointerId != -1) {
                        activeRightPointerId = -1;
                        rightJoystick.resetStick();
                        rightJoystick.hide();
                    }
                    return true;
            }
            return false;
        });
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
