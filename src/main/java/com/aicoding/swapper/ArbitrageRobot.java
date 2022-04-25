package com.aicoding.swapper;

import cn.hutool.core.util.HexUtil;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 套利机器人
 *
 * @Date: 2022/4/22 14:41
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Slf4j
public class ArbitrageRobot {

    private List<SPair> pairs;
    private Map<String, Map<String,SPair>> paris0To1= new HashMap<>();
    private Map<String,Token> tokens = new HashMap<>();

    private static final BigDecimal reserveThreshold = new BigDecimal("0.00001");

    private List<String> stableCoinList;

    /**
     * WETH,DAI,USDT,USDC,WBTC
     */
    private final String[] stableCoin = new String[]{
            "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
    };

//    private final String[] stableCoin = new String[]{
//            "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2",
//            "0x6B175474E89094C44Da98b954EedeAC495271d0F",
//            "0xdAC17F958D2ee523a2206206994597C13D831ec7",
//            "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//            "0x2260FAC5E5542a773Aa44fBCfeDf7C193bc2C599"
//    };

    public ArbitrageRobot(List<SPair> pairs)
    {
        pairs.stream().forEach(p ->{
            Map<String,SPair> pairMap = paris0To1.getOrDefault(p.getToken0().getId(),new HashMap<>());
            pairMap.put(p.getToken1().getId(),p);
            if(!paris0To1.containsKey(p.getToken0().getId()))
            {
                paris0To1.put(p.getToken0().getId(),pairMap);
            }
            tokens.put(p.getToken0().getId(),p.getToken0());
            tokens.put(p.getToken1().getId(),p.getToken1());
        });
        stableCoinList = Arrays.asList(stableCoin).stream().map(t -> t.toLowerCase()).collect(Collectors.toList());
        this.pairs = pairs;
    }

    public void arbitrageCal()
    {
        //过滤pairs
        List<SPair> valuePairs = pairs.stream().filter(
                p -> p.getReserve0().compareTo(reserveThreshold)>=0 && p.getReserve1().compareTo(reserveThreshold)>=0)
                .collect(Collectors.toList());

        //查找环
        AdjacencyListGraph adGraph = new AdjacencyListGraph(valuePairs);
        List<List<String>> rings = adGraph.rings();

        //过滤不包含稳定币的环
        List<String> stableCoinList = Arrays.asList(stableCoin).stream().map(t -> t.toLowerCase()).collect(Collectors.toList());
        List<List<String>> valueRings = rings.stream().filter(
                r -> r.stream().filter(t -> stableCoinList.contains(t)).findAny().isPresent()).collect(Collectors.toList());

        //旋转环,将稳定币转到开始位置
        List<List<String>> readyRings = valueRings.stream().map(r -> spinRing(r)).collect(Collectors.toList());
        readyRings.stream().sorted((a,b)->a.size()-b.size());


        //计算套利空间
        readyRings.stream().forEach(r -> {
            log.info("备选环: {}",formatRing(r));
            BigInteger profit = calMaxProfit(r);
            if(profit.compareTo(BigInteger.ZERO)>0)
            {
                log.info("可套利环: {}, profit: {}, {}",formatRing(r),profit,tokens.get(r.get(0)).value(profit));
            }
        });
    }


    public void crossArbitrageCal(List<SPair> sushiParis)
    {
        String weth = stableCoin[0].toLowerCase();
        paris0To1.get(weth).values().stream().forEach(p -> {
            Optional<SPair> externalPair = sushiParis.stream().filter(
                    s -> s.getToken0().equals(p.getToken0()) && s.getToken1().equals(p.getToken1())).findAny();
            if(externalPair.isPresent())
            {
                if(!p.getIntReserve0().equals(BigInteger.ZERO) && !p.getIntReserve1().equals(BigInteger.ZERO))
                {
                    SPair sushiPair = externalPair.get();
                    if(sushiPair.calToken0Price().compareTo(BigInteger.ZERO)>0 && sushiPair.calToken1Price().compareTo(BigInteger.ZERO)>0)
                    {
                        Pair<Boolean,BigInteger> temp = UniswapV2LiquidityMathUtils.computeProfitMaximizingTrade(
                                sushiPair.calToken0Price(),sushiPair.calToken1Price(),p.getIntReserve1(),p.getIntReserve0());
                        if(temp.getKey())
                        {
                            //有足够的库存可以借用
                            if(sushiPair.getIntReserve1().compareTo(temp.getValue())>0)
                            {
                                BigInteger debit = temp.getValue();
                                BigInteger needRepay = sushiPair.getAmountIn(p.getToken1().getId(),debit);
                                BigInteger receive = p.getAmountOut(p.getToken1().getId(),debit);
                                if(receive.compareTo(needRepay)>0)
                                {
                                    log.info("可套利: profit {}, sushiPair {},pair {}",receive.subtract(needRepay),sushiPair,p);
                                }
                            }
                        }else if (temp.getValue().compareTo(BigInteger.ZERO)>0)
                        {
                            if(sushiPair.getIntReserve0().compareTo(temp.getValue())>0)
                            {
                                BigInteger debit = temp.getValue();
                                BigInteger needRepay = sushiPair.getAmountIn(p.getToken0().getId(),debit);
                                BigInteger receive = p.getAmountOut(p.getToken0().getId(),debit);
                                if(receive.compareTo(needRepay)>0)
                                {
                                    log.info("可套利: profit {}, sushiPair {},pair {}",receive.subtract(needRepay),sushiPair,p);
                                }
                            }
                        }
                    }else
                    {
                        log.error("true price is zero");
                    }

                }else
                {
                    log.info("库存为0 pair{}",p);
                }

            }
        });
    }

