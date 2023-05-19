package com.pccw.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class DateUtils
 *
 * @author KennySu
 * @date 2023/5/19
 */
public class DateUtils {

    public static String getDataTime() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH时mm分ss秒");
        LocalDateTime now = LocalDateTime.now();

        return dateTimeFormatter.format(now);
    }
}
