package com.MartyrPher.smapiandroidinstaller;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends AppCompatActivity {

    //Tag used for debugging
    private static final String TAG = "MainActivity";

    //Override int for onRequestionPermssionResult to ensure the right permssion
    private static final int PERMISSION_REQUEST_STORAGE = 2;

    //Whether the user has permissions
    public static boolean mHasPermissions = false;

    //Whether ConfigEditorFragment has found the mods in the mod folder
    public static boolean mHasFoundMods = false;

    //TabAdapter to allow changes to the tabs
    private TabAdapter mTabAdapter;

    //TabLayout to control the tabs at the top of the screen
    private TabLayout mTabLayout;

    //ViewPager that allows switching between fragments
    private ViewPager mViewPager;

    /**
     * Override that create the initial view and sets up the layout
     * @param savedInstanceState = The savedInstanceState
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.view_pager);

            //Request the permissions for storage first.
            requestPermissions();
        }
        catch (Exception e)
        {
            writeToCrashLog(e);
        }
    }

    /**
     * Sets up the view after the app has the needed permissions.
     */
    private void setUpView()
    {
        File stardewValleyFolder = new File(Environment.getExternalStorageDirectory() + "/StardewValley/");
        try
        {
            if (!stardewValleyFolder.exists())
                stardewValleyFolder.mkdir();
        }
        catch(Exception e)
        {
            Toast.makeText(this, "Could not create Stardew Valley folder", Toast.LENGTH_LONG).show();
        }

        //Find the ViewPager and TabLayout ids
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout = findViewById(R.id.tab_layout);

        //Get the fragment manager and add the fragments
        mTabAdapter = new TabAdapter(getSupportFragmentManager());
        if (mTabAdapter.getCount() < 1 && mHasPermissions)
        {
            mTabAdapter.addFragment(new InstallFragment(), this.getString(R.string.install_button_text));
            mTabAdapter.addFragment(new ConfigEditorFragment(), this.getString(R.string.config_tab_text));
            mTabAdapter.addFragment(new GitHubFragment(), this.getString(R.string.links_tab_text));
        }

        //Setup the ViewPager adapter and TabLayout with ViewPager
        mViewPager.setAdapter(mTabAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * Writes to a text file if the app crashes.
     * @param e = The exception to be written to the log.
     */
    public static void writeToCrashLog(Exception e)
    {
        //Try to log a crash
        File smapiInstallerFolder = new File(Environment.getExternalStorageDirectory() + "/SMAPI Installer/");
        File logFile = new File(Environment.getExternalStorageDirectory() + "/SMAPI Installer/crash.txt");

        if (!mHasPermissions)
            return;

        if (!smapiInstallerFolder.exists())
            smapiInstallerFolder.mkdir();

        try {
            FileOutputStream stream = new FileOutputStream(logFile);
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            String stackTrace = stringWriter.toString();
            stream.write(stackTrace.getBytes());
            stream.close();
        }
        catch(Exception er) {
            Log.e(TAG, er.toString());
        }
    }

    /**
     * Request permissions to be able to read/write external storage
     */
    public void requestPermissions()
    {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_REQUEST_STORAGE);
        }
        else
        {
            mHasPermissions = true;
            setUpView();
        }
    }

    /**
     * Override to handle the result of the permission request
     * @param requestCode = The request code
     * @param permissions = An array of Strings for needed permissions
     * @param grantResults = The results of the permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode)
        {
            case PERMISSION_REQUEST_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    mHasPermissions = true;
                    setUpView();
                    Toast.makeText(this, R.string.permission, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}