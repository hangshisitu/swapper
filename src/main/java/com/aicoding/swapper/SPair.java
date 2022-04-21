package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import javafx.util.Pair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 交易对
 *
 * @Date: 2022/4/12 13:00
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Data
@ToString
@EqualsAndHashCode
public class SPair {
    private String id;
    private Token token0;
    private Token token1;
    private BigDecimal reserve0;
    private BigDecimal reserve1;

    public static BigDecimal getAmountOut(BigDecimal amountIn,BigDecimal reserveIn,BigDecimal reserveOut)
    {
        BigDecimal amountInWithFee = amountIn.multiply(BigDecimal.valueOf(997));
        BigDecimal numerator = amountInWithFee.multiply(reserveOut);
        BigDecimal denominator = reserveIn.multiply(BigDecimal.valueOf(1000)).add(amountInWithFee);
        return numerator.divide(denominator,18,BigDecimal.ROUND_FLOOR);
    }

    public static BigDecimal getAmountIn(BigDecimal amountOut,BigDecimal reserveIn,BigDecimal reserveOut)
    {
        BigDecimal numerator = reserveIn.multiply(amountOut).multiply(BigDecimal.valueOf(1000));
        BigDecimal denominator = reserveOut.subtract(amountOut).multiply(BigDecimal.valueOf(997));
        //TODO 确认公式是否正确
        return numerator.divide(denominator,18,BigDecimal.ROUND_FLOOR).add(BigDecimal.valueOf(1));
    }

    public BigDecimal getAmountOut(String tokenIdIn,BigDecimal amountIn)
    {
        BigDecimal reserveIn = tokenIdIn.equals(token0.getId())?reserve0:reserve1;
        BigDecimal reserveOut = tokenIdIn.equals(token0.getId())?reserve1:reserve0;
        return getAmountOut(amountIn,reserveIn,reserveOut);
    }
    public BigDecimal getAmountIn(String tokenIdOut,BigDecimal amountOut)
    {
        BigDecimal reserveIn = tokenIdOut.equals(token0.getId())?reserve1:reserve0;
        BigDecimal reserveOut = tokenIdOut.equals(token0.getId())?reserve0:reserve1;
        return getAmountIn(amountOut,reserveIn,reserveOut);
    }

    public Token[] sort()
    {
        BigInteger id0= HexUtil.toBigInteger(token0.getId().substring(2));
        BigInteger id1= HexUtil.toBigInteger(token1.getId().substring(2));
        return id0.compareTo(id1)<0? new Token[]{token0,token1}:new Token[]{token1,token0};
    }
}
