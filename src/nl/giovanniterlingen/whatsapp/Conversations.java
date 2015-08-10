package nl.giovanniterlingen.whatsapp;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

/**
 * Android adaptation from the PHP WhatsAPI by WHAnonymous {@link https
 * ://github.com/WHAnonymous/Chat-API/}
 * 
 * @author Giovanni Terlingen
 */
public class Conversations extends ActionBarActivity {

	private SQLiteDatabase newDB;
	Button sButton;
	String nEdit;
	EditText mEdit;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.conversations);

		sButton = (Button) findViewById(R.id.send_button);
		mEdit = (EditText) findViewById(R.id.message_text);

		Intent intent = getIntent();
		if (intent.hasExtra("numberpass")) {
			String number = intent.getExtras().getString("numberpass");
			nEdit = number;
		}

		getMessages();

		String contactname = ContactsHelper.getContactName(Conversations.this,
				nEdit);

		setTitle(contactname);

		sButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				String to = nEdit.toString();
				String str = to.replaceAll("\\D+", "");
				String message = mEdit.getText().toString();

				if (message.isEmpty()) {
					return;

				} else {
					Intent i = new Intent();
					i.setAction(MessageService.ACTION_SEND_MSG);
					i.putExtra("to", str);
					i.putExtra("msg", message);
					sendBroadcast(i);
					getMessages();
					mEdit.setText("");
				}
			}
		});

		mEdit.addTextChangedListener(new TextWatcher() {

			String to = nEdit.toString();
			String str = to.replaceAll("\\D+", "");

			private Timer timer = new Timer();
			private final long DELAY = 2000;

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				Intent i = new Intent();
				i.setAction(MessageService.ACTION_START_COMPOSING);
				i.putExtra("to", str);
				sendBroadcast(i);

			}

			@Override
			public void afterTextChanged(Editable s) {
				
				timer.cancel();
				timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {

						Intent i = new Intent();
						i.setAction(MessageService.ACTION_STOP_COMPOSING);
						i.putExtra("to", str);
						sendBroadcast(i);
					}

				}, DELAY);
			}

		});
	}

	public void getMessages() {

		DatabaseHelper dbHelper = new DatabaseHelper(
				this.getApplicationContext());
		newDB = dbHelper.getWritableDatabase();

		List<String> messages = dbHelper.getMessages(newDB, nEdit);

		String[] array = messages.toArray(new String[messages.size()]);
		
		ListView lv = (ListView) findViewById(R.id.listview);
		
		lv.setDivider(null);
		
		ChatAdapter adapter = new ChatAdapter(Conversations.this, array, nEdit);
		 
		lv.setAdapter(adapter);

	}
	
	protected void onResume() {
		super.onResume();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_ONLINE);
		sendBroadcast(i);
	}

	protected void onPause() {
		super.onPause();
		Intent i = new Intent();
		i.setAction(MessageService.ACTION_SHOW_OFFLINE);
		sendBroadcast(i);
	}

	public void onBackPressed() {
		Intent intent = new Intent(this, Main.class);
		startActivity(intent);
		finish();
		return;
	}

}
