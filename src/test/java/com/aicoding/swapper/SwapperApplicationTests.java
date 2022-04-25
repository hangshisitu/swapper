package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

	@Test
	void testProxy() throws URISyntaxException {
		URI uri = new URI("https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2");
		List<Proxy> proxyList = ProxySelector.getDefault().select(uri);
		System.out.println(proxyList.get(0).address());
		System.out.println(proxyList.get(0).type());
	}

	@Test
	void TestAmount()
	{
		System.out.println(new BigInteger("2007734677294003221625410").equals(
				new BigDecimal("2007734.67729400322162541").multiply(BigDecimal.valueOf(10L).pow(18)).toBigInteger()));
//		System.out.println(SPair.getAmountIn(BigDecimal.valueOf(0.005),BigDecimal.valueOf(0.1),BigDecimal.valueOf(0.2)));
	}

	@Test
	void testSqrt()
	{
		System.out.println(Utils.sqrt(BigInteger.valueOf(110)));
	}

}
