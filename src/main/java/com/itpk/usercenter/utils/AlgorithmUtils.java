package com.itpk.usercenter.utils;

import com.itpk.usercenter.Exception.BusinessException;
import com.itpk.usercenter.common.errorCode;

import java.util.List;

public class AlgorithmUtils {

    public static long minDistance(List<String> word1, List<String> word2) {
        if (word1 == null || word2 == null) {
            throw new BusinessException(errorCode.NULL_ERROR,"参数不能为空");
        }
        long[][] dp = new long[word1.size() + 1][word2.size() + 1];
        //初始化DP数组
        for (int i = 0; i <= word1.size(); i++) {
            dp[i][0] = i;
        }
        for (int i = 0; i <= word2.size(); i++) {
            dp[0][i] = i;
        }
        int cost;
        for (int i = 1; i <= word1.size(); i++) {
            for (int j = 1; j <= word2.size(); j++) {
                if (word1.get(i - 1) .equals(word2.get(j - 1)) ) {
                    cost = 0;
                } else {
                    cost = 1;
                }
                dp[i][j] = min(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost);
            }
        }
        return dp[word1.size()][word2.size()];
    }

    private static long min(long x, long y, long z) {
        return Math.min(x, Math.min(y, z));
    }

}
