package cn.funnymap.utils;

/**
 * @author jiao xn
 * @date 2023/12/23 11:11
 */
public class FMUtil {
    private FMUtil() {}

    public static boolean isEmpty(Object s) {
        return s == null || (s instanceof String && ((String) s).isEmpty());
    }

    public static String reverseStringInPairs(String originalStr) {
        char[] charArray = originalStr.toCharArray();

        if (charArray.length % 2 != 0) {
            return originalStr;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = charArray.length - 2; i >= 0; i -= 2) {
            stringBuilder.append(charArray[i]);
            stringBuilder.append(charArray[i + 1]);
        }

        return stringBuilder.toString();
    }
}
