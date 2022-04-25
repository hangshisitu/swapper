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
 * 邻接表图
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
public class AdjacencyListGraph {

    /**
     * 边
     */
    @Data
    @ToString
    @AllArgsConstructor
    public static class Edge{
        private SPair SPair;
        private String tokenId;
    }

    private Map<String,List<Edge>> adjacencyList;

    public AdjacencyListGraph(List<SPair> sPairList)
    {
        adjacencyList = new HashMap<>(sPairList.size()*2);
        sPairList.stream().forEach(p -> {
            List<Edge> edges = adjacencyList.getOrDefault(p.getToken0().getId(),new LinkedList<>());
            edges.add(new Edge(p,p.getToken1().getId()));
            if(!adjacencyList.containsKey(p.getToken0()))
            {
                adjacencyList.put(p.getToken0().getId(),edges);
            }
            List<Edge> edges1 = adjacencyList.getOrDefault(p.getToken1().getId(),new LinkedList<>());
            edges1.add(new Edge(p,p.getToken0().getId()));
            if(!adjacencyList.containsKey(p.getToken1()))
            {
                adjacencyList.put(p.getToken1().getId(),edges1);
            }
        });
    }

    public Boolean hasRing()
    {
        List<Map.Entry<String,List<Edge>>> entries = adjacencyList.entrySet().stream().collect(Collectors.toList());
        for(int i=0;i<entries.size();++i)
        {
            List<String> path = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            if(hasRingDfs(entries.get(i).getKey(),path,"",visited)){
                return true;
            }
        }
        return false;
    }

    public List<List<String>> rings()
    {
        List<List<String>> result = new LinkedList<>();
        List<Map.Entry<String,List<Edge>>> entries = adjacencyList.entrySet().stream().collect(Collectors.toList());
        for(int i=0;i<entries.size();++i)
        {
            List<String> path = new ArrayList<>();
            Set<String> visited = new HashSet<>();
            String start = entries.get(i).getKey();
            path.add(start);
            visited.add(start);
            ringDfs(start,start,path,"",visited,result);
        }
        return result;
    }

    private boolean hasRingDfs(String start, List<String> path, String pre, Set<String> visited)
    {
        List<Edge> edges = adjacencyList.get(start);
        for(int i=0;i<edges.size();++i)
        {
            String nextId = edges.get(i).getTokenId();
            //不是刚出发的节点
            if(pre.equals("") || !nextId.equals(pre))
            {
                if(visited.contains(nextId))
                {
                    //找到一个环
                    return true;
                }else {
                    visited.add(nextId);
                    path.add(nextId);
                    if(hasRingDfs(nextId,path,start,visited))
                    {
                        return true;
                    }
                    visited.remove(nextId);
                    path.remove(nextId);
                }
            }
        }
        return false;
    }


    private void ringDfs(String currId,
                         String start,
                         List<String> path,
                         String pre,
                         Set<String> visited,
                         List<List<String>> result)
    {
        List<Edge> edges = adjacencyList.get(currId);
        for(int i=0;i<edges.size();++i)
        {
            String nextId = edges.get(i).getTokenId();
            //不是刚出发的节点
            if(pre.equals("") || !nextId.equals(pre))
            {
                if(visited.contains(nextId))
                {
                    if(nextId.equals(start))
                    {
                        log.info("one ring: {}",path);
                        result.add(new ArrayList<>(path));
                        //跳过这条边
                    }
                    //跳过这条边
                }else {
                    visited.add(nextId);
                    path.add(nextId);
                    ringDfs(nextId,start,path,currId,visited,result);
                    visited.remove(nextId);
                    path.remove(nextId);
                }
            }
        }
    }
}
