package com.zhangwuji.im.ui.helper;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Pattern;


public class StringUtils {
    private final static Pattern emailer = Pattern
            .compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");

    // private final static Pattern mobile = Pattern
    // .compile("^(13|14|15|18)[0-9]{9}$");

    // private final static Pattern telphone = Pattern
    // .compile("\\d{3,4}-\\d{7,8}");

    private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dateFormater3 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM", Locale.CHINA);
        }
    };

    public static int parseInt(String value) {
        return parseInt(value, 0);
    }

    /**
     * 最终的转换int，添加异常捕获
     * @param value 需要转换的int字符串
     * @param defvalue 转换失败的int值
     */
    public static int parseInt(String value, int defvalue) {
        if (!isEmpty(value) && isNumber(value)) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defvalue;
    }

    /**
     * 添加异常捕获
     */
    public static float parseFloat(String value) {
        // 此处有待正则验证是否为真正的float类型，还有double验证，后面优化。
        if (!isEmpty(value) && isDecimal(value)) {
            try {
                return Float.parseFloat(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return parseInt(value, 0);
    }

    // 判断是否为浮点型数据
    public static boolean isDecimal(String str) {
        Pattern pattern = Pattern.compile("[0-9]*(\\.?)[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 转换double，异常捕获
     * @param value
     * @return
     */
    public static double parseDouble(String value) {
        if (!isEmpty(value) && isDecimal(value)) {
            try {
                return Double.parseDouble(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String showPrice(Object price) {
        double d = 0;
        if (price instanceof Double) {
            d = (Double) price;
        } else {
            d = parseDouble(price + "");
        }
        DecimalFormat decimalFormat = new DecimalFormat("#0.00");
        return "￥ " + decimalFormat.format(d);
    }

    /**
     * 判断是不是一个合法的电子邮件地址
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        if (email == null || email.trim().length() == 0)
            return false;
        return emailer.matcher(email).matches();
    }

    /*
     * 判断手机号是否合法
     */
    public static boolean isMobileNO(String mobiles) {
        if (!isEmpty(mobiles) && isMobileNo(mobiles) && mobiles.length() == 11) {
            return true;
        }
        return false;
    }

    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || input.equals("null") || "".equals(input))
            return true;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static String subString(String text, int length) {
        if (!isEmpty(text)) {
            String pix = "...";
            if (text.length() <= length) {// 当加上结尾后和原数据长度一样，则显示全部数据。
                return text;
            } else {
                return text.substring(0, length - 1) + pix;
            }
        }
        return "";
    }

    /**
     * 是否是整数，包括正整数，0，负整数
     */
    public static boolean isNumber(String str) {
        Pattern pattern = Pattern.compile("^-?[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 是否是手机号，手机号为纯数字
     */
    public static boolean isMobileNo(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断数据集是否为空，为空则返回true
     *
     * @param list input
     * @return boolean
     */
    public static boolean isEmptyList(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyList(String[] list) {
        if (list == null || list.length == 0) {
            return true;
        }
        return false;
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        return parseInt(str, defValue);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null) {
            return 0;
        }
        return parseInt(obj + "");
    }

    /**
     * 左补全月份
     *
     * @param str
     * @return
     */
    public static String padLeftMonth(String str) {
        if (isEmpty(str))
            return "01";

        if (str.length() >= 2) {
            return str;
        } else {
            return "0" + str;
        }
    }

    /**
     * 全角转半角
     *
     * @param s
     * @return String
     * @throw
     */
    public static String fullWidthToHalfWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == 12288) {
                source[i] = ' ';
                // } else if (source[i] == 12290) {
                // source[i] = '.';
            } else if (source[i] >= 65281 && source[i] <= 65374) {
                source[i] = (char) (source[i] - 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * 半角转全角
     *
     * @param s
     * @return String
     * @throw
     */
    public static String halfWidthToFullWidth(String s) {
        if (isEmpty(s)) {
            return s;
        }

        char[] source = s.toCharArray();
        for (int i = 0; i < source.length; i++) {
            if (source[i] == ' ') {
                source[i] = (char) 12288;
                // } else if (source[i] == '.') {
                // source[i] = (char)12290;
            } else if (source[i] >= 33 && source[i] <= 126) {
                source[i] = (char) (source[i] + 65248);
            } else {
                source[i] = source[i];
            }
        }
        return new String(source);
    }

    /**
     * 限制行数，超出用...代替
     *
     * @param view
     * @param maxLine
     * @return void
     * @throw
     */
    public static void truncate(final TextView view, final int maxLine) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (view.getLineCount() > maxLine) {
                    int lineEndIndex = view.getLayout().getLineEnd(maxLine - 1);
                    String text = view.getText().subSequence(0,
                            lineEndIndex - 3)
                            + "...";
                    view.setText(text);
                }
            }
        });
    }

    /**
     * MD5加密
     *
     * @param sInput
     * @return String
     */
    public static String toMD5(String sInput) {
        // MessageDigest md5 = null;
        // try {
        // md5 = MessageDigest.getInstance("MD5");
        // } catch (Exception e) {
        // e.printStackTrace();
        // return "";
        // }
        //
        // char[] charArray = sInput.toCharArray();
        // byte[] byteArray = new byte[charArray.length];
        //
        // for (int i = 0; i < charArray.length; i++) {
        // byteArray[i] = (byte) charArray[i];
        // }
        byte[] byteArray = toMd5(sInput, "utf-8");
        // byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            int val = ((int) byteArray[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString().toUpperCase();
    }

    public synchronized static final byte[] toMd5(String data,
                                                  String encodingType) {
        MessageDigest digest = null;
        if (digest == null) {
            try {
                digest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException nsae) {
                System.err.println("Failed to load the MD5 MessageDigest. ");
                nsae.printStackTrace();
            }
        }
        try {
            digest.update(data.getBytes(encodingType));
        } catch (UnsupportedEncodingException e) {
            digest.update(data.getBytes());
        }
        return digest.digest();
    }

    /**
     * 设置文字中的字体大小及颜色
     *
     * @param text
     * @param character
     * @return Spannable
     * @throw
     */
    public static Spannable setTextColor(String text, String character) {
        Spannable WordtoSpan = new SpannableString(text);
        int len = text.lastIndexOf(character);
        if (len > 0) {
            WordtoSpan.setSpan(new AbsoluteSizeSpan(14, true), 0, len,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置字体大小
        }
        if (len + 1 < text.length()) {
            WordtoSpan.setSpan(new AbsoluteSizeSpan(18, true), len + 1,
                    text.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            ForegroundColorSpan fcs = new ForegroundColorSpan(Color.rgb(233,
                    70, 67)); // 设置颜色
            WordtoSpan.setSpan(fcs, len + 1, text.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return WordtoSpan;
    }

    /**
     * 获取字符串，为空可指定默认值
     *
     * @param inputStr
     * @param defaultStr
     * @return String
     * @throw
     */
    public static String getString(String inputStr, String defaultStr) {
        if (isEmpty(inputStr)) {
            return defaultStr;
        }
        return inputStr;
    }

    /**
     * 将字符串转位日期类型（yyyy-MM-dd HH:mm:ss）
     *
     * @param sdate
     * @return
     */
    public static Date toDate(String sdate) {
        try {
            return dateFormater.get().parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将字符串转位日期类型（yyyy-MM-dd）
     *
     * @param sdate
     * @return
     */
    public static Date toDate2(String sdate) {
        try {
            return dateFormater2.get().parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 将字符串转位日期类型（yyyy-MM）
     *
     * @param sdate
     * @return
     */
    public static Date toDate3(String sdate) {
        try {
            return dateFormater3.get().parse(sdate);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 格式化日期类型
     *
     * @param currDate
     * @param format
     * @return String
     * @throw
     */
    public static String getFormatDateTime(Date currDate,
                                           String format) {
        if (currDate == null) {
            return "";
        }
        SimpleDateFormat dtFormatdB = null;
        try {
            dtFormatdB = new SimpleDateFormat(format);
            return dtFormatdB.format(currDate);
        } catch (Exception e) {
            dtFormatdB = new SimpleDateFormat("yyyy-MM-dd");
            try {
                return dtFormatdB.format(currDate);
            } catch (Exception ex) {
            }
        }
        return "";
    }

    /**
     * 增加月份
     *
     * @return addMonth - 增加多少月
     */
    public static Date addMonth(Date date, int addMonth) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8:00"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, addMonth);
        return calendar.getTime();
    }

    /**
     * 以友好的方式显示时间
     *
     * @param sdate
     * @return
     */
    public static String getFriendlyTime(String sdate) {
        Date time = toDate(sdate);
        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days <= 10) {
            ftime = days + "天前";
        } else if (days > 10) {
            ftime = dateFormater2.get().format(time);
        }
        return ftime;
    }




    // 算两个日期的时间差（天数）
    public static int betweenDays(String begindate, String enddate,
                                  String formatstr) {
        try {
            SimpleDateFormat dfs = new SimpleDateFormat(formatstr);
            Date begin = dfs.parse(begindate);
            Date end = dfs.parse(enddate);
            long between = (end.getTime() - begin.getTime()) / 1000;// 除以1000是为了转换成秒
            long day1 = between / (24 * 3600);
            long hour1 = between % (24 * 3600) / 3600;
            long minute1 = between % 3600 / 60;
            long second1 = between % 60 / 60;
            System.out.println("" + day1 + "天" + hour1 + "小时" + minute1 + "分"
                    + second1 + "秒");
            return Integer.valueOf(day1 + "");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    public static Date getNow() {
        return new Date(System.currentTimeMillis());
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format).format(date);
    }

    // 0到(num-1)随机数，不包含num
    public static int getRandom(int num) {
        Random random = new Random();
        return random.nextInt(num);
    }

    public static boolean probability(int times) {
        if (getRandom(times) == 1) {
            return true;
        }
        return false;
    }

    /**
     * 将HashMap<String,Object>转换成String[]
     *
     * @param ha
     * @return
     */
    public static String[] parseList(HashMap<String, Object> ha) {
        String[] strs = null;
        if (ha != null) {
            strs = new String[ha.size() * 2];
            Iterator<Map.Entry<String, Object>> tre = ha.entrySet().iterator();
            int i = 0;
            while (tre.hasNext()) {
                Map.Entry<String, Object> et = tre.next();
                int postion = i * 2;
                strs[postion] = et.getKey();
                strs[postion + 1] = et.getValue() + "";
                i++;
            }
        }
        return strs;
    }

    public static JSONObject parseList(String[] arrstring) {
        JSONObject obj = new JSONObject();
        if (arrstring != null) {
            // 将arrstring转换成JSONObject，然后让JSONObject转成字符串本地验签，把JSONObject转换成和本地验签顺序一致的字符串数组
            for (int i = 0; i < arrstring.length; i = i + 2) {
                String key = arrstring[i];
                String val = i == arrstring.length ? "" : arrstring[i + 1];
                try {
                    obj.put(key, val);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return obj;
    }

    /**
     * JSONOjbect转换成String[]
     *
     * @param obj  JSONObject对象
     * @param size 元素个数*2
     * @return
     */
    public static String[] parseList(JSONObject obj, int size) {
        String[] sortArrayString = new String[size];
        Iterator it1 = obj.keys();
        int i = 0;
        while (it1.hasNext()) {
            String key = (String) it1.next();
            String value = obj.optString(key);
            sortArrayString[i] = key;
            sortArrayString[i + 1] = value;
            i += 2;
        }
        return sortArrayString;
    }

//    /**
//     * 过滤特殊字符
//     * 
//     * @return
//     */
//    public static String filterSpecialChars(String str) {
//	return str.replaceAll("&", "");
//    }
}
