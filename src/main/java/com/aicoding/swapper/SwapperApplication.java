package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import graphql.kickstart.spring.webclient.boot.GraphQLResponse;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class SwapperApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(SwapperApplication.class, args);
	}

	@Autowired
	GraphQLWebClient graphQLWebClient;

	@Autowired
	GraphQlUtils graphQlUtils;

	@Override
	public void run(String... args) throws Exception {
		List<SPair> sPairs = queryAllPairs(true);
		log.info("pairs size {}", sPairs.size());
		sPairs.stream().forEach(p -> p.parseReserve());
		long start = System.currentTimeMillis();
		ArbitrageRobot robot = new ArbitrageRobot(sPairs);
//		robot.arbitrageCal();

		List<SPair> sushiPairs = queryAllPairs(false);
		sushiPairs.stream().forEach(p -> p.parseReserve());
		robot.crossArbitrageCal(sushiPairs);
		log.info("duration: {} min",(System.currentTimeMillis()-start)/1000/60);
	}


	private List<SPair> queryAllPairs(Boolean uniswap)
	{
		List<SPair> result = new ArrayList<>();
		Cursor cursor = new Cursor(0);
		do{
			log.info("size:{} cursor:{}",result.size(),cursor);
			PairsDto dto = graphQlUtils.graphQLQuery("pairs.graphql",cursor,PairsDto.class,uniswap);
			if(ObjectUtils.isEmpty(dto.getPairs()))
			{
				break;
			}
			result.addAll(dto.getPairs());
			cursor.setSkip(cursor.getSkip()+dto.getPairs().size());
		}while(cursor.getSkip()<=5000);

		return result;
	}

	private List<SPair> queryAllPairs2()
	{
		List<SPair> result = new ArrayList<>();
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
			List<SPair> temp = responseMono.block().getFirstList(SPair.class);
			if(ObjectUtils.isEmpty(temp))
			{
				break;
			}
			result.addAll(temp);
			cursor.setSkip(cursor.getSkip()+temp.size());

		}while(cursor.getSkip()<=5000);

		return result;
	}
}
