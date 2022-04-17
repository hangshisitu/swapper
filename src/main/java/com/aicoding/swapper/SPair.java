package com.aicoding.swapper;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

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
}
