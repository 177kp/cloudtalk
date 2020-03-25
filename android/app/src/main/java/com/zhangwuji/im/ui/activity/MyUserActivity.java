package com.zhangwuji.im.ui.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.entity.SystemMessage;
import com.zhangwuji.im.imcore.event.PriorityEvent;
import com.zhangwuji.im.imcore.event.UserInfoEvent;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.adapter.FriendListAdapter;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.base.TTBaseFragmentActivity;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.ui.widget.BottomMenuDialog;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.ui.widget.photo.PhotoUtils;
import com.zhangwuji.im.utils.AvatarGenerate;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class MyUserActivity extends TTBaseActivity implements View.OnClickListener {


    private IMService imService;
    private User currentUser;
    private int currentUserId;
    private RelativeLayout user_avatar,rl_nickanme,rl_sex,rl_id,rl_qrcode,rl_sign_info;
    private TextView tv_nickanme,tv_sex,tv_id,tv_sign_info;
    private IMBaseImageView user_portrait;
    int mCurrentDialogStyle = com.qmuiteam.qmui.R.style.QMUI_Dialog;
    private PhotoUtils photoUtils;
    private BottomMenuDialog dialog;
    private String imageUrl;
    private Uri selectUri;
    private QMUITipDialog tipDialog;

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }

            currentUserId = getIntent().getIntExtra(IntentConstant.KEY_PEERID,0);
            if(currentUserId == 0){
                logger.e("detail#intent params error!!");
                return;
            }
            currentUser = imService.getContactManager().findContact(currentUserId);
            init_userinfo();
        }
        @Override
        public void onServiceDisconnected() {}
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater.from(this).inflate(R.layout.my_info_activity, topContentView);
        imServiceConnector.connect(this);
        //TOP_CONTENT_VIEW
        setLeftButton(R.drawable.ac_back_icon);
        setLeftText("返回");
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);
        setTitle("个人资料");
    }

    public void init_userinfo()
    {
        user_avatar=findViewById(R.id.user_avatar);
        rl_nickanme=findViewById(R.id.rl_nickanme);
        rl_sex=findViewById(R.id.rl_sex);
        rl_id=findViewById(R.id.rl_id);
        rl_qrcode=findViewById(R.id.rl_qrcode);
        rl_sign_info=findViewById(R.id.rl_sign_info);


        tv_nickanme=findViewById(R.id.tv_nickanme);
        tv_sex=findViewById(R.id.tv_sex);
        tv_id=findViewById(R.id.tv_id);
        tv_sign_info=findViewById(R.id.tv_sign_info);

        tv_nickanme.setText(currentUser.getMainName());
        tv_id.setText(currentUser.getPeerId()+"");

        if(currentUser.getGender()==1)
        {
            tv_sex.setText("男");
        }
        else
        {
            tv_sex.setText("女");
        }

        setPortraitChangeListener();
        user_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showPhotoDialog();

            }
        });

        rl_sign_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(MyUserActivity.this);
                builder.setTitle("修改个性签名")
                        .setPlaceholder("请输入您的个性签名")
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String text = builder.getEditText().getText().toString();
                                if (!text.equals("")) {
                                    ApiAction apiAction=new ApiAction(MyUserActivity.this);
                                    apiAction.edit_userinfo(0, text, "", text, new BaseAction.ResultCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {

                                            Toast.makeText(MyUserActivity.this,"修改成功!",Toast.LENGTH_SHORT).show();

                                            tv_sign_info.setText(text);
                                            imService.getLoginManager().setLoginInfo(currentUser);

                                        }

                                        @Override
                                        public void onError(String errString) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        })
                        .create(mCurrentDialogStyle).show();
            }
        });

        rl_nickanme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(MyUserActivity.this);
                builder.setTitle("修改昵称")
                        .setPlaceholder("请输入您的昵称")
                        .setDefaultText(currentUser.getMainName())
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                String text = builder.getEditText().getText().toString();
                                if (!text.equals("")) {
                                    ApiAction apiAction=new ApiAction(MyUserActivity.this);
                                    apiAction.edit_userinfo(0, text, "", "", new BaseAction.ResultCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {

                                            Toast.makeText(MyUserActivity.this,"修改成功!",Toast.LENGTH_SHORT).show();

                                            tv_nickanme.setText(text);
                                            currentUser.setMainName(text);
                                            imService.getLoginManager().setLoginInfo(currentUser);
                                            EventBus.getDefault().postSticky(UserInfoEvent.USER_INFO_OK);
                                        }

                                        @Override
                                        public void onError(String errString) {
                                            dialog.dismiss();
                                        }
                                    });
                                }
                                dialog.dismiss();
                            }
                        })
                        .create(mCurrentDialogStyle).show();
            }
        });

        rl_sex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String[] items = new String[]{"男", "女"};
                new QMUIDialog.MenuDialogBuilder(MyUserActivity.this)
                        .addItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();
                                int sex=which+1;
                                ApiAction apiAction=new ApiAction(MyUserActivity.this);
                                apiAction.edit_userinfo(sex, "", "", "", new BaseAction.ResultCallback<String>() {
                                    @Override
                                    public void onSuccess(String s) {

                                        Toast.makeText(MyUserActivity.this,"修改成功!",Toast.LENGTH_SHORT).show();
                                        tv_sex.setText(sex==1?"男":"女");
                                        currentUser.setGender(sex);
                                        imService.getLoginManager().setLoginInfo(currentUser);
                                    }

                                    @Override
                                    public void onError(String errString) {
                                        dialog.dismiss();
                                    }
                                });

                            }
                        })
                        .setTitle("请选择性别")
                        .create(mCurrentDialogStyle).show();
            }
        });


        user_portrait=findViewById(R.id.user_portrait);
        user_portrait.setCorner(8);
        user_portrait.setImageUrl(AvatarGenerate.generateAvatar(currentUser.getAvatar(),currentUser.getMainName(),currentUser.getPeerId()+""));
        user_portrait.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyUserActivity.this, DetailPortraitActivity.class);
                intent.putExtra(IntentConstant.KEY_AVATAR_URL, currentUser.getAvatar());
                intent.putExtra(IntentConstant.KEY_IS_IMAGE_CONTACT_AVATAR, true);

                startActivity(intent);
            }
        });

        rl_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMUIHelper.openQRCodeActivity(MyUserActivity.this, LoginInfoSp.instance().getLoginInfoIdentity().getLoginName());
            }
        });

    }

    private void setPortraitChangeListener() {
        photoUtils = new PhotoUtils(new PhotoUtils.OnPhotoResultListener() {
            @Override
            public void onPhotoResult(Uri uri) {
                if (uri != null && !TextUtils.isEmpty(uri.getPath())) {
                    selectUri = uri;
                   // LoadDialog.show(mContext);
                    uploadImage("", "", selectUri);
                }
            }

            @Override
            public void onPhotoCancel() {

            }
        });
    }

    static public final int REQUEST_CODE_ASK_PERMISSIONS = 101;

    /**
     * 弹出底部框
     */
    @TargetApi(23)
    private void showPhotoDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        dialog = new BottomMenuDialog(MyUserActivity.this);
        dialog.setConfirmListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (Build.VERSION.SDK_INT >= 23) {
                    int checkPermission = checkSelfPermission(Manifest.permission.CAMERA);
                    if (checkPermission != PackageManager.PERMISSION_GRANTED) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                            requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                        } else {
                            new AlertDialog.Builder(MyUserActivity.this)
                                    .setMessage("您需要在设置里打开相机权限。")
                                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            requestPermissions(new String[] {Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);
                                        }
                                    })
                                    .setNegativeButton("取消", null)
                                    .create().show();
                        }
                        return;
                    }
                }
                photoUtils.takePicture(MyUserActivity.this);
            }
        });
        dialog.setMiddleListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                photoUtils.selectPicture(MyUserActivity.this);
            }
        });
        dialog.show();
    }

    /**
     * 质量压缩方法
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 80, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 90;

        while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset(); // 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;// 每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
        return bitmap;
    }
    /**
     * 图片按比例大小压缩方法
     *
     * @param srcPath （根据路径获取图片并压缩）
     * @return
     */
    public static Bitmap getimage(String srcPath) {

        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// 此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        float hh = 800f;// 这里设置高度为800f
        float ww = 480f;// 这里设置宽度为480f
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// 设置缩放比例
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
    }

    /**
     * 图片按比例大小压缩方法
     *
     * @param image （根据Bitmap图片压缩）
     * @return
     */
    public static Bitmap compressScale(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, baos);

        // 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出
        if (baos.toByteArray().length / 1024 > 1024) {
            baos.reset();// 重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, 90, baos);// 这里压缩50%，把压缩后的数据存放到baos中
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // 开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        // 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
        // float hh = 800f;// 这里设置高度为800f
        // float ww = 480f;// 这里设置宽度为480f
        float hh = 500f;
        float ww = 500f;
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;// be=1表示不缩放
        if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) { // 如果高度高的话根据高度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be; // 设置缩放比例
        // newOpts.inPreferredConfig = Config.RGB_565;//降低图片从ARGB888到RGB565

        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);

        // return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩

        return bitmap;
    }
    private BitmapFactory.Options getBitmapOption(int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

    public void saveBitmapFile(Bitmap bitmap, String filepath) {
        File file = new File(filepath);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param path
     *            要上传的文件路径
     *            服务端接收URL
     * @throws Exception
     */
    public void uploadImage(final String domain, String imageToken, Uri path){

        File file = new File(path.getPath());
        if (file.exists() && file.length() > 0) {

            try
            {
                Bitmap bitmap=BitmapFactory.decodeFile(path.getPath(),getBitmapOption(2));

                Bitmap bitmap1=compressScale(bitmap);
                saveBitmapFile(bitmap,path.getPath());
                file = new File(path.getPath());
                tipDialog = new QMUITipDialog.Builder(this)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .setTipWord("请稍后..")
                        .create();
                tipDialog.show();

                IMAction imAction=new IMAction(MyUserActivity.this);
                imAction.postFile(file, 1, file.getName(), new BaseAction.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String s) {
                        JSONObject jsonObject=JSONObject.parseObject(s);
                        if(jsonObject.getIntValue("code")==0)
                        {
                            JSONObject data=jsonObject.getJSONObject("data");
                            String picurl=data.getString("src");

                            ApiAction apiAction=new ApiAction(MyUserActivity.this);
                            apiAction.edit_userinfo(0, "", picurl, "", new BaseAction.ResultCallback<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    tipDialog.cancel();
                                    currentUser.setAvatar(picurl);
                                    imService.getLoginManager().setLoginInfo(currentUser);
                                    user_portrait.setImageUrl(AvatarGenerate.generateAvatar(currentUser.getAvatar(),currentUser.getMainName(),currentUser.getPeerId()+""));
                                    Toast.makeText(MyUserActivity.this,"头像更新成功!",Toast.LENGTH_SHORT).show();

                                    EventBus.getDefault().postSticky(UserInfoEvent.USER_INFO_OK);

                                }

                                @Override
                                public void onError(String errString) {
                                    tipDialog.cancel();
                                }
                            });
                        }
                        else
                        {
                            tipDialog.cancel();
                            Toast.makeText(MyUserActivity.this,"头像上传失败!",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String errString) {
                        tipDialog.cancel();
                        Toast.makeText(MyUserActivity.this,"头像上传失败!",Toast.LENGTH_SHORT).show();

                    }
                });


            }catch (Exception e){return;}


        } else {
        }
    }


    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                finish();
                break;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PhotoUtils.INTENT_CROP:
            case PhotoUtils.INTENT_TAKE:
            case PhotoUtils.INTENT_SELECT:
                photoUtils.onActivityResult(MyUserActivity.this, requestCode, resultCode, data);
                break;

        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }

}
