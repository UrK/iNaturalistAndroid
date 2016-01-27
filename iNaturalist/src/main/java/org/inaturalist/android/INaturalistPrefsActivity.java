package org.inaturalist.android;

import com.facebook.login.LoginManager;
import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


public class INaturalistPrefsActivity extends BaseFragmentActivity implements SignInTask.SignInTaskStatus {
	private static final String TAG = "INaturalistPrefsActivity";
	public static final String REAUTHENTICATE_ACTION = "reauthenticate_action";
	
    private static final int REQUEST_CODE_LOGIN = 0x1000;

    private static final String GOOGLE_AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/plus.me https://www.googleapis.com/auth/userinfo.email https://www.googleapis.com/auth/userinfo.profile";

	public static final String ACTION_RESULT_LOGOUT = "INaturalistPrefsActivity_Logout";
    
	private LinearLayout mSignInLayout;
	private LinearLayout mSignOutLayout;
	private TextView mSignOutLabel;
	private Button mSignInButton;
	private Button mSignUpButton;
	private Button mSignOutButton;
	private SharedPreferences mPreferences;
	private SharedPreferences.Editor mPrefEditor;
	private ActivityHelper mHelper;
	private INaturalistApp mApp;
	
    private int formerSelectedNetworkRadioButton;
    private int formerSelectedRadioButton;


    @Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, INaturalistApp.getAppContext().getString(R.string.flurry_api_key));
		FlurryAgent.logEvent(this.getClass().getSimpleName());
	}

	@Override
	protected void onStop()
	{
		super.onStop();		
		FlurryAgent.onEndSession(this);
	}	

    private TextView mHelp;
	private TextView mContactSupport;
	private TextView mVersion;
    
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (mApp == null) {
            mApp = (INaturalistApp) getApplicationContext();
        }


