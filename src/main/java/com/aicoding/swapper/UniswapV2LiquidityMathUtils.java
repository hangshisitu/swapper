package com.aicoding.swapper;

import javafx.util.Pair;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.BreakIterator;

/**
 * 流动性计算工具
 *
 * @Date: 2022/4/25 10:55
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
public class UniswapV2LiquidityMathUtils {


    public static Pair<Boolean, BigInteger> computeProfitMaximizingTrade(
            BigInteger truePriceTokenA,
            BigInteger truePriceTokenB,
            BigInteger reserveA,
            BigInteger reserveB
    )
    {
        Boolean aToB = reserveA.multiply(truePriceTokenB).divide(reserveB).compareTo(truePriceTokenA)<0;
        BigInteger invariant = reserveA.multiply(reserveB);
        BigInteger leftSide = Utils.sqrt(invariant.multiply(Utils.C1000).multiply(
                aToB? truePriceTokenA:truePriceTokenB).divide((aToB? truePriceTokenB:truePriceTokenA).multiply(Utils.C997)));
        BigInteger rightSide = (aToB?reserveA.multiply(Utils.C1000):reserveB.multiply(Utils.C1000)).divide(Utils.C997);

        if(leftSide.compareTo(rightSide)<0)
        {
            return new Pair<Boolean, BigInteger>(false,BigInteger.ZERO);
        }
        return new Pair<>(aToB,leftSide.subtract(rightSide));
    }
}
