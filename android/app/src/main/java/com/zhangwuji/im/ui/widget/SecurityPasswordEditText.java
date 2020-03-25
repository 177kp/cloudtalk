package com.zhangwuji.im.ui.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.zhangwuji.im.R;

import java.util.ArrayList;
import java.util.LinkedList;



/**
 * 自定义密码输入框，仿微信支付密码输入框
 * 
 * @author hezenan
 * 
 */
public class SecurityPasswordEditText extends LinearLayout {
	private AsteriskPasswordTransformationMethod asteriskPassword;
	private ArrayList<EditText> editList = new ArrayList<EditText>();
	private LinkedList<InputData> inputDatas = new LinkedList<InputData>();
	private int temp = 0;
	private int temp1 = 0;
	private EditText one_pwd;
	private EditText two_pwd;
	private EditText three_pwd;
	private EditText four_pwd;
	private EditText five_pwd;
	private EditText six_pwd;
	private String inputnumber;

    /**
     * 一定一个接口
     */
    public interface ICoallBack{
        public void onClickButton(String s);
    }
    /**
     * 初始化接口变量
     */
    ICoallBack icallBack = null;

    /**
     * 自定义控件的自定义事件
     * @param iBack 接口类型
     */
    public void setonClick(ICoallBack iBack)
    {
        icallBack = iBack;
    }

    public SecurityPasswordEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}
	public SecurityPasswordEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public String getInputnumber() {
		return inputnumber;
	}
	public void setInputnumber(String inputnumber) {
		this.inputnumber = inputnumber;
	}
	private void init(Context context) {
		LayoutInflater.from(getContext()).inflate(R.layout.edittext_password,
				this);
		one_pwd = (EditText) findViewById(R.id.pwd_one);
		two_pwd = (EditText) findViewById(R.id.pwd_two);
		three_pwd = (EditText) findViewById(R.id.pwd_three);
		four_pwd = (EditText) findViewById(R.id.pwd_four);
		five_pwd = (EditText) findViewById(R.id.pwd_five);
		six_pwd = (EditText) findViewById(R.id.pwd_six);
		editList.add(one_pwd );
        editList.add(two_pwd);
        editList.add(three_pwd);
        editList.add(four_pwd);
        editList.add(five_pwd);
        editList.add(six_pwd );
        asteriskPassword =  new AsteriskPasswordTransformationMethod();
        one_pwd.setTransformationMethod(asteriskPassword);
 		two_pwd.setTransformationMethod(asteriskPassword);
 		three_pwd.setTransformationMethod(asteriskPassword);
 		four_pwd.setTransformationMethod(asteriskPassword);
 		five_pwd.setTransformationMethod(asteriskPassword);
 		six_pwd.setTransformationMethod(asteriskPassword);
	 for (final EditText editText : editList) {
         editText.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				 if (arg1.getAction() == MotionEvent.ACTION_UP) {
			            initFocus();
			            return false;
			        }
			        return true;
			}
		});
         final int index = temp;
         if (index != 0) {
             editText.setOnKeyListener(new OnKeyListener() {
                 @Override
                 public boolean onKey(View view, int i, KeyEvent keyEvent) {
                     if (i == KeyEvent.KEYCODE_DEL && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                         if (!inputDatas.get(index).isHasInput()) {
                             editList.get(index - 1).requestFocus();
                             editList.get(index - 1).setText("");
                             inputDatas.get(index - 1).setHasInput(false);
                         }
                         inputDatas.get(index).setHasInput(false);
                         
                         inputnumber="";
                         for (EditText editText1 : editList) {
                             inputnumber += editText1.getText().toString();
                         }
                     }


                     return false;
                 }
             });
         }
         editText.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             }

             @Override
             public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {


             }

             @Override
             public void afterTextChanged(Editable editable) {
                 if (editable.length() == 1) {
                	 inputnumber = "";
                     if (index == editList.size() - 1) {
                         for (EditText editText1 : editList) {
                        	 inputnumber += editText1.getText().toString();
                         }
                         if(inputnumber.length()==6)
                         {
                             try {
                                 icallBack.onClickButton(inputnumber);
                             }
                             catch (Exception e){}
                         }

                     } else {
                         editList.get(index + 1).requestFocus();
                     }
                     inputDatas.get(index).setHasInput(true);
                 }
             }
         });
         temp++;
     }
	 initInputData();
	}
	private void initInputData() {
        for (int i = 0; i < 6; i++) {
            InputData data = new InputData();
            data.hasInput = false;
            data.tag = i;
            add(data);
        }
    }

    public void clearData()
    {
        for (InputData inputData : inputDatas) {
             editList.get(inputData.tag).setText("");
        }
        editList.get(0).requestFocus();
    }

    public void initFocus() {
        for (InputData inputData : inputDatas) {
            if (!inputData.isHasInput()) {
                editList.get(inputData.tag).requestFocus();
                return;
            }
        }
    }
    class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {

		@Override
		public CharSequence getTransformation(CharSequence source, View view) {
			return new PasswordCharSequence(source);
		} 
		private class PasswordCharSequence implements CharSequence {
			private CharSequence mSource;  
			public PasswordCharSequence(CharSequence source) {  
				mSource = source;  
			}  
			@Override
			public int length() {
				return mSource.length();
			}

			@Override
			public char charAt(int index) {
				return '●';
			}

			@Override
			public CharSequence subSequence(int start, int end) {
				return mSource.subSequence(1, end);
			}

		}
	}
    public void add(InputData inputData) {
        inputDatas.add(inputData);
    }


    public class InputData {
        boolean hasInput = false; //是否输入字符
        int tag;

        public boolean isHasInput() {
            return hasInput;
        }

        public void setHasInput(boolean hasInput) {
            this.hasInput = hasInput;
        }
    }
  
}