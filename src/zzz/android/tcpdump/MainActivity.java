package zzz.android.tcpdump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity {
    private final String TAG = "zzz";
    private final String sdcardDirTcpdump = "/sdcard/tcpdump/";
    private final String linuxDirTcpdump = "/data/local/";
    private Button buttonCopy;
    private Button buttonRemove;
    private Button buttonStart;
    private Button buttonStop;
    private Process process = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonCopy = (Button) findViewById(R.id.button_copy);
        buttonRemove = (Button) findViewById(R.id.button_remove);
        buttonStart = (Button) findViewById(R.id.button_start);
        buttonStop = (Button) findViewById(R.id.button_stop);
        OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ButtonProcess(arg0);
            }
        };
        buttonCopy.setOnClickListener(onClickListener);
        buttonRemove.setOnClickListener(onClickListener);
        buttonStart.setOnClickListener(onClickListener);
        buttonStop.setOnClickListener(onClickListener);
    }

    private void ButtonProcess(View arg0) {
        if (arg0.equals(buttonCopy)) {
            Log.d(TAG, "buttonCopy clicked");

            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Log.w(TAG, "no sdcard");
                return;
            }
            String dirname = sdcardDirTcpdump;
            File filedir = new File(dirname);
            if (!filedir.exists()) {
                filedir.mkdir();
            }
            String filename = dirname + "tcpdump";
            try {
                if (!(new File(filename)).exists()) {
                    InputStream is = getAssets().open("tcpdump");
                    FileOutputStream fos = new FileOutputStream(filename);
                    byte[] buf = new byte[1024];
                    int count = 0;
                    while ((count = is.read(buf)) > 0) {
                        Log.d(TAG, buf.toString());
                        fos.write(buf, 0, count);
                    }
                    fos.close();
                    is.close();
                    Log.d(TAG, "copy finished");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // String path = "file:///android_asset/tcpdump";

            Process p = null;
            String myCommand = "su -c cp " + filename + " " + linuxDirTcpdump;

            try {
                Runtime.getRuntime().exec(myCommand);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }

        } else if (arg0.equals(buttonRemove)) {
            Log.d(TAG, "buttonRemove clicked");
            String myCommand = "su -c rm " + linuxDirTcpdump + "tcpdump";
            String myCommand2 = "rm -r " + sdcardDirTcpdump + "tcpdump";
            try {
                Runtime.getRuntime().exec(myCommand);
                Runtime.getRuntime().exec(myCommand2);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }

        } else if (arg0.equals(buttonStart)) {
            Log.d(TAG, "buttonStart clicked");
            Handler handler = new Handler();
            handler.post(runnableStartDump);

        } else if (arg0.equals(buttonStop)) {
            Log.d(TAG, "buttonStop clicked");
            process.destroy();
        }
    }

    private Runnable runnableStartDump = new Runnable() {
        public void run() {
            String myCommand = "su -c /data/local/tcpdump -p -vv -s 0 -w "
                    + sdcardDirTcpdump + "tcpdump_"
                    + System.currentTimeMillis() + ".pcap";
            try {
                process = Runtime.getRuntime().exec(myCommand);
                // BufferedReader bufferedReader = new BufferedReader(
                // new InputStreamReader(process.getInputStream()), 1024);
                // String str = null;
                // while ((str = bufferedReader.readLine()) != null) {
                // // process = Runtime.getRuntime().exec(myCommandClear);
                // Log.i(TAG, str);
                // }
                // if (str == null) {
                // Log.i(TAG, "-------is null-------");
                // }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                e.printStackTrace();
            }
        }

    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setTitle(R.string.exit)
                .setPositiveButton("y", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        process.destroy();
                        MainActivity.this.finish();
                    }
                })
                .setNegativeButton("n", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }

}
