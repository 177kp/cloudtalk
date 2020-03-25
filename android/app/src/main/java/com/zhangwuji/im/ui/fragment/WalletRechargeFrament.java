package com.zhangwuji.im.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.base.TTBaseFragment;
import com.zhangwuji.im.ui.helper.ApiAction;

/**
 * Created by chenwang on 2019/8/23.
 */

public class WalletRechargeFrament extends TTBaseFragment {
        private View curView = null;
        private EditText tv_money;
        IMService imService;
        private QMUITipDialog tipDialog;
        private Button rechargeButtion;

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
            curView = inflater.inflate(R.layout.tt_fragment_wallet_recharge, topContentView);
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
            setTopTitle("充值中心");
            setTopLeftButton(R.drawable.tt_top_back);
            topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    getActivity().finish();
                }
            });
            setTopLeftText(getResources().getString(R.string.top_left_back));
            tv_money=curView.findViewById(R.id.et_recharge);
            rechargeButtion=curView.findViewById(R.id.recharge_button);

            tipDialog = new QMUITipDialog.Builder(getActivity())
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("Loading..")
                    .create();


            rechargeButtion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tipDialog.show();
                }
            });

        }

        @Override
        protected void initHandler() {
        }
}
