package net.bdew.wurm.compass;

import com.wurmonline.client.game.World;
import com.wurmonline.client.renderer.PickData;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CompassMod implements WurmMod, Initable, PreInitable {
    private static Logger logger = Logger.getLogger("CompassMod");

    @Override
    public void init() {
        logger.fine("Initializing");

        try {
            HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.CompassComponent", "gameTick", "()V", new InvocationHandlerFactory() {
                @Override
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            method.invoke(proxy, args);
                            Class<?> cls = proxy.getClass();
                            ReflectionUtil.callPrivateMethod(proxy, ReflectionUtil.getMethod(cls, "setQl"), 99);
                            ReflectionUtil.setPrivateField(proxy, ReflectionUtil.getField(cls, "isMoving"), false);
                            ReflectionUtil.setPrivateField(proxy, ReflectionUtil.getField(cls, "fadeAlpha"), 1f);
                            return null;
                        }
                    };
                }
            });

            HookManager.getInstance().registerHook("com.wurmonline.client.renderer.gui.CompassComponent", "pick", "(Lcom/wurmonline/client/renderer/PickData;II)V", new InvocationHandlerFactory() {
                @Override
                public InvocationHandler createInvocationHandler() {
                    return new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            method.invoke(proxy, args);
                            Class<?> cls = proxy.getClass();
                            PickData pickData = (PickData) args[0];
                            World world = ReflectionUtil.getPrivateField(proxy, ReflectionUtil.getField(cls, "world"));
                            float prettyAngle = ReflectionUtil.<Float>getPrivateField(proxy, ReflectionUtil.getField(cls, "actualAngle")) % 360.0F;
                            if (prettyAngle < 0.0F) {
                                prettyAngle += 360.0F;
                            }
                            pickData.addText("Angle: " + prettyAngle);
                            pickData.addText(String.format("Position: %.1f / %.1f",
                                    world.getPlayer().getPos().getX() / 4f,
                                    world.getPlayer().getPos().getY() / 4f));
                            pickData.addText(String.format("Height: %.1f", world.getPlayer().getPos().getH() * 10f));
                            return null;
                        }
                    };
                }
            });

            logger.fine("Loaded");
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Error loading mod", e);
        }
    }

    @Override
    public void preInit() {

    }
}
