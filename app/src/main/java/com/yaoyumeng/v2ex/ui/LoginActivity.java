package com.yaoyumeng.v2ex.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.yaoyumeng.v2ex.R;
import com.yaoyumeng.v2ex.api.HttpRequestHandler;
import com.yaoyumeng.v2ex.api.V2EXManager;
import com.yaoyumeng.v2ex.model.MemberModel;
import com.yaoyumeng.v2ex.utils.AccountUtils;
import com.yaoyumeng.v2ex.utils.MessageUtils;

import java.util.ArrayList;

/**
 * Created by yugy on 14-2-26.
 */
public class LoginActivity extends SwipeBackActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private EditText mUsername;
    private EditText mPassword;
    private Button mLogin;
    private MemberModel mProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSwipeContentView(R.layout.activity_login);

        mUsername = (EditText) findViewById(R.id.login_username_edit);
        mPassword = (EditText) findViewById(R.id.login_password_edit);
        mLogin = (Button) findViewById(R.id.login_login_btn);
        mLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_login_btn:
                if (mUsername.getText().length() == 0) {
                    mUsername.setError(getString(R.string.login_error_empty_user));
                    mUsername.requestFocus();
                } else if (mPassword.getText().length() == 0) {
                    mPassword.setError(getString(R.string.login_error_empty_passwd));
                    mPassword.requestFocus();
                } else {
                    login();
                }
                break;
            case R.id.login_sign_up_txt:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(V2EXManager.SIGN_UP_URL)));
                break;
        }
    }

    private void login() {
        showProgressBar(true, getString(R.string.login_loging));

        V2EXManager.loginWithUsername(this,
                mUsername.getText().toString(),
                mPassword.getText().toString(),
                new HttpRequestHandler<Integer>() {
                    @Override
                    public void onSuccess(Integer data) {
                        getProfile();
                    }

                    @Override
                    public void onSuccess(Integer data, int totalPages, int currentPage) {
                        getProfile();
                    }

                    @Override
                    public void onFailure(String error) {
                        MessageUtils.showErrorMessage(LoginActivity.this, error);
                        showProgressBar(false);
                    }
                });
    }

    private HttpRequestHandler<ArrayList<MemberModel>> profileHandler =
            new HttpRequestHandler<ArrayList<MemberModel>>() {
                @Override
                public void onSuccess(ArrayList<MemberModel> data) {
                    showProgressBar(false);
                    if (data.size() == 0) {
                        onFailure("");
                        return;
                    }
                    mProfile = data.get(0);
                    AccountUtils.writeLoginMember(LoginActivity.this, mProfile);
                    showProgressBar(false);
                    Intent intent = new Intent();
                    intent.putExtra("profile", (Parcelable) mProfile);
                    setResult(RESULT_OK, intent);
                    finish();
                }

                @Override
                public void onSuccess(ArrayList<MemberModel> data, int totalPages, int currentPage) {
                }

                @Override
                public void onFailure(String error) {
                    showProgressBar(false);
                    MessageUtils.showErrorMessage(LoginActivity.this, error);
                }
            };

    private void getProfile() {
        showProgressBar(true, getString(R.string.login_obtain_profile));
        V2EXManager.getProfile(this, profileHandler);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = NavUtils.getParentActivityIntent(this);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder.create(this).addNextIntentWithParentStack(upIntent).startActivities();
                } else {
                    upIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
