package awesome.distresskeyord;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends Activity {

    private static final int SET_KEYWORD_CODE = 1234;
    private static final int HEARD_CODE = 5678;
    public static final int SETTINGS_RETURN = 8989;

    private TextView speechView;
    private Dialog match_text_dialog;
    private ArrayList<String> matches_text;

    private String distressKeyword = "";
    private String numberToCall = "2345432941";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton startButton = (ImageButton) findViewById(R.id.start_reg);
        speechView = (TextView)findViewById(R.id.speech);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, HEARD_CODE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Connect to Internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button setButton = (Button) findViewById(R.id.set_word_button);
        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected()) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    startActivityForResult(intent, SET_KEYWORD_CODE);
                } else {
                    Toast.makeText(getApplicationContext(), "Please Connect to Internet", Toast.LENGTH_LONG).show();
                }
            }
        });

        if (distressKeyword.isEmpty()) speechView.setText("Please set keyword!");
    }

    boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cm.getActiveNetworkInfo();
        return (net != null && net.isAvailable() && net.isConnected());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {

            if (requestCode == SET_KEYWORD_CODE) {
                matches_text = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                match_text_dialog = new Dialog(MainActivity.this);
                match_text_dialog.setContentView(R.layout.matches_fragment);
                match_text_dialog.setTitle("Select Matching Text");
                ListView textlist = (ListView) match_text_dialog.findViewById(R.id.list);

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_list_item_1, matches_text);
                textlist.setAdapter(adapter);

                    textlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            distressKeyword = matches_text.get(position);
                            match_text_dialog.hide();
                            speechView.setText("");
                        }
                    });
                match_text_dialog.show();

            } else if (requestCode == HEARD_CODE) {
                matches_text = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                for (String matched_text : matches_text) {
                    if (distressKeyword.equals(matched_text)) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + numberToCall));
                        startActivity(intent);
                    }
                }
            } else if (requestCode == SETTINGS_RETURN){
                Uri number = data.getData();
                numberToCall = number.getQueryParameter("number");
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivityForResult(intent, SETTINGS_RETURN);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
