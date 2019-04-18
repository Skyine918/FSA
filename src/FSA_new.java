import sun.awt.image.ImageWatched;

import java.io.*;
import java.util.Hashtable;
import java.util.LinkedList;

class FSA_new {

    private LinkedList<State> states;
    private LinkedList<String> alphabet;
    private State initial;
    private LinkedList<State> finstates;
    private LinkedList<String>[][] transitions;
    Boolean isValid = true;

    FSA_new(String fileIn, String fileOut) {


        BufferedReader br = null;
        BufferedWriter bw = null;
        FileReader fr = null;
        FileWriter fw = null;

        try {

            fr = new FileReader(fileIn);
            fw = new FileWriter(fileOut);
            br = new BufferedReader(fr);
            bw = new BufferedWriter(fw);

            String line;


            line = br.readLine();
            if (!line.startsWith("states={") || !line.endsWith("}") || line.contains("_")) {
                bw.write("Error:\nE5: Input file is malformed");
                isValid = false;
                return;
            }
            line = line.replace("}", "").replace("states={", "");
            String[] array = line.split(",");
            states = new LinkedList<>();
            int i = 0;

            for (String state : array) {
                add_state(new State(state, i));
                i++;
            }


            line = br.readLine();
            if (!line.startsWith("alpha={") || !line.endsWith("}")) {
                bw.write("Error:\nE5: Input file is malformed");
                isValid = false;
                return;
            }
            alphabet = new LinkedList<>();
            line = line.replace("}", "").replace("alpha={", "");
            array = line.split(",");

            for (String symbol : array) {
                this.add_symbol(symbol);
            }


            line = br.readLine();
            if (!line.startsWith("init.st={") || !line.endsWith("}")) {
                bw.write("Error:\nE5: Input file is malformed");
                isValid = false;
                return;
            }
            initial = null;
            line = line.replace("}", "").replace("init.st={", "");
            if (line.equals("")) {
                bw.write("Error:\nE4: Initial state is not defined");
                isValid = false;
                return;
            } else if (getState(line) == null) {
                bw.write("Error:\nE1: A state \'" + line + "\' is not in set of states");
                isValid = false;
                return;
            } else initial = getState(line);


            line = br.readLine();

            if (!line.startsWith("fin.st={") || !line.endsWith("}") || line.contains("_")) {
                bw.write("Error:\nE5: Input file is malformed");
                isValid = false;
                return;
            }

            finstates = new LinkedList<>();
            line = line.replace("}", "").replace("fin.st={", "");
            array = line.split(",");
            if (!line.equals("")) {
                for (String state : array) {
                    if (getState(state) == null) {
                        bw.write("Error:\nE1: A state \'" + state + "\' is not in set of states");
                        isValid = false;
                        return;
                    } else {
                        add_final_state(getState(state));
                    }
                }
            }

            line = br.readLine();

            if (!line.startsWith("trans={") || !line.endsWith("}")) {
                bw.write("Error:\nE5: Input file is malformed");
                isValid = false;
                return;
            }

            line = line.replace("}", "").replace("trans={", "");
            array = line.split(",");
            int size = states.size();
            transitions = new LinkedList[size][size];

            for (int k = 0; k < size; k++) {
                for (int j = 0; j < size; j++) {
                    transitions[k][j] = new LinkedList<>();
                }
            }

            for (String transition : array) {
                String[] arr = transition.split(">");

                if (this.states.contains(getState(arr[0])) && this.states.contains(getState(arr[2])) && getState(arr[0]) != null && getState(arr[2]) != null && alphabet.contains(arr[1]))
                    add_transition(getState(arr[0]), getState(arr[2]), arr[1]);
                if (!this.states.contains(getState(arr[0]))) {
                    bw.write("Error:\nE1: A state \'" + arr[0] + "\' is not in set of states");
                    isValid = false;
                    return;
                }
                if (!this.states.contains(getState(arr[2]))) {
                    bw.write("Error:\nE1: A state \'" + arr[2] + "\' is not in set of states");
                    isValid = false;
                    return;
                }
                if (!this.alphabet.contains(arr[1])) {
                    bw.write("Error:\nE3: A transition \'" + arr[1] + "\' is not represented in the alphabet");
                    isValid = false;
                    return;
                }
            }

            if (DFS_undirected(initial) != states.size()) {
                bw.write("Error:\nE2: Some states are disjoint");
                isValid = false;
                return;
            }

//            if (isComplete()) bw.write("FSA is complete");
//            else bw.write("FSA is incomplete");

            LinkedList<String> warns = new LinkedList<>();
//            if (finstates.isEmpty()) warns.add("\nW1: Accepting state is not defined");
//            if (DFS_directed(initial) != states.size()) warns.add("\nW2: Some states are not reachable from initial state");
            if (!this.isDeterministic()) warns.add("\nE6: FSA is nondeterministic");

            if (!warns.isEmpty()) {
                bw.write("Error:");
                for (String str: warns) {
                    bw.write(str);
                    isValid = false;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null) br.close();
                if (fr != null) fr.close();
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void add_state(State state) {
        if (!this.states.contains(state)) states.add(state);
        else System.out.println("State [" + state.name + "] already defined");
    }

    private void add_symbol(String str) {
        if (!this.alphabet.contains(str)) alphabet.add(str);
        else System.out.println("Symbol [" + str + "] already defined");
    }

    private void add_final_state(State state) {
        if (state != null) {
            if (!this.states.contains(state))
                System.out.println("Can't add final state [" + state.name + "], no such state!");
            if (this.finstates.contains(state))
                System.out.println("Final state set already contains state [" + state.name + "]");
            if (!this.finstates.contains(state) && this.states.contains(state)) finstates.add(state);
        }
    }

    private void add_transition(State state1, State state2, String symbol) {

        if (state1 != null && state2 != null && alphabet.contains(symbol)) {
            if (!transitions[state1.index][state2.index].contains(symbol)) {
                transitions[state1.index][state2.index].add(symbol);
            }
        }
    }

    private LinkedList<State> adjecencyList_directed(State vertex) {
        LinkedList<State> list = new LinkedList<>();
        for (int i = 0; i < transitions.length; i++) {
            if (!transitions[vertex.index][i].isEmpty() && vertex.index != i) {
                list.add(getState(i));
            }
        }
        return list;
    }

    private LinkedList<State> adjecencyList_undirected(State vertex) {
        LinkedList<State> list = new LinkedList<>();
        for (int i = 0; i < transitions.length; i++) {
            if (!transitions[vertex.index][i].isEmpty() && vertex.index != i) {
                list.add(getState(i));
            }
        }
        for (int i = 0; i < transitions.length; i++) {
            if (!transitions[i][vertex.index].isEmpty() && vertex.index != i && !list.contains(getState(i))) {
                list.add(getState(i));
            }
        }
        return list;
    }

    private State getState(String name) {
        for (State state : states) {
            if (state.name.equals(name)) return state;
        }
        return null;
    }

    private State getState(int index) {
        for (State state : states) {
            if (state.index == index) return state;
        }
        return null;
    }

    private int DFS_directed(State source) {

        LinkedList<State> isVisited = new LinkedList<>();

        DFS_internalDirected(source, isVisited);

        return isVisited.size();
    }

    private int DFS_undirected(State source) {

        LinkedList<State> isVisited = new LinkedList<>();

        DFS_internalUndirected(source, isVisited);

        return isVisited.size();
    }

    private void DFS_internalUndirected(State vertex, LinkedList<State> isVisited) {
        isVisited.add(vertex);

        for (State adj_state : adjecencyList_undirected(vertex)) {
            if (!isVisited.contains(adj_state)) {
                DFS_internalUndirected(adj_state, isVisited);
            }
        }
    }

    private void DFS_internalDirected(State vertex, LinkedList<State> isVisited) {

        isVisited.add(vertex);
        for (State adj_state : adjecencyList_directed(vertex)) {
            if (!isVisited.contains(adj_state)) {
                DFS_internalDirected(adj_state, isVisited);
            }
        }
    }

    private boolean isDeterministic() {
        Hashtable<String, Boolean> temp = new Hashtable<>();

        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                for (String a: transitions[i][j]) {
                    if (temp.containsKey(a)) {
                        return false;
                    } else {
                        temp.put(a, true);
                    }
                }
            }
            temp.clear();
        }
        return true;
    }

    private boolean isComplete() {
        Hashtable<String, Boolean> temp = new Hashtable<>();

        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                for (String a: transitions[i][j]) {
                    if (!temp.containsKey(a)) {
                        temp.put(a, true);
                    }
                }
            }
            if (temp.size() < alphabet.size()) return false;
            temp.clear();
        }
        return true;
    }

    public String kleeneAlgorithm() {
        if (!finstates.isEmpty()) {
            String res = "";
            for (State s: finstates) res += R(states.size() - 1, initial.index, s.index) + "|";
            return res;
        } else return "{}";
    }

    public String R(int k, int i, int j) {

        if (k == -1) {
            if (i == j) {
                if (!transitions[i][j].isEmpty()) {
                    String resstring = "";
                    for (String str: transitions[i][j]) resstring += str+"|";
                    return resstring + "eps";
                } else {
                    return "eps";
                }

            } else {
                if (!transitions[i][j].isEmpty()) {
                    String resstring = "";
                    for (String str : transitions[i][j]) resstring += str + "|";
                    return resstring.substring(0, resstring.length() - 1) + "";
                } else {
                    return "{}";
                }
            }
        } else {
            return "("+R(k-1, i, k)+")("+R(k-1, k, k)+")*("+R(k-1, k, j)+")|("+R(k-1, i, j)+")";
        }


    }

}

class State {
    String name;
    int index;

    State(String name, int index) {
        this.name = name;
        this.index = index;
    }
}