/*
 * Copyright (c) 2015, 张涛.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kymjs.emojichat;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.kymjs.chat.ChatActivity;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        TextView textView = findViewById(R.id.start_text);
//        textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent();
//                intent.setClass(MainActivity.this, ChatActivity.class);
//                startActivity(intent);
//            }
//        });
        findViewById(R.id.start_text).setOnClickListener(this);
        findViewById(R.id.btn_testAPP).setOnClickListener(this);
        findViewById(R.id.btn_sipPhone).setOnClickListener(this);
    }

    public void click(View view) {
        startActivity(new Intent(this, ChatActivity.class));
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent();
        switch(view.getId()){
            case R.id.btn_testAPP:
//                intent.setAction("android.intent.action.MY");
//                intent.addCategory("android.intent.category.DEFAULT");
                ComponentName componentName = new ComponentName("com.example.testapp", "com.example.testapp.MainActivity");
                intent.setComponent(componentName);
                startActivity(intent);
                break;
            case R.id.start_text:
                Intent intent2 = new Intent(this, ChatActivity.class);
//                intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2);
                break;
            case R.id.btn_sipPhone:
                ComponentName componentName2 = new ComponentName("org.sipdroid.sipua", "org.sipdroid.sipua.ui.Sipdroid");
                Intent intent3 = new Intent();
                intent3.setComponent(componentName2);
                startActivity(intent3);
                break;
        }

    }
}
