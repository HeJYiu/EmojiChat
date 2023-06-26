package org.kymjs.chat.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import org.kymjs.chat.OnOperationListener;
import org.kymjs.chat.R;
import org.kymjs.chat.SoftKeyboardStateHelper;
import org.kymjs.chat.adapter.FaceCategroyAdapter;
import org.kymjs.chat.bean.ChatMessage;
import org.kymjs.chat.bean.Request;
import org.kymjs.chat.bean.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 控件主界面
 */
public class KJChatKeyboard extends RelativeLayout implements
        SoftKeyboardStateHelper.SoftKeyboardStateListener {

    //    private static final String FROM_USER = "Jerry";
    private static final String FROM_USER = "Tom";
    //    private static final String TO_USER = "Tom";
    private static final String TO_USER = "Jerry";


    public interface OnToolBoxListener {
        void onShowFace();
    }

    public static final int LAYOUT_TYPE_HIDE = 0;
    public static final int LAYOUT_TYPE_FACE = 1;
    public static final int LAYOUT_TYPE_MORE = 2;

    /**
     * 最上层输入框
     */
    private EditText mEtMsg;
    private CheckBox mBtnFace;
    private CheckBox mBtnMore;
    private Button mBtnSend;

    /**
     * 表情
     */
    private ViewPager mPagerFaceCagetory;
    private RelativeLayout mRlFace;
    private PagerSlidingTabStrip mFaceTabs;

    private int layoutType = LAYOUT_TYPE_HIDE;
    private FaceCategroyAdapter adapter;  //点击表情按钮时的适配器

    private List<String> mFaceData;

    private Context context;
    private OnOperationListener listener;
    private OnToolBoxListener mFaceListener;
    private SoftKeyboardStateHelper mKeyboardHelper;

    private Socket sendSocket;
    private Socket receiveSocket;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    InputStream inputStream;
    ObjectInputStream objectInputStream;
    InputStreamReader inputStreamReader;
    BufferedReader bufferedReader;

    OutputStream outputStream;
    ObjectOutputStream objectOutputStream;

    public KJChatKeyboard(Context context) {
        super(context);
        init(context);
    }

    public KJChatKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KJChatKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View root = View.inflate(context, R.layout.chat_tool_box, null);
        this.addView(root);
        mThreadPool = Executors.newCachedThreadPool();
        connectToServer();
        ReceiveMsg thread = new ReceiveMsg();
        thread.start();
    }

    private void connectToServer(){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    sendSocket = new Socket("10.28.132.56", 8990);
                    outputStream = sendSocket.getOutputStream();
                    objectOutputStream = new ObjectOutputStream(outputStream);
                    ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_TEXT,
                            ChatMessage.MSG_STATE_SUCCESS, FROM_USER, "avatar", TO_USER, "avatar",
                            FROM_USER, true, true, new Date(System.currentTimeMillis()));

                    Request request = new Request();
                    request.setAction("chat");
                    request.setAttribute("msg", chatMessage);
                    objectOutputStream.writeObject(request);
                    objectOutputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private class ReceiveMsg extends Thread{
        @Override
        public void run(){
            try {
                receiveSocket = new Socket("10.28.132.56", 8991);
                OutputStream outputStream1 = receiveSocket.getOutputStream();
                ObjectOutputStream objectOutputStream1 = new ObjectOutputStream(outputStream1);
                ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_TEXT,
                        ChatMessage.MSG_STATE_SUCCESS, FROM_USER, "avatar", TO_USER, "avatar",
                        FROM_USER, true, true, new Date(System.currentTimeMillis()));

                Request request = new Request();
                request.setAction("chat");
                request.setAttribute("msg", chatMessage);
                objectOutputStream1.writeObject(request);
                objectOutputStream1.flush();


                inputStream = receiveSocket.getInputStream();
                objectInputStream = new ObjectInputStream(inputStream);
                while(true){
                    Response response = (Response) objectInputStream.readObject();
                    listener.receive((ChatMessage) response.getData("txtMsg"));
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initData();
        this.initWidget();
    }

    private void initData() {
        mKeyboardHelper = new SoftKeyboardStateHelper(((Activity) getContext())
                .getWindow().getDecorView());
        mKeyboardHelper.addSoftKeyboardStateListener(this);
    }

    private void initWidget() {
        mEtMsg = (EditText) findViewById(R.id.toolbox_et_message);
        mBtnSend = (Button) findViewById(R.id.toolbox_btn_send);
        mBtnFace = (CheckBox) findViewById(R.id.toolbox_btn_face);
        mBtnMore = (CheckBox) findViewById(R.id.toolbox_btn_more);
        mRlFace = (RelativeLayout) findViewById(R.id.toolbox_layout_face);
        mPagerFaceCagetory = (ViewPager) findViewById(R.id.toolbox_pagers_face);
        mFaceTabs = (PagerSlidingTabStrip) findViewById(R.id.toolbox_tabs);
        adapter = new FaceCategroyAdapter(((FragmentActivity) getContext())
                .getSupportFragmentManager(), LAYOUT_TYPE_FACE);
        mBtnSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (listener != null) {
                    final String content = mEtMsg.getText().toString();
                    ChatMessage chatMessage = new ChatMessage(ChatMessage.MSG_TYPE_TEXT,
                            ChatMessage.MSG_STATE_SUCCESS, FROM_USER, "avatar", TO_USER, "avatar",
                            content, true, true, new Date(System.currentTimeMillis()));
                    if(!"".equals(content)) {
                        mThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Request request = new Request();
                                    request.setAction("chat");
                                    request.setAttribute("msg", chatMessage);
                                    objectOutputStream.writeObject(request); // 发送请求
                                    objectOutputStream.flush();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });
                        listener.send(chatMessage);
                        mEtMsg.setText(null);
                    }
                }
            }
        });
        // 点击表情按钮
        mBtnFace.setOnClickListener(getFunctionBtnListener(LAYOUT_TYPE_FACE));
        // 点击表情按钮旁边的加号
        mBtnMore.setOnClickListener(getFunctionBtnListener(LAYOUT_TYPE_MORE));
        // 点击消息输入框
        mEtMsg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hideLayout();
            }
        });
    }

    private OnClickListener getFunctionBtnListener(final int which) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShow() && which == layoutType) {
                    hideLayout();
                    showKeyboard(context);
                } else {
                    changeLayout(which);
                    showLayout();
                    mBtnFace.setChecked(layoutType == LAYOUT_TYPE_FACE);
                    mBtnMore.setChecked(layoutType == LAYOUT_TYPE_MORE);
                }
            }
        };
    }

    private void changeLayout(int mode) {
        adapter = new FaceCategroyAdapter(((FragmentActivity) getContext())
                .getSupportFragmentManager(), mode);
        adapter.setOnOperationListener(listener);
        layoutType = mode;
        setFaceData(mFaceData);
    }

    @Override
    public void onSoftKeyboardOpened(int keyboardHeightInPx) {
        hideLayout();
    }

    @Override
    public void onSoftKeyboardClosed() {
    }

    public void setFaceData(List<String> faceData) {
        mFaceData = faceData;
        adapter.refresh(faceData);
        mPagerFaceCagetory.setAdapter(adapter);
        mFaceTabs.setViewPager(mPagerFaceCagetory);
        if (layoutType == LAYOUT_TYPE_MORE) {
            mFaceTabs.setVisibility(GONE);
        } else {
            //加1是表示第一个分类为默认的emoji表情分类，这个分类是固定不可更改的
            if (faceData.size() + 1 < 2) {
                mFaceTabs.setVisibility(GONE);
            } else {
                mFaceTabs.setVisibility(VISIBLE);
            }
        }
    }

    public EditText getEditTextBox() {
        return mEtMsg;
    }

    public void showLayout() {
        hideKeyboard(this.context);
        // 延迟一会，让键盘先隐藏再显示表情键盘，否则会有一瞬间表情键盘和软键盘同时显示
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mRlFace.setVisibility(View.VISIBLE);
                if (mFaceListener != null) {
                    mFaceListener.onShowFace();
                }
            }
        }, 50);
    }


    public boolean isShow() {
        return mRlFace.getVisibility() == VISIBLE;
    }

    public void hideLayout() {
        mRlFace.setVisibility(View.GONE);
        if (mBtnFace.isChecked()) {
            mBtnFace.setChecked(false);
        }
        if (mBtnMore.isChecked()) {
            mBtnMore.setChecked(false);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive() && activity.getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(activity.getCurrentFocus()
                        .getWindowToken(), 0);
            }
        }
    }

    /**
     * 显示软键盘
     */
    public static void showKeyboard(Context context) {
        Activity activity = (Activity) context;
        if (activity != null) {
            InputMethodManager imm = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInputFromInputMethod(activity.getCurrentFocus()
                    .getWindowToken(), 0);
            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public OnOperationListener getOnOperationListener() {
        return listener;
    }

    public void setOnOperationListener(OnOperationListener onOperationListener) {
        this.listener = onOperationListener;
        adapter.setOnOperationListener(onOperationListener);
    }

    public void setOnToolBoxListener(OnToolBoxListener mFaceListener) {
        this.mFaceListener = mFaceListener;
    }
}
