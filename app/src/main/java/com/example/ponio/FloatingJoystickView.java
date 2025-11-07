package com.example.ponio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class FloatingJoystickView extends View {
    
    private Paint basePaint;
    private Paint stickPaint;
    private Paint borderPaint;
    
    private float centerX = 0;
    private float centerY = 0;
    private float stickX = 0;
    private float stickY = 0;
    
    private float baseRadius = 100f;
    private float stickRadius = 40f;
    private float maxDistance = 80f;
    
    private boolean isVisible = false;
    private boolean isLeft = false;
    
    private JoystickListener listener;
    
    public interface JoystickListener {
        void onJoystickMoved(float x, float y, boolean isLeft);
        void onJoystickReleased(boolean isLeft);
    }
    
    public FloatingJoystickView(Context context) {
        super(context);
        init();
    }
    
    public FloatingJoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(0x40FFFFFF); // Semi-transparent white
        basePaint.setStyle(Paint.Style.FILL);
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(0x80FFFFFF); // More opaque white
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        
        stickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        stickPaint.setColor(0xCCFFFFFF); // Almost opaque white
        stickPaint.setStyle(Paint.Style.FILL);
    }
    
    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }
    
    public void setIsLeft(boolean isLeft) {
        this.isLeft = isLeft;
    }
    
    public void show(float x, float y) {
        centerX = x;
        centerY = y;
        stickX = x;
        stickY = y;
        isVisible = true;
        invalidate();
    }
    
    public void hide() {
        isVisible = false;
        invalidate();
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (!isVisible) {
            return;
        }
        
        // Draw base circle
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        canvas.drawCircle(centerX, centerY, baseRadius, borderPaint);
        
        // Draw stick
        canvas.drawCircle(stickX, stickY, stickRadius, stickPaint);
    }
    
    public void updateStickPosition(float x, float y) {
        if (!isVisible) {
            return;
        }
        
        // Calculate distance from center
        float deltaX = x - centerX;
        float deltaY = y - centerY;
        float distance = (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        
        // Limit stick position to maxDistance
        if (distance > maxDistance) {
            float angle = (float) Math.atan2(deltaY, deltaX);
            stickX = centerX + maxDistance * (float) Math.cos(angle);
            stickY = centerY + maxDistance * (float) Math.sin(angle);
        } else {
            stickX = x;
            stickY = y;
        }
        
        // Calculate normalized joystick values (-1 to 1)
        float normalizedX = (stickX - centerX) / maxDistance;
        float normalizedY = (stickY - centerY) / maxDistance;
        
        if (listener != null) {
            listener.onJoystickMoved(normalizedX, normalizedY, isLeft);
        }
        
        invalidate();
    }
    
    public void resetStick() {
        stickX = centerX;
        stickY = centerY;
        
        if (listener != null) {
            listener.onJoystickReleased(isLeft);
        }
        
        invalidate();
    }
}
