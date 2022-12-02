package org.kaleta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Utils {

    public static String inputStreamToString(InputStream is) throws IOException {
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
    public static Integer[] mergeIntegerArrays(Integer[]... arrays){
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
    public static Integer[] subtractIntegerArrays(Integer[] base, Integer[] subtraction){
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
}
