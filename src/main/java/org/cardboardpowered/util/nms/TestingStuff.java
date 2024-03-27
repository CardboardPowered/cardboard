package org.cardboardpowered.util.nms;

import java.lang.reflect.Method;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LunarWorldView;
import net.minecraft.world.WorldView;

public class TestingStuff {

    public static void find_inheritance(Class<?> clazz, String obf_name, String desc) {
    	//System.out.println(clazz.getName());
    	
    	// for (Method m : clazz.getMethods()){
    	// }
    	
    	if (clazz == WorldView.class) {

    	}
    	
    	for (Class<?> ih : clazz.getInterfaces()) {
    		
    		
    		
    		
    		if (null != ih.getSuperclass()) {
    			//System.out.println("SUPER CL: " + ih.getSuperclass());
    		}
    		
        	for (Class<?> ii : clazz.getInterfaces()) {
        		print(ii);
        		find_inheritance(ii, "", "");
        	}
    	}
    }

    public static void main(String[] args) {
    	//find_inheritance(ServerWorld.class, "o", "()Lnet/minecraft/server/MinecraftServer;");
    
    	Class<?> clazz = LunarWorldView.class;
    	
    	find_inheritance(clazz, "", "");
    	
    }
    
    public static void print(Object o) {
    	System.out.println(o);
    }
	
}
