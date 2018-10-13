package org.kaleta.accountant.common;

public class Utils {

    /**
     * Merges two integer arrays of the same length.
     */
    public static Integer[] mergeArrays(Integer[] array1, Integer[] array2){
        assert array1.length == array2.length;
        Integer[] result = new Integer[array1.length];
        for (int i=0;i<array1.length;i++) {
            result[i] = array1[i] + array2[i];
        }
        return result;
    }

    /**
     * Substracts two integer arrays of the same length.
     */
    public static Integer[] substractArrays(Integer[] array1, Integer[] array2){
        assert array1.length == array2.length;
        Integer[] result = new Integer[array1.length];
        for (int i=0;i<array1.length;i++) {
            result[i] = array1[i] - array2[i];
        }
        return result;
    }

    /**
     * Substracts two integer arrays of the same length.
     */
    public static String[] IntegerToStringArray(Integer[] array){
        String[] result = new String[array.length];
        for (int i=0;i<array.length;i++) {
            result[i] = String.valueOf(array[i]);
        }
        return result;
    }
}
