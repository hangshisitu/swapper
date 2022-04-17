package com.aicoding.swapper;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @Date: 2022/4/15 19:04
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Data
@ToString
public class PairsDto {
    private List<SPair> pairs;
}
