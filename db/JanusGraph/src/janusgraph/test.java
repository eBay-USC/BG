/**
 * @description
 * @date 2025/4/3 12:49
 * @version 1.0
 */

package janusgraph;

public class test {
    private static double measureThroughput(int threads, int count) {
        System.out.println("count=" + count + " " + "threads=" + threads + " performance=" + -Math.pow(threads - 50, 2) + 2500);
        return -Math.pow(threads - 50, 2) + 2500;
    }

    private static boolean checkSLA(int threads, int count) {
        return threads <= 2000;
    }

    public static void main(String[] args) throws Exception {
        int left = 1;
        int right = 100;
        int count = 0;
        int bestThreadCount = ternarySearchMaxThroughput(left, right, count);
        System.out.println("best thread count " + bestThreadCount);
    }
    public static int ternarySearchMaxThroughput(int left, int right, int count) throws Exception {
        if (left >= right) {
            throw new IllegalArgumentException("left must be smaller than right");
        }

        // 记录最佳候选：既满足 SLA 且吞吐率最高的线程数
        int bestThreadCount = -1;
        double bestThroughput = Double.NEGATIVE_INFINITY;

        while (right - left > 2) {
            int m1 = left + (right - left) / 3;
            int m2 = right - (right - left) / 3;

            // 测试 m1 点
            double throughput1 = measureThroughput(m1, count);
            boolean valid1 = checkSLA(m1, count);  // 返回 true 表示 m1 满足 SLA
            count++;

            // 测试 m2 点
            double throughput2 = measureThroughput(m2, count);
            boolean valid2 = checkSLA(m2, count);  // 返回 true 表示 m2 满足 SLA
            count++;

            // 更新最佳候选（仅当满足 SLA 时）
            if (valid1 && throughput1 > bestThroughput) {
                bestThroughput = throughput1;
                bestThreadCount = m1;
            }
            if (valid2 && throughput2 > bestThroughput) {
                bestThroughput = throughput2;
                bestThreadCount = m2;
            }

            // 根据 SLA 状态以及吞吐率调整搜索区间
            if (valid1 && !valid2) {
                // m1 满足 SLA，而 m2 不满足 SLA，则向低线程数区间收敛
                right = m2 - 1;
            } else if (!valid1 && valid2) {
                // m2 满足 SLA，而 m1 不满足 SLA，则向高线程数区间收敛
                left = m1 + 1;
            } else if (valid1 && valid2) {
                // 两者都满足 SLA，比较吞吐率决定收敛方向
                if (throughput1 < throughput2) {
                    left = m1 + 1;
                } else {
                    right = m2 - 1;
                }
            } else {
                // 两个点均不满足 SLA，由于更高线程数会违反 SLA，所以向低区间收敛
                right = m1 - 1;
            }
        }

        // 最后对剩余区间进行线性扫描
        for (int t = left; t <= right; t++) {
            double currentThroughput = measureThroughput(t, count);
            boolean valid = checkSLA(t, count);
            count++;
            if (valid && currentThroughput > bestThroughput) {
                bestThroughput = currentThroughput;
                bestThreadCount = t;
            }
        }

        if (bestThreadCount == -1) {
            throw new Exception("No thread count satisfies SLA");
        }

        return bestThreadCount;
    }

}
