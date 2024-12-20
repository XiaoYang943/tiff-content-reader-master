package cn.funnymap.compression.lzw;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * LZW 压缩算法示例
 *
 * @author jiao xn
 * @date 2023/12/28 15:03
 */
public class LZWExample {
    private static final int DICT_SIZE = 256;
    // CLEAR OR ReInitialize Dict
    private LZWExample() {}

    public static List<Integer> encoder(String charStream) {
        // 初始化字典
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < DICT_SIZE; i++) {
            dictionary.put(String.valueOf((char) i), i);
        }

        // 压缩数据
        List<Integer> result = new ArrayList<>();
        String foundCharacters = "";
        for (char character : charStream.toCharArray()) {
            String charactersToAdd = foundCharacters + character;

            if (dictionary.containsKey(charactersToAdd)) {
                foundCharacters = charactersToAdd;
            } else {
                result.add(dictionary.get(foundCharacters));
                dictionary.put(charactersToAdd, dictionary.size());
                foundCharacters = String.valueOf(character);
            }
        }
        if (!foundCharacters.isEmpty()) {
            result.add(dictionary.get(foundCharacters));
        }

        return result;
    }

    public static String decode(List<Integer> codeStream) {
        // 初始化字典
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < DICT_SIZE; i++) {
            dictionary.put(i, String.valueOf((char) i));
        }

        // 解压数据
        String characters = String.valueOf((char) codeStream.remove(0).intValue());
        StringBuilder result = new StringBuilder(characters);
        for (int code : codeStream) {
            String entry = dictionary.containsKey(code) ? dictionary.get(code) : characters + characters.charAt(0);
            result.append(entry);
            dictionary.put(dictionary.size(), characters + entry.charAt(0));
            characters = entry;
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String charStream = "TOBEORNOTTOBEORTOBEORNOT";
        System.out.println("输入数据：" + charStream);

        List<Integer> codeStream = LZWExample.encoder(charStream);
        System.out.println("编码结果：" + codeStream);

        System.out.println("解码结果：" + LZWExample.decode(codeStream));
    }
}
