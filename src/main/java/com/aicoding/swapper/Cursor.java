package com.aicoding.swapper;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 游标对象
 *
 * @Date: 2022/4/13 11:41
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Data
@AllArgsConstructor
public class Cursor {
    private Integer skip;
}
