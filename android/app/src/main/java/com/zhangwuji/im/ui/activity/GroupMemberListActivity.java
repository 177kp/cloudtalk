
package com.zhangwuji.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangwuji.im.DB.entity.Group;
import com.zhangwuji.im.DB.entity.PeerEntity;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.event.GroupEvent;
import com.zhangwuji.im.imcore.manager.IMContactManager;
import com.zhangwuji.im.imcore.manager.IMGroupManager;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.ui.adapter.GroupListAdapter;
import com.zhangwuji.im.ui.adapter.GroupSelectAdapter;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.widget.SearchEditText;
import com.zhangwuji.im.ui.widget.SortSideBar;
import com.zhangwuji.im.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class GroupMemberListActivity extends TTBaseActivity   implements SortSideBar.OnTouchingLetterChangedListener,AdapterView.OnItemClickListener, View.OnClickListener {

    private static Logger logger = Logger.getLogger(GroupMemberListActivity.class);

    private IMService imService;

    private GroupListAdapter adapter;
    private ListView contactListView;

    private SortSideBar sortSideBar;
    private TextView dialog;
    private SearchEditText searchEditText;

    private String curSessionKey;
    private PeerEntity peerEntity;

    private List<User> contactList=new LinkedList<>();

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("groupselmgr#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            Intent intent = getIntent();
            curSessionKey = intent.getStringExtra(IntentConstant.KEY_SESSION_KEY);
            if(curSessionKey==null)
            {
                peerEntity=imService.getLoginManager().getLoginInfo();
            }
            else {
                peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
            }
            /**已经处于选中状态的list*/
            Set<Integer> alreadyList = getAlreadyCheckList();
            initContactList(alreadyList);
        }

        @Override
        public void onServiceDisconnected() {}
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        LayoutInflater.from(this).inflate(R.layout.tt_activity_group_member_select, topContentView);
        imServiceConnector.connect(this);
        initRes();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        imServiceConnector.disconnect(this);
    }

    private void initContactList(final Set<Integer> alreadyList) {
        // 根据拼音排序
        adapter = new GroupListAdapter(this,imService);
        contactListView.setAdapter(adapter);

        contactListView.setOnItemClickListener(this);
        contactListView.setOnItemLongClickListener(adapter);

        Group entity = (Group) peerEntity;
        IMContactManager manager = imService.getContactManager();
        for(Integer memId:entity.getlistGroupMemberIds()){
            User user =  manager.findContact(memId);
            if(user!=null){
                if(imService.getLoginManager().getLoginId() == user.getPeerId()){
                    // 群主放在第一个
                    contactList.add(0, user);
                }else {
                    contactList.add(user);
                }
            }
        }
        adapter.setAllUserList(contactList);
    }

    /**
     * 获取列表中 默认选中成员列表
     * @return
     */
    private Set<Integer> getAlreadyCheckList(){
        Set<Integer> alreadyListSet = new HashSet<>();
        return alreadyListSet;
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {

        setTitle("选择群成员");
        setLeftButton(R.drawable.ac_back_icon);
        topLeftBtn.setOnClickListener(this);
        letTitleTxt.setOnClickListener(this);

        sortSideBar = (SortSideBar) findViewById(R.id.sidrbar);
        sortSideBar.setOnTouchingLetterChangedListener(this);

        dialog = (TextView) findViewById(R.id.dialog);
        sortSideBar.setTextView(dialog);

        contactListView = (ListView) findViewById(R.id.all_contact_list);
        contactListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //如果存在软键盘，关闭掉
                InputMethodManager imm = (InputMethodManager)getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                //txtName is a reference of an EditText Field
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        searchEditText = (SearchEditText) findViewById(R.id.filter_edit);
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String key = s.toString();
                if(TextUtils.isEmpty(key)){
                    adapter.recover();
                    sortSideBar.setVisibility(View.VISIBLE);
                }else{
                    sortSideBar.setVisibility(View.INVISIBLE);
                    adapter.onSearch(key);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    @Override
    public void onTouchingLetterChanged(String s) {
        // TODO Auto-generated method stub
        int position = adapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
            contactListView.setSelection(position);
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         User selectUser=contactList.get(position);
         Intent intent2 = new Intent();
         intent2.putExtra("selectUID",selectUser.getPeerId());
         setResult(Activity.RESULT_OK,intent2);
         finish();
    }
}
