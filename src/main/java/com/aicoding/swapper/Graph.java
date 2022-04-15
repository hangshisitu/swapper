package com.aicoding.swapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 图
 *
 * @Date: 2022/4/15 12:09
 * @Author: qiaojun.xiao
 * @Copyright（C）: 2019-2022 ZP Inc.   All rights reserved.
 * 注意：本内容仅限于佐朋数科内部传阅，禁止外泄以及用于其他的商业目的。
 */
@Data
@ToString
@AllArgsConstructor
@Slf4j
public class Graph {
    /**
     * 顶点
     */
    @Data
    @ToString
    @AllArgsConstructor
    public static class Vertex{
        private Token token;
    }

    /**
     * 边
     */
    @Data
    @ToString
    @AllArgsConstructor
    public static class Edge{
        private Pair pair;
        private Long weight;
    }

    private Vertex[] vertices;

    private Edge[][] edges;

    public Graph(List<Pair> pairList)
    {
        Set<Token> temp = new HashSet<>(pairList.size()*2);
        for (int i=0;i<pairList.size();++i)
        {
            Token token0 = pairList.get(i).getToken0();
            Token token1 = pairList.get(i).getToken0();
            temp.add(token0);
            temp.add(token1);
        }
        vertices = temp.stream().map(t -> new Vertex(t)).collect(Collectors.toList()).toArray(new Vertex[0]);
        Map<String,Integer> id2Index = new HashMap<>(vertices.length);
        for (int i=0;i<vertices.length;++i)
        {
            id2Index.put(vertices[i].getToken().getId(),i);
        }
        edges = new Edge[vertices.length][vertices.length];
        pairList.forEach(p -> {
            if(!id2Index.containsKey(p.getToken0().getId()) ||
            !id2Index.containsKey(p.getToken1().getId()))
            {
                log.error("not index {}",p);
            }
            int i = id2Index.get(p.getToken0().getId());
            int j = id2Index.get(p.getToken1().getId());
            edges[i][j] = new Edge(p,0L);
            edges[j][i] = edges[i][j];
        });
    }

    public Boolean hasRing()
    {
        for(int i=0;i<edges.length;++i)
        {
            List<String> path = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            if(hasRingDfs(i,path,-1,visited))
            {
                return true;
            }
        }
        return false;
    }

    private boolean hasRingDfs(Integer depth, List<String> path, Integer pre, Set<Integer> visited)
    {
        for(Integer i=0;i<edges.length;++i)
        {
            //不是刚出发的节点
            if(pre.equals(-1) || !i.equals(pre))
            {
                if(!ObjectUtils.isEmpty(edges[depth][i]))
                {
                    if(visited.contains(i))
                    {
                        //找到一个环
                        return true;
                    }else
                    {   //无环
                        path.add(vertices[i].getToken().getId());
                        if(hasRingDfs(i,path,depth,visited))
                        {
                            return true;
                        }
                        path.remove(vertices[i].getToken().getId());
                    }
                }
            }
        }
        return false;
    }

    /**
     * 假设无环, 深度优先遍历
     * @param depth
     * @param path
     * @param pre
     */
    private void dfs(Integer depth, List<String> path,Integer pre, List<List<String>> result)
    {
        for(Integer i=0;i<edges.length;++i)
        {
            //不是刚出发的节点
            if(!i.equals(pre))
            {
                if(!ObjectUtils.isEmpty(edges[depth][i]))
                {
                    path.add(vertices[i].getToken().getSymbol());
                    dfs(i,path,depth,result);
                    path.remove(vertices[i].getToken().getSymbol());
                }
            }
        }
        result.add(path);
    }
}
