package com.zhangwuji.im.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.sp.ConfigurationSp;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.SysConstant;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.activity.WalletRechargeActivity;
import com.zhangwuji.im.ui.base.TTBaseFragment;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.CheckboxConfigHelper;

/**
 * Created by chenwang on 2019/8/23.
 */

public class WalletFrament extends TTBaseFragment {
        private View curView = null;
        private TextView tv_money;
        IMService imService;
        private QMUITipDialog tipDialog;
        private Button rechargeButton;

        private IMServiceConnector imServiceConnector = new IMServiceConnector(){
            @Override
            public void onIMServiceConnected() {
                logger.d("config#onIMServiceConnected");
                 imService = imServiceConnector.getIMService();
            }

            @Override
            public void onServiceDisconnected() {
            }
        };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            imServiceConnector.connect(this.getActivity());
            if (null != curView) {
                ((ViewGroup) curView.getParent()).removeView(curView);
                return curView;
            }
            curView = inflater.inflate(R.layout.tt_fragment_wallet, topContentView);
            initRes();
            return curView;
        }

        /**
         * Called when the fragment is no longer in use.  This is called
         * after {@link #onStop()} and before {@link #onDetach()}.
         */
        @Override
        public void onDestroy() {
            super.onDestroy();
            imServiceConnector.disconnect(getActivity());
        }

        @Override
        public void onResume() {

            super.onResume();
        }

        /**
         * @Description 初始化资源
         */
        private void initRes() {
            // 设置标题栏
            setTopTitle("我的钱包");
            setTopLeftButton(R.drawable.tt_top_back);
            topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    getActivity().finish();
                }
            });
            setTopLeftText(getResources().getString(R.string.top_left_back));
            setTopRightText("明细");
            tipDialog = new QMUITipDialog.Builder(getActivity())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("Loading..")
                    .create();
            tipDialog.show();

            rechargeButton=curView.findViewById(R.id.recharge_button);

            rechargeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().startActivity(new Intent(getActivity(), WalletRechargeActivity.class));
                }
            });

            tv_money=curView.findViewById(R.id.tv_money);
            ApiAction apiAction=new ApiAction(getActivity());
            apiAction.getMyAccount(new BaseAction.ResultCallback<String>() {
                @Override
                public void onSuccess(String s) {
                    JSONObject jsonObject= JSON.parseObject(s);
                    if(jsonObject.getIntValue("code")==200) {
                        JSONObject data = jsonObject.getJSONObject("data");
                        tv_money.setText("¥"+data.getString("availableMoney"));
                        tipDialog.cancel();
                    }
                    else
                    {
                        tipDialog.cancel();
                        Toast.makeText(getActivity(),"需要设置支付密码!",Toast.LENGTH_SHORT);
                    }

                }
                @Override
                public void onError(String errString) {
                    tipDialog.cancel();
                }
            });

        }

        @Override
        protected void initHandler() {
        }
}
