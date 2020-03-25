package com.zhangwuji.im.api.common;

import java.math.BigDecimal;
import java.text.ParseException;

public final class MathTool {

    public static String round(double num, int position)
    {
        return String.format("%." + position + "f", new Object[] { Double.valueOf(num) });
    }

    public static final byte[] int2Byte(int i)
    {
        byte[] targets = new byte[4];
        targets[0] = ((byte)(i & 0xFF));
        targets[1] = ((byte)(i >> 8 & 0xFF));
        targets[2] = ((byte)(i >> 16 & 0xFF));
        targets[3] = ((byte)(i >>> 24));
        return targets;
    }

    public static final byte[] short2Byte(short s)
    {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++)
        {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = ((byte)(s >>> offset & 0xFF));
        }
        return targets;
    }

    public static final BigDecimal multiply(Object... num)
    {
        BigDecimal n1 = BigDecimal.ONE;

        Object[] arrayOfObject = num;
        int j = num.length;
        for (int i = 0; i < j; i++)
        {
            Object n = arrayOfObject[i];
            n1 = n1.multiply(new BigDecimal(String.valueOf(n)));
        }
        return n1;
    }

    public static final BigDecimal multiplyRoundDown(int scale, Object[] num)
    {
        BigDecimal a = multiply(num);
        BigDecimal b = a.setScale(scale, 1);
        if (a.compareTo(b) > 0) {
            return add(new Object[] { b, Double.valueOf(0.01D) });
        }
        return b;
    }

    public static final BigDecimal multiplyRoundUp(int scale, Object[] num)
    {
        BigDecimal a = multiply(num);
        BigDecimal b = a.setScale(scale, 0);
        return b;
    }

    public static final BigDecimal multiplyRoundHalfUp(int scale, Object[] num)
    {
        BigDecimal a = multiply(num);
        BigDecimal b = a.setScale(scale, 4);
        return b;
    }

    public static final BigDecimal divideToScale(int scale, Object[] num)
    {
        BigDecimal n1 = new BigDecimal(String.valueOf(num[0]));
        int len = num.length;
        for (int i = 1; i < len; i++) {
            n1 = n1.divide(new BigDecimal(String.valueOf(num[i])), scale, 4);
        }
        return n1;
    }

    public static final BigDecimal divide(Object... num)
    {
        return divideToScale(2, num);
    }

    public static final BigDecimal add(Object... num)
    {
        BigDecimal n1 = new BigDecimal(0);

        Object[] arrayOfObject = num;
        int j = num.length;
        for (int i = 0; i < j; i++)
        {
            Object n = arrayOfObject[i];
            n1 = n1.add(new BigDecimal(String.valueOf(n)));
        }
        return n1;
    }

    public static final BigDecimal subtract(Object... num)
    {
        BigDecimal n1 = new BigDecimal(String.valueOf(num[0]));
        int len = num.length;
        for (int i = 1; i < len; i++) {
            n1 = n1.subtract(new BigDecimal(String.valueOf(num[i])));
        }
        return n1;
    }

    public static void main(String[] args) throws ParseException
    {
        System.out.println(multiplyRoundHalfUp(0, new Object[] { Double.valueOf(2.644D) }));
    }
}
