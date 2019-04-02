package salorsmile.lzh.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Administrator on 2018/9/3.
 * 工具类：封装一些全局的工具方法，在使用时可以直接调用
 */

public class Global {
    //把毫秒值转化为格式字符串的方法mm:ss
    public static String setTimes(int time){
        SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
        //把毫秒值转化为Date对象
        Date date = new Date();
        date.setTime(time);
        //把Date对象转化为格式字符串
        String strTime = sdf.format(date);
        return strTime;
    }
}

