package com.destroystokyo.paper;

import java.lang.reflect.Method;

public class test {

	public static void main(String[] args) throws Exception {
		
		String[] strs = {"org.cardboardpowered.mixin.screen.MixinEnchantmentScreenHandler",
		//"org.cardboardpowered.mixin.screen.MixinGenericContainerScreenHandler",
		//"org.cardboardpowered.mixin.screen.MixinLoomScreenHandler",
		//"org.cardboardpowered.mixin.screen.MixinGrindstoneScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinHopperScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinMerchantScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinAbstractFurnaceScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinGeneric3x3ContainerScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinLecternScreenHandler",
		// "org.cardboardpowered.mixin.screen.MixinHorseScreenHandler"};
		};
		
		// Class<?> cl = MixinBeaconScreenHandler.class;
		
		for (String st : strs) {
			Class<?> cl = Class.forName(st);
		try {
			Method[] mm = cl.getMethods();
			for (Method m : mm) {
				if (m.getName().equalsIgnoreCase("setPlayerInv")) {
					// System.out.println(m);
					String s= m.toString();
					s = s.split("setPlayerInv")[1];
					s = s.replace("int", "I").replace("net.minecraft", "Lnet/minecraft").replaceAll("[.]", "/");
					s = s.split("org/spongepowered")[0].replaceAll(",L", ";L").replace(",", ";").replace("I;", "I") + ")V";
					System.out.println(cl.getName() + " = " + s);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
	}

}
