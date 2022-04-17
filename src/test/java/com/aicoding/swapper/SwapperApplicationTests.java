package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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
}
