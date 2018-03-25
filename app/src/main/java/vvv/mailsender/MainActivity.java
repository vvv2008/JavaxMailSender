package vvv.mailsender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements MailSender.OnSent, View.OnClickListener {
    private final static String TAG = "__MainActivity";

    private EditText etSubject, etText, etRecipients;
    private CheckBox cbImage, cbDocument;
    private Handler handler = new Handler();
    private ProgressDialog progressDialog = null;
    private final String sendingMail = "Sending mail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate()");
        Log.d(TAG, "MainActivity start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.action_send_via_gmail).setOnClickListener(this);
        findViewById(R.id.action_send_via_mailru).setOnClickListener(this);
        findViewById(R.id.action_send_via_yandex).setOnClickListener(this);
        etSubject = findViewById(R.id.et_subject);
        etText = findViewById(R.id.et_text);
        etRecipients = findViewById(R.id.et_recipients);
        cbImage = findViewById(R.id.cb_image);
        cbDocument = findViewById(R.id.cb_document);
    }

    /**
     * Execution in another thread
     *
     * @param isMailSent - is mail sent ?
     */
    @Override
    public void onSent(final boolean isMailSent) {
        Log.v(TAG, "onSent()");
        Log.d(TAG, (isMailSent ? "Mail sent !!!" : "Mail send ERROR"));
        handler.post(new Runnable() {
            @Override
            public void run() {
                hideProgress();
                showSentResult(isMailSent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        Log.v(TAG, "onClick()");
        showProgress();
        MailSender.ViaAccount account1 = null;
        if (view.getId() == R.id.action_send_via_gmail)
            account1 = MailSender.ViaAccount.gmail;
        else if (view.getId() == R.id.action_send_via_mailru)
            account1 = MailSender.ViaAccount.mailru;
        else if (view.getId() == R.id.action_send_via_yandex)
            account1 = MailSender.ViaAccount.yandex;
        final MailSender.ViaAccount account = account1;
        final String subject = etSubject.getText().toString();
        final String text = etText.getText().toString();
        final ArrayList<String> files = new ArrayList<>();
        String filePath;
        if (cbImage.isChecked()) {
            filePath = copyFileFromAsset("Android.png");
            if (filePath != null) files.add(filePath);
        }
        if (cbDocument.isChecked()) {
            filePath = copyFileFromAsset("LoremIpsum.odt");
            if (filePath != null) files.add(filePath);
        }
        final ArrayList<String> recipients = new ArrayList<>();
        String[] recs = etRecipients.getText().toString().split(",");
        for (String s : recs) recipients.add(s.trim());
        new Thread(new Runnable() {
            @Override
            public void run() {
                new MailSender().send(account, recipients, subject, text, files, MainActivity.this);
            }
        }).start();
    }

    private String copyFileFromAsset(String name) {
        Log.v(TAG, "copyFileFromAsset()");
        String outFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + name;
        File outFile = new File(outFilePath);
        if (outFile.exists()) return outFilePath;
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = getAssets().open(name);
            os = new FileOutputStream(outFile, false);
            byte[] buf = new byte[32 * 1024];
            int len;
            while ((len = is.read(buf)) > 0) os.write(buf, 0, len);
        } catch (Exception e) {
            Log.e(TAG, "Exception copy file", e);
            outFilePath = null;
        } finally {
            try {
                is.close();
            } catch (Exception ignore) {
            }
            try {
                os.close();
            } catch (Exception ignore) {
            }
        }
        return outFilePath;
    }

    private void showProgress() {
        Log.v(TAG, "showProgress()");
        if (progressDialog == null)
            progressDialog = ProgressDialog.show(this, sendingMail, null, true, false);
    }

    private void hideProgress() {
        Log.v(TAG, "hideProgress()");
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showSentResult(boolean isMailSent) {
        Log.v(TAG, "showSentResult()");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(sendingMail)
                .setMessage(isMailSent ? "Successful" : "ERROR")
                .setPositiveButton("Ok", null)
                .create().show();
    }
}