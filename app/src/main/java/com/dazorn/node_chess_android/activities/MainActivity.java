package com.dazorn.node_chess_android.activities;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dazorn.node_chess_android.R;
import com.dazorn.node_chess_android.adapters.ScreenSliderPagerAdapter;
import com.dazorn.node_chess_android.helpers.SocketHelper;
import com.dazorn.node_chess_android.utilities.ApplicationUtils;

import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.emitter.Emitter;

public class MainActivity extends FragmentActivity {
    private ViewPager2 _viewPager;
    private FragmentStateAdapter _pagerAdapter;

    private LinearLayout _menu;
    private int _lastMenuPosition = 2;

    private ImageView _logo;

    private static Activity _this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _this = this;

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        _viewPager = findViewById(R.id.pager);
        _pagerAdapter = new ScreenSliderPagerAdapter(this);
        _viewPager.setAdapter(_pagerAdapter);
        _viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                setCurrentFragment(position);
                super.onPageSelected(position);
            }
        });

        _menu = findViewById(R.id.menu);

        for(int i = 0; i <= _menu.getChildCount(); i++) {
            ImageButton b = (ImageButton) _menu.getChildAt(i);

            if(b == null) {
                continue;
            }

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = _menu.indexOfChild(view);
                    _viewPager.setCurrentItem(position, true);
                }
            });
        }

        final ViewTreeObserver observer = _menu.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = calcAvailableHeight();
                _viewPager.setLayoutParams(new LinearLayout.LayoutParams(_viewPager.getWidth(), height));
                _menu.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                _viewPager.setCurrentItem(2, true);
            }
        });

        _logo = findViewById(R.id.logo);

        ApplicationUtils.SetRestartedComplete();
    }

    private int calcAvailableHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.heightPixels - _menu.getHeight() - _logo.getHeight() - (int)(50 / getApplicationContext().getResources().getDisplayMetrics().density);
    }

    public void setCurrentFragment(int position) {
        if(_menu != null) {
            ImageButton b = (ImageButton) _menu.getChildAt(position);
            b.setColorFilter(Color.parseColor("#ffc107"));

            b = (ImageButton) _menu.getChildAt(_lastMenuPosition);
            b.setColorFilter(Color.parseColor("#4d4d4d"));

            _lastMenuPosition = position;
        }
    }

    public static void HideKeyboard(final View view) {
        Handler handler = new Handler(_this.getMainLooper());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                InputMethodManager manager = (InputMethodManager) _this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        };

        handler.post(runnable);
    }
}
