package org.kaleta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Utils
{
    public static String inputStreamToString(InputStream is) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();
        return sb.toString();
    }

    /**
     * Merges integer arrays of the same length.
     */
    public static Integer[] mergeIntegerArrays(Integer[]... arrays)
    {
        // check same length
        for(Integer[] array : arrays){
            if (!(array.length == arrays[0].length)){
                throw new IllegalArgumentException("arrays not same length: a0=" + arrays[0].length + " != an="+array.length);
            }
        }
        Integer[] result = new Integer[arrays[0].length];
        Arrays.fill(result, 0);

        for(Integer[] array : arrays){
            for (int i=0;i<result.length;i++) {
                result[i] += array[i];
            }
        }
        return result;
    }

    /**
     * subtract two integer arrays of the same length.
     */
    public static Integer[] subtractIntegerArrays(Integer[] base, Integer[] subtraction)
    {
        // check same length
        if (!(base.length == subtraction.length)){
            throw new IllegalArgumentException("arrays not same length: a0=" + base.length + " != an="+subtraction.length);
        }

        Integer[] result = new Integer[base.length];

        for (int i=0;i<base.length;i++) {
            result[i] = base[i] - subtraction[i];
        }
        return result;
    }

    /**
     * add two integer arrays of the same length.
     */
    public static Integer[] addIntegerArrays(Integer[] base, Integer[] addition)
    {
        // check same length
        if (!(base.length == addition.length)){
            throw new IllegalArgumentException("arrays not same length: a0=" + base.length + " != an="+addition.length);
        }

        Integer[] result = new Integer[base.length];

        for (int i=0;i<base.length;i++) {
            result[i] = base[i] + addition[i];
        }
        return result;
    }

    public static Integer[] toCumulativeArray(Integer[] linearArray)
    {
        Integer[] cumulativeArray = new Integer[linearArray.length];

        Integer sum = 0;
        for (int i=0;i<linearArray.length;i++){
            sum += linearArray[i];
            cumulativeArray[i] = sum;
        }
        return cumulativeArray;
    }

    public static <T> T[] concatArrays(T[] array1, T[] array2)
    {
        List<T> resultList = new ArrayList<>(array1.length + array2.length);
        Collections.addAll(resultList, array1);
        Collections.addAll(resultList, array2);

        @SuppressWarnings("unchecked")
        //the type cast is safe as the array1 has the type T[]
        T[] resultArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), 0);
        return resultList.toArray(resultArray);
    }

    public static Integer sumArray(Integer[] array){
        Integer sum = 0;
        for (int i=0; i<array.length; i++){
            sum += array[i];
        }
        return sum;
    }
}
