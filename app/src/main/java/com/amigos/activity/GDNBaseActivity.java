package com.amigos.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.amigos.R;
import com.amigos.helpers.GDNSharedPrefrences;
import com.amigos.helpers.GDNVolleySingleton;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Nirav on 27/11/2015.
 */
public abstract class GDNBaseActivity extends AppCompatActivity {

    protected DrawerLayout mDrawerLayout;

    protected void renderToolbarActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        //actionBar.setHomeAsUpIndicator(R.drawable.ic_reorder_white_18dp);
        //actionBar.setHomeAsUpIndicator(R.drawable.ic_logo_4);
        actionBar.setTitle(GDNSharedPrefrences.getCurrentService());
    }

    protected void renderChildActivityToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    protected void renderNavigationDrawer() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                Toast.makeText(getApplicationContext(), menuItem.getTitle(), Toast.LENGTH_LONG).show();
                return true;
            }
        });

        View drawerHeader = LayoutInflater.from(this).inflate(R.layout.drawer_header, navigationView);
        final TextView userName = (TextView) drawerHeader.findViewById(R.id.user_name_tv);
        userName.setText(GDNSharedPrefrences.getAcctName());
        final CircleImageView imageView = (CircleImageView) drawerHeader.findViewById(R.id.profile_image);

        ImageRequest request = new ImageRequest(GDNSharedPrefrences.getPhotUrl(),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        imageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        imageView.setImageResource(R.drawable.profile);
                    }
                });

        GDNVolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                //mDrawerLayout.openDrawer(GravityCompat.START);
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_service_selector:
                startActivity(new Intent(this,ServiceSelectorActivity.class));
                return true;
            case R.id.action_job_summary:
                startActivity(new Intent(this,JobSummaryViewActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
