package com.minicodex.memory;



import java.util.List;



public interface MemoryStore {



    void save(
            Memory memory
    );



    List<Memory> query(
            String keyword
    );



    void clear();



}