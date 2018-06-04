package go.planner.plannergo;

import java.util.ArrayList;
import java.util.HashMap;

//TODO: test
public class Bag<E> {
     private HashMap<E, Integer> map;

     Bag(){
         map = new HashMap<>();
     }

     Bag(int size){
         map = new HashMap<>(size);
     }

    void add(E e) {
        if (map.containsKey(e)){
            map.put(e, map.get(e) + 1);
        } else {
            map.put(e, 1);
        }
    }

    boolean remove(E e) {
        if (map.containsKey(e)){
            map.put(e, map.get(e) - 1);
            if (map.get(e) <= 0)
                map.remove(e);
            return true;
        } else return false;
    }

    boolean contains(E e) {
        return map.containsKey(e);
    }

    int getQuantity(E e) {
        return map.containsKey(e) ? map.get(e) : 0;
    }

    void clear() {
        map.clear();
    }

    void empty() {
        clear();
    }

    int size(){
        return map.size();
    }

    E get(int position) {
         return getSortedArray().get(position);
    }

    ArrayList<E> getSortedArray(){
         ArrayList<E> arrayList = new ArrayList<>();
         for (E e: map.keySet()){
             int i = 0;
             while (i < arrayList.size() && map.get(e) < map.get(arrayList.get(i))){
                 i++;
             }
             arrayList.add(i, e);
         }
         return arrayList;
    }

    @Override
    public String toString() {
        return getSortedArray().toString();
    }
}
