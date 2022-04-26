package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import javafx.util.Pair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@EqualsAndHashCode
public class SPair {
    private String id;
    private Token token0;
    private Token token1;
    private BigDecimal reserve0;
    private BigDecimal reserve1;
    private BigInteger intReserve0;
    private BigInteger intReserve1;
    private BigDecimal token0Price;
    private BigDecimal token1Price;

//    public BigInteger calToken0Price()
//    {
//        return intReserve0.multiply(BigInteger.TEN.pow(token0.getDecimals())).divide(intReserve1);
//    }
//
//    public BigInteger calToken1Price()
//    {
//        return intReserve1.multiply(BigInteger.TEN.pow(token1.getDecimals())).divide(intReserve0);
//    }

    public Token getTokenById(String id)
    {
        if(token0.getId().equals(id))
        {
            return token0;
        }else if(token1.getId().equals(id))
        {
            return token1;
        }
        return null;
    }

    public BigInteger getReserveById(String id)
    {
        if(token0.getId().equals(id))
        {
            return intReserve0;
        }else if(token1.getId().equals(id))
        {
            return intReserve1;
        }
        return null;
    }

    public void parseReserve()
    {
        intReserve0 = reserve0.multiply(BigDecimal.valueOf(10L).pow(token0.getDecimals())).toBigInteger();
        intReserve1 = reserve1.multiply(BigDecimal.valueOf(10L).pow(token1.getDecimals())).toBigInteger();
    }

    public static BigInteger getAmountOut(BigInteger amountIn,BigInteger reserveIn,BigInteger reserveOut)
    {
        BigInteger amountInWithFee = amountIn.multiply(BigInteger.valueOf(997L));
        BigInteger numerator = amountInWithFee.multiply(reserveOut);
        BigInteger denominator = reserveIn.multiply(BigInteger.valueOf(1000L)).add(amountInWithFee);
        return numerator.divide(denominator);
    }

    public static BigInteger getAmountIn(BigInteger amountOut,BigInteger reserveIn,BigInteger reserveOut)
    {
        if(amountOut.compareTo(reserveOut)>=0)
        {
            throw new RuntimeException("输出不能大于库存");
        }
        BigInteger numerator = reserveIn.multiply(amountOut).multiply(BigInteger.valueOf(1000));
        BigInteger denominator = reserveOut.subtract(amountOut).multiply(BigInteger.valueOf(997));
        return numerator.divide(denominator).add(BigInteger.valueOf(1));
    }

    public BigInteger getAmountOut(String tokenIdIn,BigInteger amountIn)
    {
        BigInteger reserveIn = tokenIdIn.equals(token0.getId())?intReserve0:intReserve1;
        BigInteger reserveOut = tokenIdIn.equals(token0.getId())?intReserve1:intReserve0;
        return getAmountOut(amountIn,reserveIn,reserveOut);
    }
    public BigInteger getAmountIn(String tokenIdOut,BigInteger amountOut)
    {
        BigInteger reserveIn = tokenIdOut.equals(token0.getId())?intReserve1:intReserve0;
        BigInteger reserveOut = tokenIdOut.equals(token0.getId())?intReserve0:intReserve1;
        return getAmountIn(amountOut,reserveIn,reserveOut);
    }

    public Token[] sort()
    {
        BigInteger id0= HexUtil.toBigInteger(token0.getId().substring(2));
        BigInteger id1= HexUtil.toBigInteger(token1.getId().substring(2));
        return id0.compareTo(id1)<0? new Token[]{token0,token1}:new Token[]{token1,token0};
    }
}
