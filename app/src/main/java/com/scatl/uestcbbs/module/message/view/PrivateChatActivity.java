package com.scatl.uestcbbs.module.message.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.scatl.uestcbbs.R;
import com.scatl.uestcbbs.base.BaseActivity;
import com.scatl.uestcbbs.base.BaseEvent;
import com.scatl.uestcbbs.base.BasePresenter;
import com.scatl.uestcbbs.custom.MyLinearLayoutManger;
import com.scatl.uestcbbs.custom.emoticon.EmoticonPanelLayout;
import com.scatl.uestcbbs.entity.PrivateChatBean;
import com.scatl.uestcbbs.entity.SendPrivateMsgResultBean;
import com.scatl.uestcbbs.entity.UploadResultBean;
import com.scatl.uestcbbs.helper.glidehelper.GlideEngineForPictureSelector;
import com.scatl.uestcbbs.module.message.adapter.PrivateChatAdapter;
import com.scatl.uestcbbs.module.message.presenter.PrivateChatPresenter;
import com.scatl.uestcbbs.util.CommonUtil;
import com.scatl.uestcbbs.util.Constant;
import com.scatl.uestcbbs.util.ImageUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PrivateChatActivity extends BaseActivity implements PrivateChatView{

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private PrivateChatAdapter privateChatAdapter;
    private ImageView addImageBtn, addEmoticonBtn, senBtn;
    private EditText chatContent;
    private CoordinatorLayout coordinatorLayout;
    private EmoticonPanelLayout emoticonPanelLayout;

    private PrivateChatPresenter privateChatPresenter;

   // private static final int ACTION_SELECT_PHOTO = 23;

    private int hisId;
    private String hisName, sendType, sendContent;

    @Override
    protected void getIntent(Intent intent) {
        super.getIntent(intent);
        hisId = intent.getIntExtra(Constant.IntentKey.USER_ID, Integer.MAX_VALUE);
        hisName = intent.getStringExtra(Constant.IntentKey.USER_NAME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_private_chat;
    }

    @Override
    protected void findView() {
        toolbar = findViewById(R.id.private_chat_toolbar);
        recyclerView = findViewById(R.id.private_chat_rv);
        addImageBtn = findViewById(R.id.private_chat_add_photo);
        addEmoticonBtn = findViewById(R.id.private_chat_add_emoticon);
        chatContent = findViewById(R.id.private_chat_edittext);
        senBtn = findViewById(R.id.private_chat_send_btn);
        coordinatorLayout = findViewById(R.id.private_chat_coor_layout);
        emoticonPanelLayout = findViewById(R.id.private_chat_emoticon_layout);
    }

    @Override
    protected void initView() {
        privateChatPresenter = (PrivateChatPresenter) presenter;

        toolbar.setTitle(hisName);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        senBtn.setOnClickListener(this);
        addImageBtn.setOnClickListener(this);
        addEmoticonBtn.setOnClickListener(this::onClickListener);
        chatContent.setOnClickListener(this::onClickListener);

        privateChatAdapter = new PrivateChatAdapter(R.layout.item_private_chat);
        privateChatAdapter.setHasStableIds(true);
        MyLinearLayoutManger myLinearLayoutManger = new MyLinearLayoutManger(this);
        myLinearLayoutManger.setStackFromEnd(true);
        recyclerView.setLayoutManager(myLinearLayoutManger);
        recyclerView.setAdapter(privateChatAdapter);
        recyclerView.setLayoutAnimation(AnimationUtils.loadLayoutAnimation(this, R.anim.layout_animation_scale_in));

        privateChatPresenter.getPrivateMsg(hisId, this);
    }

    @Override
    protected BasePresenter initPresenter() {
        return new PrivateChatPresenter();
    }

    @Override
    protected void onClickListener(View view) {

        if (view.getId() == R.id.private_chat_add_photo) {
            PictureSelector.create(this)
                    .openGallery(PictureMimeType.ofImage())
                    .isCamera(true)
                    .isGif(false)
                    .showCropFrame(false)
                    .hideBottomControls(false)
                    .theme(com.luck.picture.lib.R.style.picture_WeChat_style)
                    .maxSelectNum(1)
                    .isEnableCrop(false)
                    .imageEngine(GlideEngineForPictureSelector.createGlideEngine())
                    .forResult(PictureConfig.CHOOSE_REQUEST);
            //privateChatPresenter.requestPermission(this, ACTION_SELECT_PHOTO, Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (view.getId() == R.id.private_chat_add_emoticon) {
            if (emoticonPanelLayout.getVisibility() == View.GONE) {
                CommonUtil.hideSoftKeyboard(this, chatContent);
                emoticonPanelLayout.postDelayed(() -> {
                    emoticonPanelLayout.setVisibility(View.VISIBLE);
                }, 100);

            } else if (emoticonPanelLayout.getVisibility() == View.VISIBLE) {
                CommonUtil.showSoftKeyboard(this, chatContent, 100);
                emoticonPanelLayout.setVisibility(View.GONE);
            }
        }

        if (view.getId() == R.id.private_chat_edittext) {
            emoticonPanelLayout.setVisibility(View.GONE);
        }

        if (view.getId() == R.id.private_chat_send_btn) {
            sendType = "text";
            sendContent = chatContent.getText().toString();
            privateChatPresenter.sendPrivateMsg(
                    sendContent,
                    sendType, hisId, this );
        }
    }

    @Override
    protected void setOnItemClickListener() {
        privateChatAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.item_private_chat_his_img || view.getId() == R.id.item_private_chat_mine_img) {
                List<String> urls = new ArrayList<>();
                urls.add(privateChatAdapter.getData().get(position).content);
                ImageUtil.showImages(this, urls, 0);
            }
        });

    }

    @Override
    public void onGetPrivateListSuccess(PrivateChatBean privateChatBean) {
        privateChatAdapter.setHisInfo(privateChatBean.body.pmList.get(0).name,
                privateChatBean.body.pmList.get(0).avatar,
                privateChatBean.body.pmList.get(0).fromUid);
        recyclerView.scheduleLayoutAnimation();
        privateChatAdapter.setNewData(privateChatBean.body.pmList.get(0).msgList);
        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.SET_NEW_PRIVATE_COUNT_SUBTRACT));
        EventBus.getDefault().post(new BaseEvent<>(BaseEvent.EventCode.READ_PRIVATE_CHAT_MSG));
    }

    @Override
    public void onGetPrivateListError(String msg) {
        showToast(msg);
    }

    @Override
    public void onSendPrivateChatMsgSuccess(SendPrivateMsgResultBean sendPrivateMsgResultBean) {
        chatContent.setText("");

        privateChatAdapter.insertMsg(this, sendContent, sendType);
        recyclerView.scrollToPosition(privateChatAdapter.getData().size() - 1);
        showSnackBar(getWindow().getDecorView(), sendPrivateMsgResultBean.head.errInfo);
    }

    @Override
    public void onSendPrivateChatMsgError(String msg) {
        showSnackBar(getWindow().getDecorView(), msg);
    }

    @Override
    public void onCompressImageSuccess(List<File> compressedFiles) {
        privateChatPresenter.upload(compressedFiles, "pm", "image", this);
    }

    @Override
    public void onCompressImageFail(String msg) {
        showSnackBar(getWindow().getDecorView(), msg);
    }

    @Override
    public void onUploadSuccess(UploadResultBean uploadResultBean) {
        sendType = "image";
        sendContent = uploadResultBean.body.attachment.get(0).urlName;
        privateChatPresenter.sendPrivateMsg(sendContent,
                sendType, hisId, this);
    }

    @Override
    public void onUploadError(String msg) {
        showToast(msg);
    }

    @Override
    public void showMsg(String msg) {
        showSnackBar(getWindow().getDecorView(), msg);
    }

    @Override
    protected boolean registerEventBus() {
        return true;
    }

    @Override
    protected void receiveEventBusMsg(BaseEvent baseEvent) {
        if (baseEvent.eventCode == BaseEvent.EventCode.INSERT_EMOTION) {
            privateChatPresenter.insertEmotion(this, chatContent, (String) baseEvent.eventData);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == ACTION_SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
//
//        }
        if (resultCode == RESULT_OK && requestCode == PictureConfig.CHOOSE_REQUEST) {
            List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
            List<String> files = new ArrayList<>();
            for (int i = 0; i < selectList.size(); i ++) {
                files.add(selectList.get(i).getRealPath());
            }
            privateChatPresenter.checkBeforeSendImage(this, files);
        }
    }
}
