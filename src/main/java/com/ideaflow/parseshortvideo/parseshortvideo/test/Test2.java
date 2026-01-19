package com.ideaflow.parseshortvideo.parseshortvideo.test;

public class Test2 {
    static void main() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[ 1, \"7:[\\\"$\\\",\\\"$L9\\\",null,{\\\"awemeId\\\":\\\"7335767155465588006\\\",\\\"aweme\\\":{\\\"statusCode\\\":0,\\\"detail\\\":{\\\"rawAdData\\\":\\\"$undefined\\\",\\\"packAdDate\\\":null,\\\"isAds\\\":false,\\\"isSlides\\\":true,\\\"logPb\\\":\\\"$undefined\\\",\\\"offset\\\":\\\"$undefined\\\",\\\"name\\\":\\\"$undefined\\\",\\\"");



        if (stringBuilder.toString().contains("\\\"awemeId\\\":\\\"7335767155465588006\\\"")) {
            System.out.println("213");
        }

    }
}
