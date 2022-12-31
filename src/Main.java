import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    private static String getStartNode(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        if (line != null) {
            return String.valueOf(line.charAt(0));
        }
        return null;
    }

    private static Map<String, List<String>> getProd(String filename) throws IOException {
        Map<String, List<String>> prods = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        while (line != null) {
            String[] data = line.split("->");
            if(prods.containsKey(data[0])){
                prods.get(data[0]).add(data[1]);
            }
            else {
                List<String> list = new ArrayList<>();
                list.add(data[1]);
                prods.put(data[0], list);
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return prods;
    }

    private static HashMap<String, Set<String>> getFirst(HashMap<String, List<String>> prods){
        HashMap<String, Set<String>> first = new HashMap<>();

        for (String key: prods.keySet()) {
            first.put(key, new HashSet<>());
        }

        boolean semaphore = true;
        while (semaphore){
            semaphore = false;
            for (String key: prods.keySet()) {
                List<String> rightParts = prods.get(key);
                for (String right: rightParts) {
                    if(right.substring(0, 1).matches("[^A-Z]")) {
                        if(!first.get(key).contains(right.substring(0,1))) {
                            first.get(key).add(right.substring(0, 1));
                            semaphore = true;
                        }
                    }
                    else{
                        for(int i=0; i <= right.length()-1; i++) {
                            String neterminal = right.substring(i, i+1);
                            if(neterminal.matches("[^A-Z]")){
                                if (!first.get(key).contains(neterminal)) {
                                    first.get(key).add(neterminal);
                                    semaphore = true;
                                }
                                break;
                            }
                            boolean semaphore1 = false;
                            for (String a : first.get(neterminal)) {
                                if (a.equals("&")) {
                                    semaphore1 =true;
                                }
                                else {
                                    if (!first.get(key).contains(a)) {
                                        if (!first.get(key).contains(a)) {
                                            first.get(key).add(a);
                                            semaphore = true;
                                        }
                                    }
                                }
                            }
                            if(!semaphore1){
                                break;
                            }
                            if(i == right.length()-1){
                                if(!first.get(key).contains("&")) {
                                    first.get(key).add("&");
                                    semaphore = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return first;
    }

    private static List<String> getFirstForSequence(String seq, HashMap<String, Set<String>> first){
        List<String> firstValues = new ArrayList<>();
        boolean aux = false;
        for(int i=0; i <= seq.length()-1 && !aux; i++) {
            String character = seq.substring(i, i + 1);
            if(character.matches("[A-Z]")){
                Set<String> strings = first.get(character);
                firstValues.addAll(strings);
                if(!strings.contains("&")){
                    aux = true;
                }
            }
            else{
                firstValues.add(character);
                aux=true;
            }
        }
        firstValues = firstValues.stream().filter(x-> !x.equals("&")).collect(Collectors.toList());
        if(!aux){
            firstValues.add("&");
        }
        return firstValues;
    }

    private static HashMap<String, Set<String>> getFollow(HashMap<String, List<String>> prods, String filename, HashMap<String, Set<String>> first) throws IOException {
        HashMap<String, Set<String>> follow = new HashMap<>();
        for (String key: getNet(filename)) {
            follow.put(key, new HashSet<>());
        }

        String firstState = getStartNode(filename);
        follow.get(firstState).add("$");

        boolean semaphore = true;
        while (semaphore){
            semaphore = false;
            for (String key: prods.keySet()) {
                List<String> rightParts = prods.get(key);
                for (String right: rightParts) {
                    for(int i=0; i <= right.length()-2; i++) {
                        String character = right.substring(i, i + 1);
                        if(character.matches("[A-Z]")){
                            String beta = right.substring(i+1);
                            List<String> firstForSequence = getFirstForSequence(beta, first);
                            if(firstForSequence.contains("&")){
                                firstForSequence.remove("&");
                                if(firstForSequence.size() != 0) {
                                    for (String terminal : firstForSequence) {
                                        if (!follow.get(character).contains(terminal)) {
                                            follow.get(character).add(terminal);
                                            semaphore = true;
                                        }
                                        for (String terminalFromFollow : follow.get(key)) {
                                            if (!follow.get(character).contains(terminalFromFollow)) {
                                                follow.get(character).add(terminalFromFollow);
                                                semaphore = true;
                                            }
                                        }
                                    }
                                }
                                else{
                                    for (String terminalFromFollow : follow.get(key)) {
                                        if (!follow.get(character).contains(terminalFromFollow)) {
                                            follow.get(character).add(terminalFromFollow);
                                            semaphore = true;
                                        }
                                    }
                                }
                            }
                            else{
                                for(String terminal : firstForSequence) {
                                    System.out.println(character);
                                    if (!follow.get(character).contains(terminal)) {
                                        follow.get(character).add(terminal);
                                        semaphore = true;
                                    }
                                }
                            }
                        }
                    }
                    if(right.substring(right.length()-1).matches("[A-Z]")) {
                        for (String terminalFromFollow : follow.get(key)) {
                            if (!follow.get(right.substring(right.length() - 1)).contains(terminalFromFollow)) {
                                follow.get(right.substring(right.length() - 1)).add(terminalFromFollow);
                                semaphore = true;
                            }
                        }
                    }
                }
            }
        }
        return follow;
    }

    private static Set<String> getCharacters(String filename) throws IOException {
        Set<String> characters = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        while (line != null){
            String[] data = line.split("->");
            if (data[0].length() > 0) {
                for (int i = 0; i < data[0].length(); i++){
                    if (!String.valueOf(data[0].charAt(i)).equals(" ")) {
                        characters.add(String.valueOf(data[0].charAt(i)));
                    }
                }
            }
            if (data[1].length() > 0){
                for (int i = 0; i < data[1].length(); i++){
                    if (!String.valueOf(data[1].charAt(i)).equals(" ")) {
                        characters.add(String.valueOf(data[1].charAt(i)));
                    }
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        return characters;
    }

    private static List<String> getNet(String filename) throws IOException {
        Set<String> characters = getCharacters(filename);
        List<String> net = new ArrayList<>();
        characters.forEach(character -> {
            if (character.matches("[A-Z]")){
                net.add(character);
            }
        });
        return net;
    }

    private static List<String> getTer(String filename) throws IOException {
        Set<String> characters = getCharacters(filename);
        List<String> ter = new ArrayList<>();
        characters.forEach(character -> {
            if (character.matches("[^A-Z&]")){
                ter.add(character);
            }
        });
        return ter;
    }

    public static void main(String[] args) throws IOException {
        String filename = "gramatica10.txt";

        HashMap<String, List<String>> prods = (HashMap<String, List<String>>) getProd(filename);
        HashMap<String, Set<String>> first = getFirst(prods);
        HashMap<String, Set<String>> follow = getFollow(prods, filename, first);
        HashMap<String, Integer> indexesForTerAndNeter = new HashMap<>();
        List<List<Pair<String, Integer>>> llTable = new ArrayList<>();
        List<String> all = makeOneList(getTer(filename), getNet(filename));
        System.out.println(follow);
        getIndexes(indexesForTerAndNeter, all);
        initializeTable(llTable, getTer(filename), all, indexesForTerAndNeter);
        List<Pair> productions = populateTable(llTable, prods, first, follow, indexesForTerAndNeter);
        Scanner scan= new Scanner(System.in);
//        System.out.print(">>>");
//        String sequenceToBeVerified = scan.nextLine();
        String fipFilename = "fip1.txt";
        String sequenceFromFip = sequenceFromFip(fipFilename);
        System.out.println("Sequence built from fip is: "+sequenceFromFip);
        String result = verifySequence(llTable, indexesForTerAndNeter, filename, sequenceFromFip);
        if(result != null){
            System.out.println(productions);
            System.out.println(result);
        }
        else{
            System.out.println(ANSI_RED + "Sequence is not accepted!" + ANSI_RESET);

        }


    }

    private static String sequenceFromFip(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        String line = bufferedReader.readLine();
        StringBuilder sequence= new StringBuilder();
        while (line!=null){
            String[] split = line.split("~");
            sequence.append(split[1]);
            line= bufferedReader.readLine();
        }
        return sequence.toString();
    }

    private static String verifySequence(List<List<Pair<String, Integer>>> llTable, HashMap<String, Integer> indexesForTerAndNeter, String filename, String sequenceToBeVerified) throws IOException {
        String input = sequenceToBeVerified+"$";
        String stack = getStartNode(filename)+"$";
        StringBuilder output = new StringBuilder();

        while(!input.equals("") && !stack.equals("")){
            String firstCharFromInput = input.substring(0, 1);
            String firstCharFromStack = stack.substring(0, 1);
            input = input.substring(1);
            stack = stack.substring(1);

            Pair<String, Integer> valueFromTable = llTable.get(indexesForTerAndNeter.get(firstCharFromStack)).get(indexesForTerAndNeter.get(firstCharFromInput));
            if(valueFromTable.getRight() == 0 && valueFromTable.getLeft().equals(" ")){
                System.out.println(input);
                return null;
            }
            if(valueFromTable.getLeft().equals("acc")){
                return output.toString();
            }
            if(!valueFromTable.getLeft().equals("pop")){
                output.append(valueFromTable.getRight());
                if(!valueFromTable.getLeft().equals("&")) {
                    stack = valueFromTable.getLeft() + stack;
                }
                input = firstCharFromInput + input;
            }
        }
        System.out.println(input);
        return null;
    }

    private static List<Pair> populateTable(List<List<Pair<String, Integer>>> llTable, HashMap<String, List<String>> prods, HashMap<String, Set<String>> first, HashMap<String, Set<String>> follow, HashMap<String, Integer> indexesForTerAndNeter) {
        List<Pair> prod = new ArrayList<>();
        int i = 1;
        for (String key : prods.keySet()){
            for(String str : prods.get(key)){
                Pair<String, Integer> newProd = new Pair<>(key+"->"+str, i);
                prod.add(newProd);
                Set<String> firstOrFollow;
                if(!str.equals("&")) {
                    firstOrFollow = new HashSet<>(getFirstForSequence(str, first));
                }
                else {
                    firstOrFollow = follow.get(key);
                }

                for (String firsts : firstOrFollow){
                    if (!firsts.equals("&")) {
                        Pair<String, Integer> pair = llTable.get(indexesForTerAndNeter.get(key)).get(indexesForTerAndNeter.get(firsts));
                        if (!pair.getLeft().equals(" ") && !pair.getRight().equals(0)) {
                            System.out.println(pair);
                            System.out.println(key +" " + str);
                            System.out.println(firstOrFollow);
                            return null;
                        } else {
                            llTable.get(indexesForTerAndNeter.get(key)).get(indexesForTerAndNeter.get(firsts)).setLeft(str);
                            llTable.get(indexesForTerAndNeter.get(key)).get(indexesForTerAndNeter.get(firsts)).setRight(i);
                        }

                    }
                }
                i++;
            }
        }
        return prod;
    }

    private static void getIndexes(HashMap<String, Integer> indexesForTerAndNeter, List<String> all) {
        for(int i = 0; i<all.size(); i++){
            indexesForTerAndNeter.put(all.get(i), i+1);
        }
    }

    private static List<String> makeOneList(List<String> ter, List<String> net) {
        ter.add("$");
        List<String> all = new ArrayList<>(ter);
        all.addAll(net);
        return all;
    }

    private static void printTable(List<List<Pair<String, Integer>>> llTable) {
        for(List<Pair<String, Integer>> row : llTable){
            System.out.println(row);
        }
    }

    private static void initializeTable(List<List<Pair<String, Integer>>> llTable, List<String> ter, List<String> all, HashMap<String, Integer> indexes) {
        ter.add("$");
        for(int i=0; i<=all.size(); i++){
            List<Pair<String, Integer>> row = new ArrayList<>();
            for(int j=0; j<=ter.size(); j++){
                row.add(new Pair<>(" ", 0));
            }
            llTable.add(row);
        }
        for(String terNeter : all){
            llTable.get(indexes.get(terNeter)).get(0).setLeft(terNeter);
        }
        for(String term : ter){
            llTable.get(0).get(indexes.get(term)).setLeft(term);
            if(!term.equals("$")) {
                llTable.get(indexes.get(term)).get(indexes.get(term)).setLeft("pop");
            }
            else {
                llTable.get(indexes.get(term)).get(indexes.get(term)).setLeft("acc");
            }
        }
    }
}
