package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import com.alibaba.fastjson.JSONObject;
import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import graphql.kickstart.spring.webclient.boot.GraphQLResponse;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class SwapperApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SwapperApplication.class, args);
	}

	@Autowired
	GraphQLWebClient graphQLWebClient;

	@Override
	public void run(String... args) throws Exception {
		List<Pair> pairs = queryAllPairs();
		log.info("pairs size {}",pairs.size());
		Set<BigInteger> tokens = new HashSet<>(pairs.size()*2);
		pairs.stream().forEach(p ->{
			tokens.add(HexUtil.toBigInteger(p.getToken0().getId().substring(2)));
			tokens.add(HexUtil.toBigInteger(p.getToken0().getId().substring(2)));
		});
		List<BigInteger> tokenList = tokens.stream().collect(Collectors.toList());
		Collections.sort(tokenList);
		Boolean[][] nodeGraph = new Boolean[tokenList.size()][tokenList.size()];
		Map<BigInteger,Integer> token2Index = new HashMap<>(tokenList.size());
		for (int i=0;i<tokenList.size();++i)
		{
			token2Index.put(tokenList.get(i),i);
		}
		pairs.stream().forEach(p -> {
			int i= token2Index.get(HexUtil.toBigInteger(p.getToken0().getId().substring(2)));
			int j = token2Index.get(HexUtil.toBigInteger(p.getToken1().getId().substring(2)));
			nodeGraph[i][j] = true;
			nodeGraph[j][i] = true;
		});


	}

	private List<Pair> queryAllPairs()
	{
		List<Pair> result = new ArrayList<>();
		Cursor cursor = new Cursor(0);
		GraphQLRequest graphqlRequest = GraphQLRequest.builder()
				.resource("pairs.graphql")
				.variables(cursor)
				.build();

		do{
			log.info("size:{} cursor:{}",result.size(),cursor);
			Mono<GraphQLResponse> responseMono =  graphQLWebClient.post(graphqlRequest);
			try {
				responseMono.block().validateNoErrors();
			}catch (Exception e)
			{
				log.error("exception： {}",e);
				return result;
			}
			List<Pair> temp = responseMono.block().getFirstList(Pair.class);
			result.addAll(temp);
			cursor.setSkip(cursor.getSkip()+temp.size());
		}while(cursor.getSkip()<=5000);

		return result;
	}
}
