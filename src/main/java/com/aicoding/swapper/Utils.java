package com.aicoding.swapper;

import java.math.BigInteger;

/**
 * 工具类
 *
 * @Date: 2022/4/22 16:39
 * @Author: Moffatt
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
public class Utils {

    public final static BigInteger C1000 = BigInteger.valueOf(1000);
    public final static BigInteger C997 = BigInteger.valueOf(997);

    public static BigInteger max(BigInteger a, BigInteger b)
    {
        return a.compareTo(b)>=0?a:b;
    }

    public static BigInteger min(BigInteger a, BigInteger b)
    {
        return a.compareTo(b)<=0?a:b;
    }

    public static BigInteger sqrt(BigInteger a)
    {
        BigInteger h = a;
        BigInteger l = BigInteger.valueOf(0);
        BigInteger ans = null;
        while(l.compareTo(h)<=0)
        {
            BigInteger mid = h.add(l).divide(BigInteger.valueOf(2));
            int c = mid.pow(2).compareTo(a);
            if(c<=0)
            {
                ans = mid;
                l = mid.add(BigInteger.ONE);
            }else {
                h = mid.subtract(BigInteger.ONE);
            }
        }
        return ans;
    }
}
