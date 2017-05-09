package com.plugin.silent;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 网络相关工具类.
 *
 * @author yuanjl
 */
public final class NetworkUtil {
    /**
     * http协议.
     */
    public static final String SCHEMA_HTTP = "http://";
    private static final String NETWORK_TYPE_CMWAP = "cmwap";
    private static final String NETWORK_TYPE_UNIWAP = "uniwap";
    private static final String NETWORK_TYPE_3GWAP = "3gwap";
    private static final String NETWORK_TYPE_CTWAP = "ctwap";
    private static final String PROXY_CMWAP = "10.0.0.172";
    private static final String PROXY_CTWAP = "10.0.0.200";

    private NetworkUtil() {
    }

    public static boolean isUrlValid(String url) {
        // 匹配是否为有效URL
        Pattern patt = Pattern
                .compile("(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*");
        Matcher matcher = patt.matcher(url);
        boolean isMatch = matcher.matches();

        if (isMatch == false || url.toLowerCase().contains("javascript")) {
            return false;
        }

        return true;
    }

    /**
     * 获取代理host地址.目前仅支持{@code #NETWORK_TYPE_CMWAP},{@code #NETWORK_TYPE_UNIWAP},
     * {@code #NETWORK_TYPE_3GWAP},{@code #NETWORK_TYPE_CTWAP}.四种.其余不进行代理.
     *
     * @param context 上下文
     * @return 代理地址.
     */
    public static String getProxy(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager
                .getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getExtraInfo() == null) {
            return null;
        }

        String info = networkInfo.getExtraInfo().toLowerCase(
                Locale.getDefault());
        if (info != null) {
            if (info.startsWith(NETWORK_TYPE_CMWAP)
                    || info.startsWith(NETWORK_TYPE_UNIWAP)
                    || info.startsWith(NETWORK_TYPE_3GWAP)) {
                return PROXY_CMWAP;
            } else if (info.startsWith(NETWORK_TYPE_CTWAP)) {
                return PROXY_CTWAP;
            }
        }
        return null;
    }

    /**
     * 获取url的host.
     *
     * @param url url地址.
     * @return host地址
     */
    public static String getUrlHost(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }
        String host = null;
        int hostIndex = SCHEMA_HTTP.length();
        int pathIndex = url.indexOf('/', hostIndex);
        if (pathIndex < 0) {
            host = url.substring(hostIndex);
        } else {
            host = url.substring(hostIndex, pathIndex);
        }
        return host;
    }

    /**
     * 检测网络是否可用.wifi和手機有一種可用即爲可用.
     *
     * @param context 上下文对象.此处请使用application的context.
     * @return true可用.false不可用.
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        return isNetworkActive(info);
    }

    /**
     * 移动数据网络是否可用.
     *
     * @param context context.此处请使用application的context.
     * @return 是否可用.true可用.false不可用.
     */
    public static boolean isMobileNetAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return isNetworkActive(info);
    }

    /**
     * WIFI连接是否可用.
     *
     * @param context 上下文對象.此处请使用application的context.
     * @return wifi連接是否可用.true可用.false不可用.
     */
    public static boolean isWifiAvailable(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return isNetworkActive(info);
    }

    /**
     * 指定类型的连接是否可用.
     *
     * @param info networkinfo.
     * @return 是否可用.true可用, false不可用.
     */
    private static boolean isNetworkActive(NetworkInfo info) {
        if ((info != null) && info.isConnected() && info.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * url encode.
     *
     * @param url url
     * @return encode后的值.
     */
    public static String urlEncode(String url) {
        try {
            return URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("urlencode", e.getMessage(), e);
        } catch (Exception e) {
            Log.e("urlencode", e.getMessage(), e);
        }
        return "";
    }

    /**
     * url encode.
     *
     * @param url     url
     * @param charset 编码.
     * @return encode后的值.
     */
    public static String urlEncode(String url, String charset) {
        try {
            return URLEncoder.encode(url, charset);
        } catch (UnsupportedEncodingException e) {
            Log.e("urlencode", e.getMessage(), e);
        }
        return "";
    }

    /**
     * url decode.
     *
     * @param url url
     * @return decode后的值.
     */
    public static String urlDecode(String url) {
        try {
            return URLDecoder.decode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e("urlencode", e.getMessage(), e);
        } catch (Exception e) {
            Log.e("urlencode", e.getMessage(), e);
        }
        return "";
    }

    /**
     * url decode.
     *
     * @param url     url
     * @param charset 编码.
     * @return decode后的值.
     */
    public static String urlDecode(String url, String charset) {
        try {
            return URLDecoder.decode(url, charset);
        } catch (UnsupportedEncodingException e) {
            Log.e("urlencode", e.getMessage(), e);
        }
        return "";
    }

    /**
     * 获取网络类型，GSM等
     *
     * @param context 上下文
     * @return
     */
    public static String getNetworkTypeStr(Context context) {
        String type = "";

        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        int networkType = tm.getNetworkType();

        if (activeNetInfo == null) {
            type = "NO_NETWORK";
        } else if (activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            type = "WIFI";
        } else {
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    type = "1xRTT";
                    break;
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    type = "CDMA";
                    break;
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    type = "EDGE";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    type = "EVDO_0";
                    break;
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    type = "EVDO_A";
                    break;
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    type = "GPRS";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    type = "HSDPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    type = "HSPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    type = "HSUPA";
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    type = "UMTS";
                    break;
                default:
                    type = "UNKNOWN";
                    break;
            }
        }

        return type;
    }
}
