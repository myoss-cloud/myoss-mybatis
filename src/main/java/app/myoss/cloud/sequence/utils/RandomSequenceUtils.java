/*
 * Copyright 2018-2018 https://github.com/myoss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package app.myoss.cloud.sequence.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import app.myoss.cloud.sequence.exception.SequenceException;

/**
 * 随机序列工具类
 *
 * @author Jerry.Chen
 * @since 2018年12月16日 下午2:56:51
 */
public class RandomSequenceUtils {
    /**
     * 产生包含 0~n-1 的n个数值的随机序列
     *
     * @param n 序列范围值
     * @return n个数值的随机序列
     */
    public static int[] randomIntSequence(int n) {
        if (n <= 0) {
            throw new SequenceException("产生随机序列范围值小于等于0");
        }
        int[] num = new int[n];
        for (int i = 0; i < n; i++) {
            num[i] = i;
        }
        if (n == 1) {
            return num;
        }
        Random random = new Random();
        if (n == 2 && random.nextInt(2) == 1) {
            // 50%的概率换一下
            int temp = num[0];
            num[0] = num[1];
            num[1] = temp;
        }
        return randomIntSequence(num);
    }

    /**
     * 乱序一个数组
     *
     * @param sourceQueue 原始队列
     * @return 数组
     */
    public static int[] randomIntSequence(int[] sourceQueue) {
        int size = sourceQueue.length;
        Map<Integer, Integer> map = new HashMap<>(size);
        Random random = new Random();
        for (int i : sourceQueue) {
            int randomNum = random.nextInt(size * 100);
            map.put(i, randomNum);
        }
        ArrayList<Entry<Integer, Integer>> resultQueue = sortByValueAsc(map);
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = resultQueue.get(i).getKey();
        }
        return result;
    }

    private static ArrayList<Map.Entry<Integer, Integer>> sortByValueAsc(Map<Integer, Integer> map) {
        ArrayList<Map.Entry<Integer, Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((arg0, arg1) -> {
            int result = 0;
            if (arg0.getValue() - arg1.getValue() > 0) {
                result = 1;
            }
            return result;
        });
        return list;
    }
}
