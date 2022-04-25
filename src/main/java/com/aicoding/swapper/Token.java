package com.aicoding.swapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * erc20 token
 *
 * @Date: 2022/4/13 12:53
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Data
@ToString
public class Token {
    private String id;
    private String symbol;
    private String name;
    private Integer decimals;
    private BigDecimal  derivedETH;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return id.equals(token.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public BigDecimal value(BigInteger v)
    {
        return new BigDecimal(v).divide(BigDecimal.valueOf(10).pow(decimals),decimals,BigDecimal.ROUND_DOWN);
    }
}
