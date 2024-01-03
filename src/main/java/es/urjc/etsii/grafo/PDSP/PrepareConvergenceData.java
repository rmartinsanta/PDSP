//package es.urjc.etsii.grafo.PDSP;
//
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.urjc.etsii.grafo.solution.metrics.Metrics;
//import es.urjc.etsii.grafo.solution.metrics.TimeValue;
//import es.urjc.etsii.grafo.util.TimeUtil;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.TreeSet;
//import java.util.concurrent.TimeUnit;
//
//public class PrepareConvergenceData {
//    public static void main(String[] args) {
//        if(args.length != 2){
//            System.err.println("Usage: java -jar PrepareConvergenceData.jar <path_to_json> <nIterations>");
//            System.exit(1);
//        }
//
//        String path = args[0];
//        int nIterations = Integer.parseInt(args[1]);
//        String[][] parsedData = new String[nIterations][];
//        for (int i = 0; i < nIterations; i++) {
//            try (var br = Files.newBufferedReader(Path.of(path.replace("!!", String.valueOf(i))))) {
//                parsedData[i] = parseFile(br);
//            } catch (Exception e) {
//                System.err.println("Error reading file: " + path);
//                e.printStackTrace();
//                System.exit(1);
//            }
//        }
//
//        var out = new StringBuilder();
//        int max = Integer.MIN_VALUE;
//        for (String[] parsedDatum : parsedData) {
//            if(parsedDatum.length > max){
//                max = parsedDatum.length;
//            }
//        }
//        for (int i = 0; i < max; i++) {
//            for (int j = 0; j < nIterations; j++) {
//                if(i < parsedData[j].length){
//                    out.append(parsedData[j][i]);
//                } else {
//                    out.append("\t");
//                }
//                out.append("\t");
//            }
//            out.append("\n");
//        }
//        System.out.println(out);
//    }
//
//    private record PDSPResult(Map<String, TreeSet<TimeValue>> metrics){}
//
//    private static String[] parseFile(BufferedReader br) throws IOException {
//        var jsonMapper = getJSONMapper();
//        var wur = jsonMapper.readValue(br, PDSPResult.class);
//        return printData(wur.metrics().get(Metrics.BEST_OBJECTIVE_FUNCTION));
//    }
//
//    private static ObjectMapper getJSONMapper(){
//        var om = new ObjectMapper();
//        // Configure mapper
//        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        return om;
//    }
//
//    public static String[] printData(TreeSet<TimeValue> data){
//        long lastTime = 0;
//        double minValue = Integer.MAX_VALUE;
//        List<String> result = new ArrayList<>();
//        for(var tv : data){
//            if(tv.timeElapsed() < lastTime){
//                System.err.printf("Data is not sorted! %s < %s%n", tv.timeElapsed(), lastTime);
//                System.exit(1);
//            }
//            lastTime = tv.timeElapsed();
//            if(tv.value() < minValue){
//                minValue = tv.value();
//                long millis = TimeUtil.convert(tv.timeElapsed(), TimeUnit.NANOSECONDS, TimeUnit.MILLISECONDS);
//                result.add("%s\t%s".formatted(millis, tv.value()));
//            }
//        }
//        return result.toArray(new String[0]);
//    }
//}
