package com.aicoding.swapper;

import javafx.util.Pair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 邻接矩阵图
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
public class MatrixGraph {
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
        private SPair SPair;
        private Long weight;
    }

    private Vertex[] vertices;

    private Edge[][] edges;

    public MatrixGraph(List<SPair> SPairList)
    {
        Set<Token> temp = new HashSet<>(SPairList.size()*2);
        for (int i = 0; i< SPairList.size(); ++i)
        {
            Token token0 = SPairList.get(i).getToken0();
            Token token1 = SPairList.get(i).getToken1();
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
        SPairList.forEach(p -> {
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

    public double fullRate()
    {
        double nullCount=0.0;
        for (int i=0;i<edges.length;++i)
        {
            for (int j=0;j<edges.length;++j)
            {
                if(!ObjectUtils.isEmpty(edges[i][j]))
                {
                    nullCount +=1;
                }
            }
        }
        return nullCount/(edges.length*edges.length);
    }

    public Boolean hasRing()
    {
        for(int i=0;i<edges.length;++i)
        {
            List<String> path = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            if(hasRingDfs(i,path,-1,visited))
            {
                log.info("ring: {}",path);
                return true;
            }
        }
        return false;
    }

    public List<List<String>> rings()
    {
        List<List<Pair<Integer,Integer>>> result= new LinkedList<>();
        for(int i=0;i<vertices.length;++i)
        {
            List<Pair<Integer,Integer>> path = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            visited.add(i);
            ringDfs(i,i,path,-1,visited,result);
        }
        return result.stream().map(l -> {
            return l.stream().map(p -> {
                return String.format("%s->%s",vertices[p.getKey()].getToken().getSymbol(),
                        vertices[p.getValue()].getToken().getSymbol());
            }).collect(Collectors.toList());
        }).collect(Collectors.toList());
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
                        visited.add(i);
                        path.add(vertices[i].getToken().getSymbol());
                        if(hasRingDfs(i,path,depth,visited))
                        {
                            return true;
                        }
                        visited.remove(i);
                        path.remove(vertices[i].getToken().getSymbol());
                    }
                }
            }
        }
        return false;
    }

    private void ringDfs(Integer depth,
                         Integer start,
                         List<Pair<Integer,Integer>> path,
                         Integer pre,
                         Set<Integer> visited,
                         List<List<Pair<Integer,Integer>>> result)
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
                        if(start.equals(i))
                        {
                            log.info("a ring {}",path);
                            result.add(path);
                            //跳过这条边继续遍历
                        }
                        //跳过这条边继续遍历
                    }else
                    {   //无环
                        visited.add(i);
                        Pair<Integer,Integer> temp = new Pair<>(depth,i);
                        path.add(temp);
                        ringDfs(i,start,path,depth,visited,result);
                        visited.remove(i);
                        path.remove(temp);
                    }
                }
            }
        }
    }
}
