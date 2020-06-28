package com.condition.umzzal.etc;

import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler {
    private long backKeyClickTime = 0;
    private Activity activity;

    public BackPressCloseHandler(Activity activity){
        this.activity = activity;

    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyClickTime + 2000) {   //1번 눌렀을 때
            backKeyClickTime = System.currentTimeMillis();
            Toast.makeText(activity,"뒤로가기를 한 번 더 누르시면, 짤이 만들어지지 않습니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        if (System.currentTimeMillis() <= backKeyClickTime + 2000) { //연속 두번 눌렀을 때
            activity.finish();
        }

    }


}
