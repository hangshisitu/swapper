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

	private Map<String,Map<String,SPair>> paris= new HashMap<>();

	private Map<String,Token> tokens = new HashMap<>();

	private static final BigDecimal reserveThreshold = new BigDecimal("0.00001");
	@Override
	public void run(String... args) throws Exception {
		List<SPair> sPairs = queryAllPairs();
		log.info("pairs size {}", sPairs.size());
		List<SPair> valuePairs = sPairs.stream().filter(
				p -> p.getReserve0().compareTo(reserveThreshold)>=0 && p.getReserve1().compareTo(reserveThreshold)>=0)
				.collect(Collectors.toList());

		valuePairs.stream().forEach(p ->{
			Token[] temp = p.sort();
			Map<String,SPair> pairMap = paris.getOrDefault(temp[0].getId(),new HashMap<>());
			pairMap.put(temp[1].getId(),p);
			if(!paris.containsKey(temp[0].getId()))
			{
				paris.put(temp[0].getId(),pairMap);
			}
			tokens.put(temp[0].getId(),temp[0]);
			tokens.put(temp[1].getId(),temp[1]);
		});

		AdjacencyListGraph adGraph = new AdjacencyListGraph(valuePairs);
		log.info("has ring: {}",adGraph.hasRing());
		long start = System.currentTimeMillis();
		List<List<String>> rings = adGraph.rings();

		rings.stream().forEach(r -> {
			BigDecimal tmp = new BigDecimal("0.00001");
			BigDecimal needRepay = getAmountIn(tmp,new String[]{r.get(1),r.get(0)});
			String[] path = new String[r.size()];
			path[0] = r.get(0);
			int j=1;
			for(int i=r.size()-1;i>0;--i)
			{
				path[j] = r.get(i);
				++j;
			}
			BigDecimal receive = getAmountOut(tmp,path);
			if(receive.compareTo(needRepay)>0)
			{
				log.info("可套利环: {}, amount: {}",formatRing(r),receive.subtract(needRepay));
			}
		});
		log.info("duration: {} min",(System.currentTimeMillis()-start)/1000/60);
	}

	private List<String> formatRing(List<String> ring)
	{
		return ring.stream().map(t -> tokens.get(t).getSymbol()).collect(Collectors.toList());
	}
	public  BigDecimal getAmountIn(BigDecimal amountOut,String[] path)
	{
		BigDecimal[] amounts = new BigDecimal[path.length];
		amounts[path.length-1] = amountOut;
		for(int i=path.length-1;i>0;i--)
		{
			BigInteger id0= HexUtil.toBigInteger(path[i].substring(2));
			BigInteger id1= HexUtil.toBigInteger(path[i-1].substring(2));
			String token0Id = id0.compareTo(id1)<0?path[i]:path[i-1];
			String token1Id = id0.compareTo(id1)<0?path[i-1]:path[i];
			amounts[i-1] = paris.get(token0Id).get(token1Id).getAmountIn(path[i-1],amounts[i]);
		}
		return amounts[0];
	}

	public  BigDecimal getAmountOut(BigDecimal amountIn,String[] path)
	{
		BigDecimal[] amounts = new BigDecimal[path.length];
		amounts[0] = amountIn;
		for(int i=0;i<path.length-1;i++)
		{
			BigInteger id0= HexUtil.toBigInteger(path[i].substring(2));
			BigInteger id1= HexUtil.toBigInteger(path[i+1].substring(2));
			String token0Id = id0.compareTo(id1)<0?path[i]:path[i+1];
			String token1Id = id0.compareTo(id1)<0?path[i+1]:path[i];
			amounts[i+1] = paris.get(token0Id).get(token1Id).getAmountOut(path[i],amounts[i]);
		}
		return amounts[path.length-1];
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
