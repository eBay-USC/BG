package edu.usc.bg;

import edu.usc.bg.JanusGraphBGCoord;

import java.util.HashMap;
import java.util.Map;

/** 演示：用抛物线模拟吞吐量，用阈值做 SLA */
public class OptimizerDemo {

    /* --------- 抛物线模拟函数 --------- */
    private static double simulatePerformance(int threads) {
        return -Math.pow(threads - 50, 2) + 2500;   // 峰值 2500，峰位 50
    }

    /* --------- 继承前面写的优化器 --------- */
    private static class DemoOptimizer extends JanusGraphBGCoord {

        private double lastTp;

        @Override
        public double measureThroughput(int threads, int run) {
            lastTp = simulatePerformance(threads);
            return lastTp;
        }

        @Override
        public boolean checkSLA(int run) {
            return lastTp >= 200;                   // SLA：tp ≥ 1200
        }
    }

    /* --------- 主函数 --------- */
    public static void main(String[] args) throws Exception {
        DemoOptimizer opt = new DemoOptimizer();
        int best = opt.findMaxThroughput(1);        // 起始线程数随便给个 4

        System.out.println("=====================================");
        System.out.println("Best thread count (SLA pass): " + best);
        System.out.println("Throughput at best           : " +
                simulatePerformance(best));
        System.out.println("=====================================");
    }
}

