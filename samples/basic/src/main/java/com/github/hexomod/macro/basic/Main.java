package com.github.hexomod.macro.basic;

public class Main {

    public static void main(String [] args) {
        //#if VAR_BOOL==true
        System.out.println(Greeting.getGreeting());
        //#endif
    }
}
