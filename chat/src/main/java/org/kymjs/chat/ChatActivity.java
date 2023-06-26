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
package org.kymjs.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.kymjs.chat.adapter.ChatAdapter;
import org.kymjs.chat.bean.ChatMessage;
import org.kymjs.chat.bean.Emojicon;
import org.kymjs.chat.bean.Faceicon;
import org.kymjs.chat.emoji.DisplayRules;
import org.kymjs.chat.widget.KJChatKeyboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * 聊天主界面
 */
public class ChatActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_GETIMAGE_BYSDCARD = 0x1;
//    private static final String FROM_USER = "Jerry";
    private static final String FROM_USER = "Tom";
//    private static final String TO_USER = "Tom";
    private static final String TO_USER = "Jerry";

    private KJChatKeyboard box;
    private ListView mRealListView;

    List<ChatMessage> datas = new ArrayList<ChatMessage>();
    private ChatAdapter adapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        box = (KJChatKeyboard) findViewById(R.id.chat_msg_input_box);
        mRealListView = (ListView) findViewById(R.id.chat_listview);
        mRealListView.setSelector(android.R.color.transparent);
        initMessageInputToolBox();
        initListView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initMessageInputToolBox() {
        box.setOnOperationListener(new OnOperationListener() {
            @Override
            public void send(ChatMessage chatMessage) {
                datas.add(chatMessage);
                adapter.refresh(datas);
            }

            @Override
            public void receive(ChatMessage chatMessage) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100 * (new Random().nextInt(3) + 1));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    datas.add(chatMessage);
                                    adapter.refresh(datas);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void selectedFace(Faceicon content) {
                ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_FACE, ChatMessage.MSG_STATE_SUCCESS,
                        FROM_USER, "avatar", TO_USER, "avatar", content.getPath(), true, true, new
                        Date());
                datas.add(chatMessage);
                adapter.refresh(datas);
            }

            @Override
            public void selectedEmoji(Emojicon emoji) {
                box.getEditTextBox().append(emoji.getValue());
            }

            @Override
            public void selectedBackSpace(Emojicon back) {
                DisplayRules.backspace(box.getEditTextBox());
            }

            @Override
            public void selectedFunction(int index) {
                switch (index) {
                    case 0:
                        goToAlbum();
                        break;
                    case 1:
                        Toast.makeText(getApplication(), "跳转相机，只做演示", Toast.LENGTH_SHORT).show();
                        goToCamara();
                        break;
                    case 2:
                        ComponentName componentName2 = new ComponentName("org.sipdroid.sipua", "org.sipdroid.sipua.ui.Sipdroid");
                        Intent intent3 = new Intent();
                        intent3.setComponent(componentName2);
                        startActivity(intent3);
                        break;
                }
            }
        });

        List<String> faceCagegory = new ArrayList<>();
        File faceList = new File("");
        if (faceList.isDirectory()) {
            File[] faceFolderArray = faceList.listFiles();
            for (File folder : faceFolderArray) {
                if (!folder.isHidden()) {
                    faceCagegory.add(folder.getAbsolutePath());
                }
            }
        }

        box.setFaceData(faceCagegory);
        mRealListView.setOnTouchListener(getOnTouchListener());
    }

    private void initListView() {
        byte[] emoji = new byte[]{
                (byte) 0xF0, (byte) 0x9F, (byte) 0x98, (byte) 0x81
        };
        ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_TEXT,
                ChatMessage.MSG_STATE_SUCCESS, "\ue415", "avatar", FROM_USER, "avatar",
                new String(emoji), false, true, new Date(System.currentTimeMillis()
                - (1000 * 60 * 60 * 24) * 8));

//        ChatMessage chatMessage7 = new ChatMessage(ChatMessage.MSG_TYPE_TEXT,
//                ChatMessage.MSG_STATE_SENDING, TO_USER, "avatar", FROM_USER, "avatar",
//                "<a href=\"http://kymjs.com\">]自定义链接</a>也是支持的kkkk" + new Date(System.currentTimeMillis()), true, true, new Date(System.currentTimeMillis()));
//
        datas.add(chatMessage);
//        datas.add(chatMessage7);

        adapter = new ChatAdapter(this, datas, getOnChatItemClickListener());
        mRealListView.setAdapter(adapter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && box.isShow()) {
            box.hideLayout();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * 跳转到选择相册界面
     */
    private void goToAlbum() {
        Intent intent;
        intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "选择图片"),
                REQUEST_CODE_GETIMAGE_BYSDCARD);

    }
    private static int REQUEST_CAMERA=1;
    private void goToCamara(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 启动系统相机
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_GETIMAGE_BYSDCARD) {
            Uri dataUri = data.getData();
            if (dataUri != null) {
                File file = FileUtils.uri2File(ChatActivity.this, dataUri);
                ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_PHOTO, ChatMessage.MSG_STATE_SUCCESS,
                        FROM_USER, "avatar", TO_USER,
                        "avatar", file.getAbsolutePath(), true, true, new Date());
                datas.add(chatMessage);
                adapter.refresh(datas);
            }
        }
    }

    /**
     * 若软键盘或表情键盘弹起，点击上端空白处应该隐藏输入法键盘
     *
     * @return 会隐藏输入法键盘的触摸事件监听器
     */
    private View.OnTouchListener getOnTouchListener() {
        return new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                box.hideLayout();
                box.hideKeyboard(ChatActivity.this);
                return false;
            }
        };
    }

    /**
     * @return 聊天列表内存点击事件监听器
     */
    private OnChatItemClickListener getOnChatItemClickListener() {
        return new OnChatItemClickListener() {
            @Override
            public void onPhotoClick(int position) {
                Log.d("debug", datas.get(position).getContent() + "点击图片的");
                Toast.makeText(ChatActivity.this, datas.get(position).getContent() + "点击图片的", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTextClick(int position) {
                Toast.makeText(ChatActivity.this, datas.get(position).getContent(), Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFaceClick(int position) {
            }
        };
    }

    /**
     * 聊天列表中对内容的点击事件监听
     */
    public interface OnChatItemClickListener {
        void onPhotoClick(int position);

        void onTextClick(int position);

        void onFaceClick(int position);
    }
}
