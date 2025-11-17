package com.example.p1t2;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class Die extends AppCompatActivity {

    int delayTime = 20;
    int rollAnimations = 40;
    int[] diceImages = new int[]{R.drawable.die1, R.drawable.die2, R.drawable.die3, R.drawable.die4, R.drawable.die5, R.drawable.die6};
    Random random = new Random();
    ImageView die1;
    ImageView die2;
    int total;

    public Die(ImageView die1, ImageView die2) {
        this.die1 = die1;
        this.die2 = die2;
    }
    public interface OnDiceRollCompleteListener {
        void onDiceRollComplete(int total);
    }

    private OnDiceRollCompleteListener listener;

    public void setOnDiceRollCompleteListener(OnDiceRollCompleteListener listener) {
        this.listener = listener;
    }

    public void rollDice() {
        Runnable runnable = () -> {
            for (int i = 0; i < rollAnimations; i++) {
                int dice1 = random.nextInt(6) + 1;
                int dice2 = random.nextInt(6) + 1;
                total = dice1 + dice2;
                runOnUiThread((Runnable) () -> {
                    // Update the ImageView on the UI thread
                    die1.setImageResource(diceImages[dice1 - 1]);
                    die2.setImageResource(diceImages[dice2 - 1]);
                });
                try {
                    Thread.sleep(delayTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Notify the listener on the UI thread after rolling is complete
            runOnUiThread((Runnable) () -> {
                if (listener != null) {
                    listener.onDiceRollComplete(total);
                }
            });
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

}