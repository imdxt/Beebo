
package org.zarroboogs.weibo.activity;

import org.zarroboogs.util.net.WeiboException;
import org.zarroboogs.weibo.GlobalContext;
import org.zarroboogs.weibo.R;
import org.zarroboogs.weibo.asynctask.MyAsyncTask;
import org.zarroboogs.weibo.dao.ShareShortUrlCountDao;
import org.zarroboogs.weibo.fragment.BrowserWebFragment;
import org.zarroboogs.weibo.support.lib.CheatSheet;

import com.umeng.analytics.MobclickAgent;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class BrowserWebActivity extends AbstractAppActivity {

    private Button shareCountBtn;

    private int shareCountInt;

    private String url;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("shareCountInt", shareCountInt);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equalsIgnoreCase(action)) {
            url = getIntent().getData().toString();
        } else {
            url = getIntent().getStringExtra("url");
        }

        // getActionBar().setDisplayShowHomeEnabled(false);
        // getActionBar().setDisplayShowTitleEnabled(true);
        // getActionBar().setDisplayHomeAsUpEnabled(false);

        View title = getLayoutInflater().inflate(R.layout.browserwebactivity_title_layout, null);
        shareCountBtn = (Button) title.findViewById(R.id.share_count);
        CheatSheet.setup(BrowserWebActivity.this, shareCountBtn, R.string.share_sum);
        shareCountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = BrowserShareTimeLineActivity.newIntent(url);
                startActivity(intent);
            }
        });
        // getActionBar().setCustomView(title, new ActionBar.LayoutParams(Gravity.RIGHT));
        // getActionBar().setDisplayShowCustomEnabled(true);
        //
        // getActionBar().setTitle(url);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new BrowserWebFragment(url)).commit();
            new ShareCountTask().executeOnExecutor(MyAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            shareCountInt = savedInstanceState.getInt("shareCountInt");
            shareCountBtn.setText(String.valueOf(shareCountInt));
        }

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        MobclickAgent.onPageStart(this.getClass().getName());
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPageEnd(this.getClass().getName());
        MobclickAgent.onPause(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = MainTimeLineActivity.newIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class ShareCountTask extends MyAsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... params) {
            int result = 0;
            try {
                result = new ShareShortUrlCountDao(GlobalContext.getInstance().getAccessToken(), url).getCount();
            } catch (WeiboException e) {

            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result == null) {
                return;
            }
            if (shareCountBtn == null) {
                return;
            }
            shareCountInt = result;
            shareCountBtn.setText(String.valueOf(shareCountInt));
        }
    }
}