    private List<String> formatRing(List<String> ring)
    {
        return ring.stream().map(t -> tokens.get(t).getSymbol()).collect(Collectors.toList());
    }

    private BigInteger calMaxProfit(List<String> ring)
    {
        BigInteger maxProfit = BigInteger.valueOf(Integer.MIN_VALUE);
        Integer[] percents = new Integer[]{1,3,5,8,10,30,50};
        for (int i=0;i<percents.length;++i)
        {
             maxProfit = Utils.max(maxProfit,Utils.max(calArbitrageAmount(ring,percents[i]),calArbitrageAmountEx(ring,percents[i])));
        }
        return maxProfit;
    }

    /**
     * 旋转环，将稳定币转到开始位置
     * @param ring
     * @return
     */
    private List<String> spinRing(List<String> ring)
    {
        String stableCoin = stableCoinList.stream().filter(t ->ring.contains(t)).findFirst().get();
        int index = ring.indexOf(stableCoin);
        String[] temp = new String[ring.size()];
        int step = index-0;
        for(int i=0;i<ring.size();++i)
        {
            if(i-step>=0)
            {
                temp[i-step] = ring.get(i);
            }else
            {
                temp[ring.size()+i-step] = ring.get(i);
            }
        }
        return Arrays.stream(temp).collect(Collectors.toList());
    }

    private BigInteger calArbitrageAmount(List<String> path,Integer percent)
    {
        SPair pair = getPair(path.get(0),path.get(1));
        BigInteger debit= pair.getReserveById(path.get(1)).multiply(BigInteger.valueOf(percent)).divide(BigInteger.valueOf(10000L));
        BigInteger needRepay = pair.getAmountIn(path.get(1),debit);

        List<String> temp = new ArrayList<>();
        temp.addAll(path.subList(1,path.size()));
        temp.add(path.get(0));
        BigInteger receive = getAmountOut(debit,temp.toArray(new String[0]));
        return receive.subtract(needRepay);
    }

    private BigInteger calArbitrageAmountEx(List<String> path,Integer percent)
    {
        SPair pair = getPair(path.get(0),path.get(path.size()-1));
        BigInteger debit= pair.getReserveById(path.get(path.size()-1)).multiply(BigInteger.valueOf(percent)).divide(BigInteger.valueOf(10000L));
        BigInteger needRepay = pair.getAmountIn(path.get(path.size()-1),debit);

        List<String> temp = new ArrayList<>();
        temp.addAll(path);
        Collections.reverse(temp);
        BigInteger receive = getAmountOut(debit,temp.toArray(new String[0]));
        return receive.subtract(needRepay);
    }

    private SPair getPair(String id0,String id1)
    {
        if(paris0To1.containsKey(id0) && paris0To1.get(id0).containsKey(id1))
        {
            return paris0To1.get(id0).get(id1);
        }
        if(paris0To1.containsKey(id1) && paris0To1.get(id1).containsKey(id0))
        {
            return paris0To1.get(id1).get(id0);
        }
        log.error("id0:{},id1:{}",id0,id1);
        return null;
    }

    private BigInteger getAmountIn(BigInteger amountOut, String[] path)
    {
        BigInteger[] amounts = new BigInteger[path.length];
        amounts[path.length-1] = amountOut;
        for(int i=path.length-1;i>0;i--)
        {
            SPair pair = getPair(path[i],path[i-1]);
            amounts[i-1] = pair.getAmountIn(path[i-1],amounts[i]);
        }
        return amounts[0];
    }

    private  BigInteger getAmountOut(BigInteger amountIn, String[] path)
    {
        BigInteger[] amounts = new BigInteger[path.length];
        amounts[0] = amountIn;
        for(int i=0;i<path.length-1;i++)
        {
            SPair pair = getPair(path[i],path[i+1]);
            amounts[i+1] = pair.getAmountOut(path[i],amounts[i]);
        }
        return amounts[path.length-1];
    }
}
