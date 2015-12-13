package com.crash.lib.uuxia.sample;

import android.app.Application;

import com.crash.lib.uuxia.library.CrashHandler;
import com.crash.lib.uuxia.library.Email;

/**
 * Created by uuxia-mac on 15/12/13.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
//		Email.create("263996097@qq.com");
        Email mail = Email.create("smtp.163.com", "263996097@qq.com", "sxmobi_1@163.com", "het123457");
        crashHandler.setEmail(mail);
        crashHandler.init(getApplicationContext());
    }
}
