package cn.funnymap.utils;

import java.lang.reflect.Field;

/**
 * @author jiao xn
 * @date 2024/1/10 14:58
 */
public class TagUtil {
    private TagUtil() {}

    public static String getConstantNameByVale(Class<?> interfaceClass, Object targetValue) {
        Field[] fields = interfaceClass.getDeclaredFields();

        for (Field field : fields) {
            try {
                Object value = field.get(null);

                if (value != null && value.equals(targetValue)) {
                    return field.getName();
                }
            } catch (IllegalAccessException exception) {
                System.out.println(exception.getMessage());
            }
        }

        return null;
    }
}