//		try {
//		    Log.d("KeyHash:", "ENTER");
//		    PackageInfo info = getPackageManager().getPackageInfo(
//		            "org.inaturalist.android", 
//		            PackageManager.GET_SIGNATURES);
//		    for (Signature signature : info.signatures) {
//		        MessageDigest md = MessageDigest.getInstance("SHA");
//		        md.update(signature.toByteArray());
//		        Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//		    }
//		} catch (NameNotFoundException e) {
//		    Log.d("NameNotFoundException: ", e.toString());
//		} catch (NoSuchAlgorithmException e) {
//		    Log.d("NoSuchAlgorithmException: ", e.toString());
//		}	
		
	    setContentView(R.layout.preferences);
	    
	    onDrawerCreate(savedInstanceState);
	    
	    mPreferences = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
	    mPrefEditor = mPreferences.edit();
	    mHelper = new ActivityHelper(this);
	    
	    
	    mSignInLayout = (LinearLayout) findViewById(R.id.signIn);
	    mSignOutLayout = (LinearLayout) findViewById(R.id.signOut);
	    mSignOutLabel = (TextView) findViewById(R.id.signOutLabel);
	    mSignInButton = (Button) findViewById(R.id.signInButton);
		mSignUpButton = (Button) findViewById(R.id.signUpButton);
	    mSignOutButton = (Button) findViewById(R.id.signOutButton);
	    mHelp = (TextView) findViewById(R.id.tutorial_link);
	    mHelp.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
	    
	    
	    mContactSupport = (TextView) findViewById(R.id.contact_support);
	    mContactSupport.setText(Html.fromHtml(mContactSupport.getText().toString()));
		mContactSupport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
				// Get app version
				try {
					PackageManager manager = INaturalistPrefsActivity.this.getPackageManager();
					PackageInfo info = manager.getPackageInfo(INaturalistPrefsActivity.this.getPackageName(), 0);

					// Open the email client
					Intent mailer = new Intent(Intent.ACTION_SEND);
					mailer.setType("message/rfc822");
					mailer.putExtra(Intent.EXTRA_EMAIL, new String[]{getString(R.string.inat_support_email_address)});
					String username = mPreferences.getString("username", null);
					mailer.putExtra(Intent.EXTRA_SUBJECT, String.format(getString(R.string.inat_support_email_subject), info.versionName, info.versionCode, username == null ? "N/A" : username));
					startActivity(Intent.createChooser(mailer, getString(R.string.send_email)));
				} catch (NameNotFoundException e) {
					e.printStackTrace();
				}
            }
        });
	    //mContactSupport.setMovementMethod(LinkMovementMethod.getInstance());
	    
	    mVersion = (TextView) findViewById(R.id.version);
	    try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			mVersion.setText(String.format("Version %s (%d)", packageInfo.versionName, packageInfo.versionCode));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			mVersion.setText("");
		}
	    
	    mHelp.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(INaturalistPrefsActivity.this, TutorialActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				intent.putExtra("first_time", false);
				startActivity(intent);
			}
		});
	    
	    toggle();
	    
        mSignInButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(INaturalistPrefsActivity.this, OnboardingActivity.class);
				intent.putExtra(OnboardingActivity.LOGIN, true);

				startActivityForResult(intent, REQUEST_CODE_LOGIN);
			}
		});

		mSignUpButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivityForResult(new Intent(INaturalistPrefsActivity.this, OnboardingActivity.class), REQUEST_CODE_LOGIN);
			}
		});

        mSignOutButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mHelper.confirm(getString(R.string.signed_out),
						getString(R.string.alert_sign_out),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								signOut();
							}
						},
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								dialogInterface.cancel();
							}
						}
				);
			}
		});

		findViewById(R.id.prefsMyProjectsButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivityIfNew(new Intent(INaturalistPrefsActivity.this, ProjectsActivity.class)
						.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
			}
		});

	    if (getIntent().getAction() != null && getIntent().getAction().equals(REAUTHENTICATE_ACTION)) {
	    	signOut();
	    	mHelper.alert(getString(R.string.username_invalid));
	    }
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    
	    mHelper = new ActivityHelper(this);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CODE_LOGIN) && (resultCode == Activity.RESULT_OK)) {
			// Refresh login state
            toggle();
			refreshUserDetails();
        }
    }


	
	private void toggle() {
	    String username = mPreferences.getString("username", null);
	    if (username == null) {
	    	mSignInLayout.setVisibility(View.VISIBLE);
	    	mSignOutLayout.setVisibility(View.GONE);

	    } else {
	    	mSignInLayout.setVisibility(View.GONE);
	    	mSignOutLayout.setVisibility(View.VISIBLE);
	    	mSignOutLabel.setText(String.format(getString(R.string.signed_in_as), username));
	    }
	}
	
	private void signOut() {
        INaturalistService.LoginType loginType = INaturalistService.LoginType.valueOf(mPreferences.getString("login_type", INaturalistService.LoginType.PASSWORD.toString()));

        if (loginType == INaturalistService.LoginType.FACEBOOK) {
            LoginManager.getInstance().logOut();
        }

        SharedPreferences prefs = getSharedPreferences("iNaturalistPreferences", MODE_PRIVATE);
        String login = prefs.getString("username", null);

		mPrefEditor.remove("username");
		mPrefEditor.remove("credentials");
		mPrefEditor.remove("password");
		mPrefEditor.remove("login_type");
        mPrefEditor.remove("last_sync_time");
		mPrefEditor.remove("observation_count");
		mPrefEditor.remove("user_icon_url");
		mPrefEditor.commit();
		
		int count1 = getContentResolver().delete(Observation.CONTENT_URI, "((_updated_at > _synced_at AND _synced_at IS NOT NULL) OR (_synced_at IS NULL))", null);
		int count2 = getContentResolver().delete(ObservationPhoto.CONTENT_URI, "((_updated_at > _synced_at AND _synced_at IS NOT NULL) OR (_synced_at IS NULL))", null);
        int count3 = getContentResolver().delete(ProjectObservation.CONTENT_URI, "(is_new = 1) OR (is_deleted = 1)", null);
        int count4 = getContentResolver().delete(ProjectFieldValue.CONTENT_URI, "((_updated_at > _synced_at AND _synced_at IS NOT NULL) OR (_synced_at IS NULL))", null);

		Log.d(TAG, String.format("Deleted %d / %d / %d / %d unsynced observations", count1, count2, count3, count4));

		toggle();
		refreshUserDetails();

		sendBroadcast(new Intent(ACTION_RESULT_LOGOUT));
	}
	
	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}	

	@Override
	public void onLoginSuccessful() {
		// Refresh the login controls
		toggle();
	}
}
