package com.ui_utils.features;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ClipUtils {
    
    private static Boolean hasHorizontalCollisionParam = null;
    private static Boolean hasFromVehicleMethod = null;
    
    private static boolean hasHorizontalCollisionParam() {
        if (hasHorizontalCollisionParam == null) {
            try {
                PlayerMoveC2SPacket.OnGroundOnly.class.getConstructor(boolean.class, boolean.class);
                hasHorizontalCollisionParam = true;
            } catch (NoSuchMethodException e) {
                hasHorizontalCollisionParam = false;
            }
        }
        return hasHorizontalCollisionParam;
    }

    private static boolean hasFromVehicleMethod() {
        if (hasFromVehicleMethod == null) {
            try {
                VehicleMoveC2SPacket.class.getMethod("fromVehicle", Entity.class);
                hasFromVehicleMethod = true;
            } catch (NoSuchMethodException e) {
                hasFromVehicleMethod = false;
            }
        }
        return hasFromVehicleMethod;
    }
    
    private static VehicleMoveC2SPacket createVehiclePacket(Entity vehicle) {
        try {
            if (hasFromVehicleMethod()) {
                Method method = VehicleMoveC2SPacket.class.getMethod("fromVehicle", Entity.class);
                return (VehicleMoveC2SPacket) method.invoke(null, vehicle);
            } else {
                Constructor<VehicleMoveC2SPacket> constructor = VehicleMoveC2SPacket.class.getConstructor(Entity.class);
                return constructor.newInstance(vehicle);
            }
        } catch (Exception e) {
            try {
                Constructor<VehicleMoveC2SPacket> constructor = VehicleMoveC2SPacket.class.getConstructor(
                    double.class, double.class, double.class, float.class, float.class
                );
                return constructor.newInstance(
                    vehicle.getX(), vehicle.getY(), vehicle.getZ(), 
                    vehicle.getYaw(), vehicle.getPitch()
                );
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        }
    }
    
    private static PlayerMoveC2SPacket createOnGroundPacket(boolean onGround, boolean horizontalCollision) {
        try {
            if (hasHorizontalCollisionParam()) {
                Constructor<PlayerMoveC2SPacket.OnGroundOnly> constructor = 
                    PlayerMoveC2SPacket.OnGroundOnly.class.getConstructor(boolean.class, boolean.class);
                return constructor.newInstance(onGround, horizontalCollision);
            } else {
                Constructor<PlayerMoveC2SPacket.OnGroundOnly> constructor = 
                    PlayerMoveC2SPacket.OnGroundOnly.class.getConstructor(boolean.class);
                return constructor.newInstance(onGround);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private static PlayerMoveC2SPacket createPositionPacket(double x, double y, double z, boolean onGround, boolean horizontalCollision) {
        try {
            if (hasHorizontalCollisionParam()) {
                Constructor<PlayerMoveC2SPacket.PositionAndOnGround> constructor = 
                    PlayerMoveC2SPacket.PositionAndOnGround.class.getConstructor(
                        double.class, double.class, double.class, boolean.class, boolean.class
                    );
                return constructor.newInstance(x, y, z, onGround, horizontalCollision);
            } else {
                Constructor<PlayerMoveC2SPacket.PositionAndOnGround> constructor = 
                    PlayerMoveC2SPacket.PositionAndOnGround.class.getConstructor(
                        double.class, double.class, double.class, boolean.class
                    );
                return constructor.newInstance(x, y, z, onGround);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void vClip(double blocks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10));
        if (packetsRequired > 20) {
            packetsRequired = 1;
        }
        
        if (mc.player.hasVehicle()) {
            for (int i = 0; i < (packetsRequired - 1); i++) {
                VehicleMoveC2SPacket packet = createVehiclePacket(mc.player.getVehicle());
                if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            }
            mc.player.getVehicle().setPosition(
                mc.player.getVehicle().getX(), 
                mc.player.getVehicle().getY() + blocks, 
                mc.player.getVehicle().getZ()
            );
            VehicleMoveC2SPacket packet = createVehiclePacket(mc.player.getVehicle());
            if (packet != null) mc.getNetworkHandler().sendPacket(packet);
        } else {
            for (int i = 0; i < (packetsRequired - 1); i++) {
                PlayerMoveC2SPacket packet = createOnGroundPacket(true, mc.player.horizontalCollision);
                if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            }
            PlayerMoveC2SPacket packet = createPositionPacket(
                mc.player.getX(), 
                mc.player.getY() + blocks, 
                mc.player.getZ(), 
                true, 
                mc.player.horizontalCollision
            );
            if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            mc.player.setPosition(mc.player.getX(), mc.player.getY() + blocks, mc.player.getZ());
        }
    }
    
    public static void hClip(double blocks) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        
        int packetsRequired = (int) Math.ceil(Math.abs(blocks / 10));
        if (packetsRequired > 20) {
            packetsRequired = 1;
        }
        
        float yaw = mc.player.getYaw();
        double radians = Math.toRadians(yaw);
        double deltaX = -Math.sin(radians) * blocks;
        double deltaZ = Math.cos(radians) * blocks;
        
        if (mc.player.hasVehicle()) {
            for (int i = 0; i < (packetsRequired - 1); i++) {
                VehicleMoveC2SPacket packet = createVehiclePacket(mc.player.getVehicle());
                if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            }
            mc.player.getVehicle().setPosition(
                mc.player.getVehicle().getX() + deltaX, 
                mc.player.getVehicle().getY(), 
                mc.player.getVehicle().getZ() + deltaZ
            );
            VehicleMoveC2SPacket packet = createVehiclePacket(mc.player.getVehicle());
            if (packet != null) mc.getNetworkHandler().sendPacket(packet);
        } else {
            for (int i = 0; i < (packetsRequired - 1); i++) {
                PlayerMoveC2SPacket packet = createOnGroundPacket(true, mc.player.horizontalCollision);
                if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            }
            PlayerMoveC2SPacket packet = createPositionPacket(
                mc.player.getX() + deltaX, 
                mc.player.getY(), 
                mc.player.getZ() + deltaZ, 
                true, 
                mc.player.horizontalCollision
            );
            if (packet != null) mc.getNetworkHandler().sendPacket(packet);
            mc.player.setPosition(mc.player.getX() + deltaX, mc.player.getY(), mc.player.getZ() + deltaZ);
        }
    }
}
