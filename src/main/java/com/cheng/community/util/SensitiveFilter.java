package com.cheng.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private static final String REPLACEMENT = "***";

    // 初始化
    private TrieNode root = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ) {
            String keyWord;
            while((keyWord = reader.readLine()) != null){
                // 添加到前缀树
                this.addKeyWord(keyWord);
            }
        } catch (IOException e) {
            logger.error("加载铭感词文件失败:", e.getMessage());

        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        if (StringUtils.isBlank(text)){
            return null;
        }
        TrieNode tempNode = root;
        int begin = 0;
        int position = 0;
        StringBuilder sb = new StringBuilder();
        while (position < text.length()) {
            char c = text.charAt(position);
            if (isSymbol(c)) {
                if(tempNode == root){
                    sb.append(c);
                    ++begin;
                }
                // 无论符号在开头或中间，指针三都向后走
                ++position;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                ++begin;
                position = begin;
                tempNode = root;
            } else if (tempNode.isKeyWordEnd) {
                // 发现敏感词，将begin到position的字符替换掉
                sb.append(REPLACEMENT);
                // 进入系一个位置
                ++position;
                begin = position;
                tempNode = root;
            } else {
                // 继续检查下一个字符
                position++;
            }
        }
        // 将最后一批字符记录
        sb.append(text.substring(begin));
        return sb.toString();
    }

    private boolean isSymbol(Character c) {
        // 0x2e80 - 0x9fff是东亚文字范围
        return !CharUtils.isAsciiNumeric(c) && (c < 0x2e80 || c >0x9fff);
    }

    private void addKeyWord(String keyword) {
        TrieNode tempNode = root;
        for (int i =0 ; i<keyword.length(); ++i){
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null){
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            tempNode = subNode;
            if (i == keyword.length() - 1) {
                tempNode.setKeyWordEnd(true);
            }
        }
    }

    // 前缀树结构
    private class TrieNode{

        // 关键词结束的标志
        private boolean isKeyWordEnd = false;

        // 当前节点的子节点, key是下级字符, value是节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();


        public boolean isKeyWordEnd() {
            return isKeyWordEnd;
        }

        public void setKeyWordEnd(boolean keyWordEnd) {
            isKeyWordEnd = keyWordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }
    }
}
