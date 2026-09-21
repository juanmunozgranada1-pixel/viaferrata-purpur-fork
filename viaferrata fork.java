package com.viaferrata.fork.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ViaFerrataAnimationOptimizer {

    // Distancia máxima al cuadrado (32 bloques) para procesar animaciones secundarias
    private static final double MAX_ANIMATION_DISTANCE_SQ = 1024.0;

    /**
     * Procesa y optimiza el paquete de animación antes de la traducción pesada del protocolo.
     */
    public boolean shouldProcessAnimation(int entityId, double playerX, double playerZ, double targetX, double targetZ) {
        double deltaX = playerX - targetX;
        double deltaZ = playerZ - targetZ;
        
        // Culling: Si está fuera de rango, descartamos el paquete para ahorrar CPU/Red
        if ((deltaX * deltaX + deltaZ * deltaZ) > MAX_ANIMATION_DISTANCE_SQ) {
            return false; 
        }
        return true;
    }

    /**
     * Traducción ultra-rápida de Swing de brazo para 1.7.10
     */
    public void writeFast17Swing(ByteBuf outputBuffer, int entityId) {
        // ID de paquete de animación en 1.7.10
        writeVarInt(outputBuffer, 0x0B); 
        writeVarInt(outputBuffer, entityId);
        outputBuffer.writeByte(0); // Animation 0: Swing arm
    }

    private void writeVarInt(ByteBuf buffer, int value) {
        while ((value & -128) != 0) {
            buffer.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        buffer.writeByte(value);
    }
}