package cn.jiguang.imui.utils;

import android.util.Log;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author fuxiaosong
 * @version 1.0.0
 * @since 2017年05月31日
 */
public class LinkUtil {
    /**
     *
     * @param source
     * @return
     */
    public static Object[][] parseLinks(String source) {
        ArrayList<String> hosts = new ArrayList<>(4);
        ArrayList<String> urls = new ArrayList<>(4);
        ArrayList<Integer> starts = new ArrayList<>(4);
        ArrayList<Integer> ends = new ArrayList<>(4);
        Pattern pattern = Pattern.compile("<a href=\".*?\">(.*?)</a>");//首先将a标签分离出来
        Matcher matcher = pattern.matcher(source);
        int i=0;
        while(matcher.find()){
            String raw = matcher.group(0);
            Pattern url_pattern = Pattern.compile("<a href=\"(.*?)\">");//将href分离出来
            Matcher url_matcher = url_pattern.matcher(raw);
            try {
                if(url_matcher.find()){
                    String url = url_matcher.group(1);
                    Log.i("LinkUtil","真实url是："+url);//括号里面的
                    urls.add(i,url);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            String host = null;//将要显示的文字分离出来
            try {
                host = matcher.group(1);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Log.i("LinkUtil","蓝色文字是："+host);//括号里面的
            hosts.add(i,host);
            starts.add(i,matcher.start());
            ends.add(i,matcher.end());
            Log.i("LinkUtil","字符串起始下标是："+matcher.start()+"，结尾下标是："+matcher.end());//匹配出的字符串在源字符串的位置
            i++;
        }
        if(hosts.size()==0){
            Log.i("LinkUtil","没有发现url");
            return null;
        }
        Object[][] outputs = new Object[4][hosts.size()];//第一个下标是内容的分类，第二个下标是url的序号
        outputs[0] = hosts.toArray(new String[hosts.size()]);//下标0是蓝色的文字
        outputs[1] = urls.toArray(new String[urls.size()]);//下标1是url
        outputs[2] = starts.toArray(new Integer[starts.size()]);//下标2是<a>标签起始位置
        outputs[3] = ends.toArray(new Integer[ends.size()]);//下标3是<a>标签结束位置
        return outputs;
    }
}
