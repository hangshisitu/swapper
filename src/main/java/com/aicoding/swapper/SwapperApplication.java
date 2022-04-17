package com.aicoding.swapper;

import graphql.kickstart.spring.webclient.boot.GraphQLRequest;
import graphql.kickstart.spring.webclient.boot.GraphQLResponse;
import graphql.kickstart.spring.webclient.boot.GraphQLWebClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.*;

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
		List<SPair> SPairs = queryAllPairs();
		log.info("pairs size {}", SPairs.size());
		Graph graph = new Graph(SPairs);
//		log.info("has ring: {}",graph.hasRing());
//		log.info("rings: {}",graph.rings());
		log.info("fullRate:{}",graph.fullRate());
	}

	private List<SPair> queryAllPairs()
	{
		List<SPair> result = new ArrayList<>();
		Cursor cursor = new Cursor(0);
		do{
			log.info("size:{} cursor:{}",result.size(),cursor);
			PairsDto dto = graphQlUtils.graphQLQuery("pairs.graphql",cursor,PairsDto.class);
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
			result.addAll(temp);
			cursor.setSkip(cursor.getSkip()+temp.size());
		}while(cursor.getSkip()<=5000);

		return result;
	}
}
