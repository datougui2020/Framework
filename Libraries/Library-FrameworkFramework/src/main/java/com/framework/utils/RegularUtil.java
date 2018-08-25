package com.framework.utils;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则匹配util
 *
 * @author YobertJomi
 * className RegularUtil
 * created at  2017/4/12  10:42
 */
public class RegularUtil {

    private static volatile RegularUtil singleton;

    private RegularUtil() {
    }

    public static RegularUtil getInstance() {
        if (singleton == null) {
            synchronized (RegularUtil.class) {
                if (singleton == null) {
                    singleton = new RegularUtil();
                }
            }
        }
        return singleton;
    }

    /**
     * 匹配邮箱
     *
     * @param email email
     * @return boolean
     */
    public boolean isEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            return false;
        }
//        String strPattern = "^[a-zA-Z][\\w\\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\" +
//                ".]*[a-zA-Z]$";
        //这个是一个企业级的程序里copy出来的
        String strPattern = "^\\s*\\w+(?:\\.{0,1}[\\w-]+)*@[a-zA-Z0-9]+(?:[-.][a-zA-Z0-9]+)*\\.[a-zA-Z]+\\s*$";
        Pattern p = Pattern.compile(strPattern);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    /**
     * 验证号码的有效性
     *
     * @param mobile mobile
     * @return boolean
     */
    public boolean isMobileNO(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^((13[0-9])|(15[^4,\\D])|(18[0,1,2,3,5-9])|(17[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobile);
        return m.matches();
    }

    /**
     * 验证手机号(1开头的15位数字)
     *
     * @param number number
     * @return boolean
     */
    public boolean isMobileNOSimple(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^1\\d{10}$");
        Matcher m = p.matcher(number);
        return m.matches();
    }

    /**
     * 验证数字
     *
     * @param number number
     * @return boolean
     */
    public boolean isNumberAtLeast15bit(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^[0-9]{15,}$");
        Matcher m = p.matcher(number);
        return m.matches();
    }

    /**
     * 验证数字
     *
     * @param number number
     * @return boolean
     */
    public boolean isNumberAtLeast6bit(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^[0-9]{6,}$");
        Matcher m = p.matcher(number);
        return m.matches();
    }

    public boolean isNumberAtLeast1bit(String number) {
        if (TextUtils.isEmpty(number)) {
            return false;
        }
        Pattern p = Pattern
                .compile("^[0-9]+$");
        Matcher m = p.matcher(number);
        return m.matches();
    }

    /**
     * 验证数字字母
     *
     * @param str str
     * @return boolean
     */
    public boolean isCharOrNumber(String str) {
        Pattern p = Pattern
                .compile("^[_a-zA-Z0-9]{6,}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 验证中文字符
     *
     * @param str str
     * @return boolean
     */
    public boolean isChineseChar(String str) {
        Pattern p = Pattern
                .compile("\"[\\u4e00-\\u9fa5]\"");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 验证小数点后3位
     *
     * @param str str
     * @return boolean
     */
    public boolean isAtleast3bitNumberAfterDigit(String str) {
        Pattern p = Pattern
                .compile("^[0-9]*[\\.][0-9]{3,}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * 从短信字符串提取验证码
     *
     * @param body      短信内容
     * @param YZMLENGTH 验证码的长度 一般6位或者4位
     * @return 接取出来的验证码
     */
    public String getCaptcha(String body, int YZMLENGTH) {
        // 首先([a-zA-Z0-9]{YZMLENGTH})是得到一个连续的六位数字字母组合
        // (?<![a-zA-Z0-9])负向断言([0-9]{YZMLENGTH})前面不能有数字
        // (?![a-zA-Z0-9])断言([0-9]{YZMLENGTH})后面不能有数字出现
        //  获得数字字母组合
        //    Pattern p = Pattern   .compile("(?<![a-zA-Z0-9])([a-zA-Z0-9]{" + YZMLENGTH + "})(?![a-zA-Z0-9])");

        //  获得纯数字
        Pattern p = Pattern.compile("(?<![0-9])([0-9]{" + YZMLENGTH + "})(?![0-9])");

        Matcher m = p.matcher(body);
        if (m.find()) {
            System.out.println(m.group());
            return m.group(0);
        }
        return null;
    }

    /**
     * 验证强密码
     *
     * @param pwd pwd
     */
    public int isPwdStrong(String pwd) {
        try {
            // 弱：纯数字，纯字母，纯特殊字符
            Pattern p1 = Pattern.compile("^(?:\\d+|[a-zA-Z]+|[!@#$%^&*]+)$");
            // 中：字母+数字，字母+特殊字符，数字+特殊字符
            Pattern p2 = Pattern
                    .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$");
            // 强：字母+数字+特殊字符
            Pattern p3 = Pattern
                    .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)(?![a-zA-z\\d]+$)(?![a-zA-z!@#$%^&*]+$)" +
                            "(?![\\d!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$");
            Matcher m1 = p1.matcher(pwd);
            Matcher m2 = p2.matcher(pwd);
            Matcher m3 = p3.matcher(pwd);
            if (m1.matches()) {
                return 0;
            } else if (m2.matches()) {
                return 1;
            } else if (m3.matches()) {
                return 2;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * @param str 用户名（汉字、字母、数字的组合）
     * @return boolean
     */
    public boolean isCommonUserName(String str) {
        Pattern p = Pattern
                .compile("^(?!_)(?!.*?_$)[a-zA-Z0-9_\\u4e00-\\u9fa5]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * @param str 密码（8-24位数字和字母的组合）
     * @return boolean
     */
    public boolean isCommonPwd(String str) {
        Pattern p = Pattern
                .compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,24}$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * @param str 特殊密码（8-24位数字和字母的组合）
     * @return boolean
     */
    public boolean isSpecialPwd(String str) {
        Pattern p1 = Pattern
                .compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,24}$");
        // 中：字母+数字，字母+特殊字符，数字+特殊字符
        Pattern p2 = Pattern
                .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$");
        // 强：字母+数字+特殊字符
        Pattern p3 = Pattern
                .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![!@#$%^&*]+$)(?![a-zA-z\\d]+$)(?![a-zA-z!@#$%^&*]+$)" +
                        "(?![\\d!@#$%^&*]+$)[a-zA-Z\\d!@#$%^&*]+$");
        Matcher m = p1.matcher(str);
        Matcher m1 = p1.matcher(str);
        Matcher m2 = p2.matcher(str);
        Matcher m3 = p3.matcher(str);
        return m1.matches() || m2.matches();
    }

    /**
     * @param str 年龄（1岁--120岁）
     * @return boolean
     */
    public boolean isCommonAge(String str) {
        Pattern p = Pattern
                .compile("^(?:[1-9][0-9]?|1[01][0-9]|120)$$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * @param str 含数字，字母，特殊字符其中之二
     * @return boolean
     */
    public boolean isSpecial(String str) {
        Pattern p1 = Pattern
                .compile("^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{8,24}$");
        // 中：字母+数字，字母+特殊字符，数字+特殊字符
        Pattern p2 = Pattern
                .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![`~!@#$%^&*()+=|{}':;',\\[\\]" +
                        ".<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$)[a-zA-Z\\d`~!@#$%^&*()+=|{}':;',\\[\\]" +
                        ".<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$");
        // 强：字母+数字+特殊字符
        Pattern p3 = Pattern
                .compile("^(?![a-zA-z]+$)(?!\\d+$)(?![`~!@#$%^&*()+=|{}':;',\\[\\]" +
                        ".<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$)(?![a-zA-z\\d]+$)(?![a-zA-z`~!@#$%^&*()+=|{}':;'," +
                        "\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$)" +
                        "(?![\\d`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$)" +
                        "[a-zA-Z\\d`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]+$");
        Matcher m = p1.matcher(str);
        Matcher m1 = p1.matcher(str);
        Matcher m2 = p2.matcher(str);
        Matcher m3 = p3.matcher(str);
        return m1.matches() || m2.matches();
    }

    /**
     * @return 是否含特殊字符
     */
    public boolean containsSpecialChar(String str) {
        Pattern p = Pattern.compile("[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     *（汉字、字母组合）
     * @return boolean
     */
    public boolean isChineseCharactersOrLetter(String str) {
        Pattern p = Pattern
                .compile("^(?!_)(?!.*?_$)[a-zA-Z_\\u4e00-\\u9fa5]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     *（汉字）
     * @return boolean
     */
    public boolean isChineseCharacters(String str) {
        Pattern p = Pattern
                .compile("^(?!_)(?!.*?_$)[\\u4e00-\\u9fa5]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     *（汉字、字母组合）
     * @return boolean
     */
    public boolean isLetter(String str) {
        Pattern p = Pattern
                .compile("^(?!_)(?!.*?_$)[a-zA-Z_]+$");
        Matcher m = p.matcher(str);
        return m.matches();
    }
}