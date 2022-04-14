package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
class SwapperApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void testHexStr()
	{
		System.out.println(HexUtil.toBigInteger("12b5c672b6470ec245f8c640b0cf2aaef3add054"));
	}
}
